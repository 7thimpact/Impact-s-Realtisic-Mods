package com.impact.realisticvitality.air;

import com.impact.lib.backslot.BackSlot;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.client.ClientVitality;
import com.impact.realisticvitality.config.VitalityCommonConfig;
import com.impact.realisticvitality.config.VitalityGear;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.item.AirBladderItem;
import com.impact.realisticvitality.item.AirTankItem;
import com.impact.realisticvitality.item.BreathingMaskItem;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * What breathing gear does: masks against smoke (a damp Cloth Mask halves it, a filtered Respirator or Rebreather stops it,
 * the Netherite Rebreather always), Air Tanks against thin air (with a Respirator on), and the Air Bladder and Bottled Air
 * for a few bubbles back. Tanks and filters change once a second, so their items aren't re-sent every tick.
 */
public final class BreathingGear {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final int TICKS_PER_SECOND = 20;

    private BreathingGear() {
    }

    public static Optional<BreathingMask> wornMask(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof BreathingMaskItem maskItem ? Optional.of(maskItem.mask) : Optional.empty();
    }

    /** Smoke counts as good air: a Netherite Rebreather, or a Respirator or Rebreather with a working filter. */
    public static boolean blocksSmoke(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        return wornMask(player).filter(mask -> mask.blocksSmokeAlways || mask.takesFilter && MaskFilters.hasWorkingFilter(head)).isPresent();
    }

    public static boolean isDampMaskWorn(Player player) {
        return wornMask(player).filter(mask -> mask == BreathingMask.CLOTH_MASK).isPresent()
                && BreathingMaskItem.dampTicks(player.getItemBySlot(EquipmentSlot.HEAD)) > 0;
    }

    /** With a Respirator or Rebreather on, a tank with air in the back slot or anywhere in the inventory. */
    public static boolean hasTankAir(Player player) {
        if (wornMask(player).filter(BreathingMask::connectsTanks).isEmpty()) {
            return false;
        }
        boolean[] found = new boolean[1];
        forEachTank(player, tank -> found[0] |= airIn(tank) > 0);
        return found[0];
    }

    /** The air around the player, as this side knows it. */
    public static AirQuality qualityFor(Player player) {
        return player.level().isClientSide ? ClientVitality.state().airQuality() : VitalityState.of(player).air.quality();
    }

    /** Once a second: masks dry, filters wear in smoke, tanks give air in thin air and refill in good air. */
    public static void tickSecond(ServerPlayer player, AirQuality quality, boolean isDrainingWithoutGear) {
        Optional<BreathingMask> mask = wornMask(player);
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        mask.filter(worn -> worn == BreathingMask.CLOTH_MASK).ifPresent(worn -> dryMask(player, head));
        if (quality.isSmoke && isDrainingWithoutGear) {
            mask.filter(worn -> worn.takesFilter).ifPresent(worn -> wearFilter(player, head, worn));
        }
        if (quality == AirQuality.GOOD) {
            refillTanks(player);
        } else if (!quality.isSmoke && isDrainingWithoutGear) {
            mask.filter(BreathingMask::connectsTanks).ifPresent(worn -> breatheFromTank(player, worn));
        }
    }

