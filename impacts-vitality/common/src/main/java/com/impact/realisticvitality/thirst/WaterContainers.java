package com.impact.realisticvitality.thirst;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.item.WaterBottleItem;
import com.impact.realisticvitality.item.WaterContainerItem;
import com.impact.realisticvitality.player.VitalityState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;
import java.util.Set;

/**
 * What drink containers do: fill from water blocks and cauldrons (mixing purity down to the worst), drink one at a time,
 * pour over yourself to get Wet, and (the Tidebound Canteen) refill themselves with purified water.
 */
public final class WaterContainers {
    /** The campfire recipes that purify a filled Wooden Cup; their time comes from the config. */
    public static final Set<ResourceLocation> CUP_PURIFYING_RECIPES = Set.of(
            ResourceIds.of(VitalityMod.MOD_ID, "wooden_cup_purifying_unclean"),
            ResourceIds.of(VitalityMod.MOD_ID, "wooden_cup_purifying_salt"));
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final int POUR_SPLASH_PARTICLES = 20;

    private WaterContainers() {
    }

    public static int capacity(WaterContainer container) {
        return VitalityServerConfig.get().containers.capacities.get(container).get();
    }

    public static Optional<WaterContainer> containerOf(ItemStack stack) {
        return stack.getItem() instanceof WaterContainerItem containerItem ? Optional.of(containerItem.container) : Optional.empty();
    }

    /** A container used while looking at water: fills it up. PASS when it's full, holds soup, or no water is in reach. */
    public static InteractionResult tryFillFromWorld(Level level, Player player, InteractionHand hand, WaterContainer container) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = WaterBottleItem.lookedAtWater(level, player, ClipContext.Fluid.SOURCE_ONLY);
        boolean isLookingAtWater = hit.getType() == HitResult.Type.BLOCK && level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER);
        if (!ThirstDrain.isOn() || !isLookingAtWater || !level.mayInteract(player, hit.getBlockPos())) {
            return InteractionResult.PASS;
        }
        ContainerContents contents = ContainerContents.read(stack);
        int room = capacity(container) - contents.drinks();
        WaterPurity purity = WaterSources.purityAt(level, hit.getBlockPos());
        Optional<ContainerContents> filled = contents.withWater(room, purity, false, container.keepsTemperature);
        if (room <= 0 || filled.isEmpty()) {
            return InteractionResult.PASS;
        }
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PICKUP, hit.getBlockPos());
        if (!level.isClientSide) {
            filled.get().writeTo(stack);
            logFilled(player, stack, filled.get(), "water");
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Vitality's entry in vanilla's cauldron table for each container: one level of cauldron water is one drink. */
    public static InteractionResult fillFromCauldron(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, ItemStack stack) {
        Optional<WaterContainer> container = containerOf(stack);
        if (container.isEmpty() || !ThirstDrain.isOn()) {
            return InteractionResult.PASS;
        }
        ContainerContents contents = ContainerContents.read(stack);
        Optional<ContainerContents> filled = contents.withWater(1, WaterSources.cauldronPurity(level, position), false, container.get().keepsTemperature);
        if (contents.drinks() >= capacity(container.get()) || filled.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            filled.get().writeTo(stack);
            player.awardStat(Stats.USE_CAULDRON);
            LayeredCauldronBlock.lowerFillLevel(state, level, position);
            level.playSound(null, position, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, position);
            logFilled(player, stack, filled.get(), "cauldron");
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Sneak-use: pours one drink over yourself, which leaves you Wet and cools you off. */
    public static InteractionResult pourOverSelf(Level level, Player player, ItemStack stack) {
        ContainerContents contents = ContainerContents.read(stack);
        if (contents.isEmpty() || contents.kind() == ContainerContents.Kind.SOUP) {
            return InteractionResult.PASS;
        }
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.8F, 1.2F);
        if (level instanceof ServerLevel serverLevel) {
            contents.minusOneDrink().writeTo(stack);
            VitalityState.of(player).temperature.setWetTicks(VitalityServerConfig.get().temperature.wetTicks.get());
            serverLevel.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getEyeY(), player.getZ(), POUR_SPLASH_PARTICLES, 0.3, 0.3, 0.3, 0.1);
            LOG.debug(VitalityLogCategories.DRINK, "poured over self", "player", player.getScoreboardName(),
                    "item", ResourceIds.itemId(stack.getItem()), "drinksLeft", ContainerContents.read(stack).drinks());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Takes one drink out after drinking finishes. Thirst comes from the drink table, through the same finished-using
     * hook as every other drink; a soup also feeds you, as the bowl would have.
     */
    public static void finishDrink(ServerPlayer player, ItemStack stack) {
        ContainerContents contents = ContainerContents.read(stack);
        if (contents.isEmpty()) {
            return;
        }
        contents.soup().flatMap(ResourceIds::item).ifPresent(soup -> feedSoup(player, soup));
        contents.minusOneDrink().writeTo(stack);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
    }

    /** The Tidebound Canteen, each tick it's carried: 1 drink of purified water every so often, faster in rain or water. */
    public static void refillTick(ItemStack stack, Entity carrier, long gameTime) {
        Optional<WaterContainer> container = containerOf(stack).filter(candidate -> candidate.refillsItself);
        if (container.isEmpty() || !ThirstDrain.isOn()) {
            return;
        }
        VitalityServerConfig.ContainerSettings settings = VitalityServerConfig.get().containers;
        int interval = carrier.isInWaterRainOrBubble() ? settings.tideboundWetRefillTicks.get() : settings.tideboundRefillTicks.get();
        ContainerContents contents = ContainerContents.read(stack);
        if (gameTime % interval != 0 || contents.drinks() >= capacity(container.get())) {
            return;
        }
        contents.withWater(1, WaterPurity.PURIFIED, false, false).ifPresent(refilled -> {
            refilled.writeTo(stack);
            LOG.debug(VitalityLogCategories.DRINK, "self refill", "item", ResourceIds.itemId(stack.getItem()), "drinks", refilled.drinks(),
                    "purity", refilled.purity().serializedName);
        });
    }

    // Forge deprecates this in favor of its entity-aware overload, which loader-independent code can't call; both feed the same.
    @SuppressWarnings("deprecation")
    private static void feedSoup(ServerPlayer player, Item soup) {
        if (soup.isEdible()) {
            player.getFoodData().eat(soup, new ItemStack(soup));
        }
    }

    private static void logFilled(Player player, ItemStack stack, ContainerContents contents, String source) {
        LOG.debug(VitalityLogCategories.DRINK, "container filled", "player", player.getScoreboardName(), "item", ResourceIds.itemId(stack.getItem()),
                "source", source, "purity", contents.purity().serializedName, "drinks", contents.drinks());
    }
}
