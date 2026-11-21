package com.impact.realisticvitality.thirst;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.item.WaterBottleItem;
import com.impact.realisticvitality.mixin.DispenserBlockAccessor;
import com.impact.realisticvitality.registry.VitalityItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Every bottle of water picks up its purity when it's filled: from water blocks (Vitality's own glass-bottle handler),
 * from water cauldrons (Vitality's entry in vanilla's cauldron table) and from dispensers. While thirst is off, bottles
 * fill as in vanilla. Vitality's water also pours back into cauldrons like a water bottle.
 */
public final class BottleFilling {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final double DRAGON_BREATH_REACH = 2.0;
    private static final int FULL_CAULDRON = 3;
    private static volatile CauldronInteraction vanillaCauldronFill;

    private BottleFilling() {
    }

    /** Once every mod is ready: takes over glass bottles at water cauldrons and dispensers, fills containers at cauldrons, and lets cauldrons take Vitality's water. */
    public static void registerInteractions() {
        vanillaCauldronFill = CauldronInteraction.WATER.get(Items.GLASS_BOTTLE);
        CauldronInteraction.WATER.put(Items.GLASS_BOTTLE, BottleFilling::fillFromCauldron);
        for (BottledWater water : BottledWater.values()) {
            Item waterItem = VitalityItems.waterBottle(water);
            CauldronInteraction.EMPTY.put(waterItem, BottleFilling::pourIntoEmptyCauldron);
            CauldronInteraction.WATER.put(waterItem, BottleFilling::pourIntoWaterCauldron);
        }
        for (WaterContainer container : WaterContainer.values()) {
            CauldronInteraction.WATER.put(VitalityItems.container(container), WaterContainers::fillFromCauldron);
        }
        DispenseItemBehavior vanillaDispense = DispenserBlockAccessor.impactsvitality$behaviors().get(Items.GLASS_BOTTLE);
        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE, (source, stack) -> dispense(source, stack, vanillaDispense));
    }

    /** A glass bottle used while looking at water. PASS when it isn't, so vanilla and other mods handle it. */
    public static InteractionResult fillFromWorld(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.GLASS_BOTTLE) || !ThirstDrain.isOn() || hasDragonBreathNearby(level, player)) {
            return InteractionResult.PASS;
        }
        BlockHitResult hit = WaterBottleItem.lookedAtWater(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        BlockPos waterPosition = hit.getBlockPos();
        if (!level.mayInteract(player, waterPosition) || !level.getFluidState(waterPosition).is(FluidTags.WATER)) {
            return InteractionResult.PASS;
        }
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PICKUP, waterPosition);
        if (!level.isClientSide) {
            giveBottle(player, hand, held, WaterSources.purityAt(level, waterPosition), "water");
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    static InteractionResult fillFromCauldron(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, ItemStack held) {
        if (!ThirstDrain.isOn()) {
            return vanillaCauldronFill.interact(state, level, position, player, hand, held);
        }
        if (!level.isClientSide) {
            giveBottle(player, hand, held, WaterSources.cauldronPurity(level, position), "cauldron");
            player.awardStat(Stats.USE_CAULDRON);
            LayeredCauldronBlock.lowerFillLevel(state, level, position);
            level.playSound(null, position, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, position);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    static InteractionResult pourIntoEmptyCauldron(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, ItemStack held) {
        if (!level.isClientSide) {
            emptyBottle(player, hand, held);
            level.setBlockAndUpdate(position, Blocks.WATER_CAULDRON.defaultBlockState());
            pourEffects(level, position);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    static InteractionResult pourIntoWaterCauldron(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, ItemStack held) {
        if (state.getValue(LayeredCauldronBlock.LEVEL) == FULL_CAULDRON) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            emptyBottle(player, hand, held);
            level.setBlockAndUpdate(position, state.cycle(LayeredCauldronBlock.LEVEL));
            pourEffects(level, position);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void giveBottle(Player player, InteractionHand hand, ItemStack held, WaterPurity purity, String source) {
        BottledWater water = BottledWater.filledWith(purity);
        player.awardStat(Stats.ITEM_USED.get(held.getItem()));
        player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, VitalityItems.bottle(water)));
        logFilled(player, purity, source);
    }

    static void logFilled(Player player, WaterPurity purity, String source) {
        LOG.debug(VitalityLogCategories.DRINK, "bottle filled", "player", player.getScoreboardName(), "source", source,
                "purity", purity.serializedName, "boiling", purity == WaterPurity.PURIFIED);
    }

    private static void emptyBottle(Player player, InteractionHand hand, ItemStack held) {
        player.awardStat(Stats.USE_CAULDRON);
        player.awardStat(Stats.ITEM_USED.get(held.getItem()));
        player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(Items.GLASS_BOTTLE)));
    }

    private static void pourEffects(Level level, BlockPos position) {
        level.playSound(null, position, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(null, GameEvent.FLUID_PLACE, position);
    }

    // Vanilla gives dragon's breath before water when a bottle is used near a breath cloud.
    private static boolean hasDragonBreathNearby(Level level, Player player) {
        AABB reach = player.getBoundingBox().inflate(DRAGON_BREATH_REACH);
        return !level.getEntitiesOfClass(AreaEffectCloud.class, reach,
                cloud -> cloud.isAlive() && cloud.getParticle().getType() == ParticleTypes.DRAGON_BREATH).isEmpty();
    }

    private static ItemStack dispense(BlockSource source, ItemStack stack, DispenseItemBehavior vanillaDispense) {
        ServerLevel level = source.getLevel();
        BlockPos target = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
        if (!ThirstDrain.isOn() || !level.getFluidState(target).is(FluidTags.WATER)) {
            return vanillaDispense.dispense(source, stack);
        }
        ItemStack filled = VitalityItems.bottle(BottledWater.filledWith(WaterSources.purityAt(level, target)));
        LOG.debug(VitalityLogCategories.DRINK, "bottle filled", "source", "dispenser", "purity", VitalityItems.bottledWater(filled).orElseThrow().purity.serializedName);
        stack.shrink(1);
        if (stack.isEmpty()) {
            level.gameEvent(null, GameEvent.FLUID_PICKUP, source.getPos());
            return filled;
        }
        DispenserBlockEntity dispenser = source.getEntity();
        if (dispenser.addItem(filled) < 0) {
            new DefaultDispenseItemBehavior().dispense(source, filled);
        }
        return stack;
    }
}