    /** The Air Bladder: fills in good air; in thin air each use gives back 4 bubbles. */
    public static boolean useAirBladder(Level level, Player player, ItemStack bladder) {
        VitalityServerConfig.BreathingSettings settings = VitalityServerConfig.get().breathing;
        AirQuality quality = qualityFor(player);
        int uses = AirBladderItem.uses(bladder);
        if (quality == AirQuality.GOOD && uses < settings.airBladderUses.get()) {
            level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 0.5F, 1.6F);
            AirBladderItem.setUses(bladder, settings.airBladderUses.get());
            return true;
        }
        boolean isThin = quality != AirQuality.GOOD && !quality.isSmoke;
        if (!isThin || uses <= 0) {
            return false;
        }
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 0.8F, 1.0F);
        AirBladderItem.setUses(bladder, uses - 1);
        addBubbles(player, settings.airBladderBubbles.get());
        logGear(player, "air bladder used", "usesLeft", uses - 1);
        return true;
    }

    /** A glass bottle used in good air (not looking at water) bottles it. PASS anywhere else. */
    public static InteractionResult tryBottleAir(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean isOn = AirTicks.isOn() && VitalityCommonConfig.get().isOn(VitalityGear.BOTTLED_AIR);
        if (!held.is(Items.GLASS_BOTTLE) || !isOn || qualityFor(player) != AirQuality.GOOD) {
            return InteractionResult.PASS;
        }
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.6F, 1.6F);
        if (!level.isClientSide) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(VitalityItems.bottledAir())));
            logGear(player, "air bottled");
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void dryMask(ServerPlayer player, ItemStack head) {
        int damp = BreathingMaskItem.dampTicks(head);
        if (damp <= 0) {
            return;
        }
        BreathingMaskItem.setDampTicks(head, damp - TICKS_PER_SECOND);
        if (damp <= TICKS_PER_SECOND) {
            logGear(player, "mask dried");
        }
    }

    // A Rebreather wears its filter only every other second, so it lasts twice as long.
    private static void wearFilter(ServerPlayer player, ItemStack head, BreathingMask mask) {
        ItemStack filter = MaskFilters.filterIn(head);
        int everySeconds = (int) Math.round(mask.filterLifeMultiplier());
        if (filter.isEmpty() || player.tickCount / TICKS_PER_SECOND % everySeconds != 0) {
            return;
        }
        filter.setDamageValue(filter.getDamageValue() + 1);
        if (filter.getDamageValue() >= filter.getMaxDamage()) {
            MaskFilters.setFilter(head, ItemStack.EMPTY);
            player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
            logGear(player, "filter worn out", "mask", mask.itemPath);
            return;
        }
        MaskFilters.setFilter(head, filter);
    }

    private static void breatheFromTank(ServerPlayer player, BreathingMask mask) {
        int used = (int) Math.max(1, Math.round(TICKS_PER_SECOND / mask.tankLifeMultiplier()));
        boolean[] breathed = new boolean[1];
        forEachTank(player, tank -> {
            int air = airIn(tank);
            if (breathed[0] || air <= 0) {
                return;
            }
            breathed[0] = true;
            setAir(tank, air - used);
            if (air - used <= 0) {
                logGear(player, "tank empty", "tank", ((AirTankItem) tank.getItem()).tank.itemPath);
            }
        });
    }

    private static void refillTanks(ServerPlayer player) {
        int refillTicks = VitalityServerConfig.get().breathing.tankRefillTicks.get();
        forEachTank(player, tank -> {
            AirTank kind = ((AirTankItem) tank.getItem()).tank;
            int air = kind.airIn(tank);
            if (air >= kind.capacity()) {
                return;
            }
            int perSecond = Math.max(1, kind.capacity() * TICKS_PER_SECOND / refillTicks);
            kind.setAir(tank, air + perSecond);
            if (air + perSecond >= kind.capacity()) {
                logGear(player, "tank refilled", "tank", kind.itemPath);
            }
        });
    }

    /** Every tank the player carries: the back slot first (written back after), then the inventory. */
    private static void forEachTank(Player player, Consumer<ItemStack> action) {
        ItemStack onBack = BackSlot.get(player);
        if (onBack.getItem() instanceof AirTankItem) {
            ItemStack backTank = onBack.copy();
            action.accept(backTank);
            if (!ItemStack.matches(backTank, onBack)) {
                BackSlot.set(player, backTank);
            }
        }
        player.getInventory().items.stream().filter(stack -> stack.getItem() instanceof AirTankItem).forEach(action);
        player.getInventory().offhand.stream().filter(stack -> stack.getItem() instanceof AirTankItem).forEach(action);
    }

    private static int airIn(ItemStack tank) {
        return ((AirTankItem) tank.getItem()).tank.airIn(tank);
    }

    private static void setAir(ItemStack tank, int ticks) {
        ((AirTankItem) tank.getItem()).tank.setAir(tank, ticks);
    }

    private static void addBubbles(Player player, int bubbles) {
        player.setAirSupply(Math.min(player.getMaxAirSupply(), player.getAirSupply() + bubbles * AirTicks.AIR_PER_BUBBLE));
    }

    private static void logGear(Player player, String event, Object... details) {
        Object[] keyValues = new Object[details.length + 2];
        keyValues[0] = "player";
        keyValues[1] = player.getScoreboardName();
        System.arraycopy(details, 0, keyValues, 2, details.length);
        LOG.debug(VitalityLogCategories.GEAR, event, keyValues);
    }
}
