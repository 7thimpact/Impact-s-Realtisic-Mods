package com.impact.realisticvitality.client;

import com.impact.realisticvitality.air.AirQuality;
import com.impact.realisticvitality.config.VitalityClientConfig;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.registry.VitalitySounds;
import com.impact.realisticvitality.temperature.BodyState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * The player's own body sounds, heard by the player: panting while thirsty, shivering while Cold or colder, and a cough
 * each time smoke takes a bubble. Played on the client so the client config ({@code display.needSounds}) can turn them off.
 */
public final class NeedSounds {
    private static final int PANT_TICKS = 100;
    private static final int SHIVER_TICKS = 80;
    private static final float VOLUME = 0.6F;
    private static int lastAirSupply = -1;

    private NeedSounds() {
    }

    /** Called at the end of each client tick. */
    public static void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !VitalityClientConfig.get().display.areNeedSoundsOn.get() || player.isSpectator()) {
            lastAirSupply = -1;
            return;
        }
        boolean isThirsty = ClientVitality.isNeedOn(VitalityNeed.THIRST)
                && ClientVitality.state().thirstLevel() <= VitalityServerConfig.get().thirst.noSprintAtOrBelow.get();
        if (isThirsty && player.tickCount % PANT_TICKS == 0) {
            play(player, VitalitySounds.pant(), 1.0F);
        }
        boolean isShivering = ClientVitality.isNeedOn(VitalityNeed.TEMPERATURE) && BodyState.of(ClientVitality.state().bodyTemperature()).isColdOrWorse();
        if (isShivering && player.tickCount % SHIVER_TICKS == 0) {
            play(player, VitalitySounds.shiver(), 1.4F);
        }
        AirQuality quality = ClientVitality.state().airQuality();
        boolean lostABubbleToSmoke = quality.isSmoke && lastAirSupply >= 0 && player.getAirSupply() < lastAirSupply && !player.isUnderWater();
        if (lostABubbleToSmoke) {
            play(player, VitalitySounds.cough(), 1.0F);
        }
        lastAirSupply = player.getAirSupply();
    }

    private static void play(LocalPlayer player, SoundEvent sound, float pitch) {
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, VOLUME, pitch, false);
    }
}
