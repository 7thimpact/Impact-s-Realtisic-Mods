package com.impact.realisticinventory.pack;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.advancement.InventoryTriggers;
import com.impact.realisticinventory.config.InventoryCommonConfig;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

/**
 * Setting packs down as blocks and picking them up again. Placed packs can be switched off on their own in the common
 * config ({@code packs.placedPacks}); packs already placed can still be opened and picked up then.
 */
public final class PlacedPacks {
    private static final float PLACED_PACK_STRENGTH = 0.5F;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);
    private static Supplier<PlacedPackBlock> block;
    private static Supplier<BlockEntityType<PlacedPackBlockEntity>> blockEntityType;

    private PlacedPacks() {
    }

    /** Called once, during mod construction. No item: picking a placed pack up gives the pack itself. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(InventoryMod.MOD_ID);
        block = registrar.register(Registries.BLOCK, "placed_pack", () -> new PlacedPackBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(PLACED_PACK_STRENGTH)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .noLootTable()
                .pushReaction(PushReaction.DESTROY)));
        blockEntityType = registrar.register(Registries.BLOCK_ENTITY_TYPE, "placed_pack",
                () -> LoaderPlatform.current().blockEntityType(PlacedPackBlockEntity::new, block.get()));
    }

    public static PlacedPackBlock block() {
        return block.get();
    }

    public static BlockEntityType<PlacedPackBlockEntity> blockEntityType() {
        return blockEntityType.get();
    }

    public static boolean isPlacingEnabled() {
        return InventoryCommonConfig.get().features.arePlacedPacksEnabled.get() && WornPack.arePacksEnabled();
    }

    /** Sneak-right-click on a block with a pack: it's set down on the clicked face, facing the player. */
    public static InteractionResult placeFromHand(UseOnContext context) {
        Player player = context.getPlayer();
        boolean isPlacing = player != null && player.isSecondaryUseActive() && isPlacingEnabled();
        if (!isPlacing) {
            return InteractionResult.PASS;
        }
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        if (!placeContext.canPlace()) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = context.getItemInHand();
        String itemId = ResourceIds.itemId(held.getItem()).toString();
        place(level, placeContext.getClickedPos(), player.getDirection().getOpposite(), held.copyWithCount(1));
        held.shrink(1);
        LOG.debug(InventoryLogCategories.PACKS, "pack placed", "player", player.getGameProfile().getName(), "item", itemId,
                "pos", placeContext.getClickedPos());
        if (player instanceof ServerPlayer serverPlayer) {
            InventoryTriggers.PACK_PLACED.trigger(serverPlayer);
        }
        return InteractionResult.CONSUME;
    }

    /** Sets a pack down at the position, its front facing the given way. */
    public static void place(Level level, BlockPos position, Direction facing, ItemStack pack) {
        PackKind kind = ((PackItem) pack.getItem()).kind();
        boolean isInWater = level.getFluidState(position).isSourceOfType(Fluids.WATER);
        BlockState state = block().defaultBlockState()
                .setValue(PlacedPackBlock.FACING, facing)
                .setValue(PlacedPackBlock.KIND, kind)
                .setValue(PlacedPackBlock.WATERLOGGED, isInWater);
        level.setBlockAndUpdate(position, state);
        if (level.getBlockEntity(position) instanceof PlacedPackBlockEntity placedPack) {
            placedPack.holdPack(pack);
            level.sendBlockUpdated(position, state, state, Block.UPDATE_CLIENTS);
        }
        level.playSound(null, position, SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
    }

    /**
     * Sneak-right-click with an empty main hand: the pack, contents and all, goes into that hand and the block is gone.
     * Returns whether it was picked up.
     */
    public static boolean pickUp(Player player, BlockPos position) {
        Level level = player.level();
        boolean canPickUp = player.isSecondaryUseActive() && player.getMainHandItem().isEmpty()
                && level.getBlockEntity(position) instanceof PlacedPackBlockEntity;
        if (!canPickUp) {
            return false;
        }
        if (level.isClientSide()) {
            return true;
        }
        PlacedPackBlockEntity placedPack = (PlacedPackBlockEntity) level.getBlockEntity(position);
        ItemStack packWithContents = placedPack.takePack();
        level.removeBlock(position, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, packWithContents);
        level.playSound(null, position, SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        LOG.debug(InventoryLogCategories.PACKS, "pack picked up", "player", player.getGameProfile().getName(),
                "item", ResourceIds.itemId(packWithContents.getItem()), "stacks", PackContents.stacksIn(packWithContents).size(), "pos", position);
        return true;
    }

    static void logDropped(Level level, BlockPos position, ItemStack packWithContents) {
        if (level.isClientSide()) {
            return;
        }
        LOG.debug(InventoryLogCategories.PACKS, "placed pack dropped", "item", ResourceIds.itemId(packWithContents.getItem()),
                "stacks", PackContents.stacksIn(packWithContents).size(), "pos", position);
    }
}
