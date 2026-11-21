package com.impact.realisticvitality.thirst;

import com.impact.realisticvitality.config.VitalityFeatures;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.item.WaterBottleItem;
import com.impact.realisticvitality.player.ThirstState;
import com.impact.realisticvitality.player.VitalityState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;

/** Sipping by hand: sneak-use water with an empty main hand for 1 thirst of unclean water. */
public final class Sipping {
    private static final String SIP_SOURCE = "sip";

    private Sipping() {
    }

    /** True when the player is set up to sip and looking at water within reach. Works on either side. */
    public static boolean canSip(Player player) {
        boolean isOn = ThirstDrain.isOn() && VitalityFeatures.byConditionName(VitalityFeatures.SIPPING).orElseThrow().getAsBoolean();
        if (!isOn || !player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) {
            return false;
        }
        Level level = player.level();
        BlockHitResult hit = WaterBottleItem.lookedAtWater(level, player, ClipContext.Fluid.ANY);
        return hit.getType() == HitResult.Type.BLOCK && level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER);
    }

    /** Takes a sip if the player can, at most once per {@code thirst.sipCooldownTicks}. */
    public static boolean trySip(ServerPlayer player) {
        if (!canSip(player)) {
            return false;
        }
        VitalityServerConfig.ThirstSettings settings = VitalityServerConfig.get().thirst;
        ThirstState thirst = VitalityState.of(player).thirst;
        long now = player.level().getGameTime();
        if (now - thirst.lastSipGameTime() < settings.sipCooldownTicks.get()) {
            return true;
        }
        thirst.setLastSipGameTime(now);
        DrinkValue sip = new DrinkValue(settings.sipThirst.get(), 0.0F);
        Drinking.drink(player, sip, Optional.of(WaterPurity.UNCLEAN), SIP_SOURCE, VitalityLogCategories.DRINK);
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F, 1.0F);
        player.swing(InteractionHand.MAIN_HAND, true);
        return true;
    }
}
