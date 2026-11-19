package com.impact.realisticfood.client;

import com.impact.realisticfood.boiling.ClientBoilingPots;
import com.impact.realisticfood.config.FoodClientConfig;
import com.impact.realisticfood.network.PotStateMessage;
import com.impact.realisticfood.registry.FoodSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/** Client side: bubbles, steam and a simmering sound over every boiling pot the server reported. */
public final class BoilingEffects {
    private static final double SURFACE_SPREAD = 0.55;
    private static final float BUBBLE_CHANCE = 0.7F;
    private static final float STEAM_CHANCE = 0.12F;
    private static final double STEAM_RISE_SPEED = 0.04;
    private static final float SIMMER_CHANCE = 0.04F;
    private static final float SIMMER_VOLUME = 0.35F;
    private static final float SIMMER_PITCH_BASE = 0.8F;
    private static final float SIMMER_PITCH_SPREAD = 0.4F;
    private static ClientLevel lastLevel;

    private BoilingEffects() {
    }

    public static void onPotState(PotStateMessage message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            ClientBoilingPots.update(message.position(), message.surfaceCenter(), message.isBoiling(), message.soupState(), level.getGameTime());
        }
    }

    /** Once per client tick. */
    public static void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != lastLevel) {
            ClientBoilingPots.clear();
            lastLevel = level;
        }
        if (level == null || Minecraft.getInstance().isPaused()) {
            return;
        }
        FoodClientConfig.BoilingDisplaySettings settings = FoodClientConfig.get().boiling;
        ClientBoilingPots.activePots(level.getGameTime()).values().stream().filter(ClientBoilingPots.PotView::isBoiling).forEach(pot -> {
            if (settings.areParticlesOn.get()) {
                spawnParticles(level, pot.surfaceCenter());
            }
            if (settings.areSoundsOn.get()) {
                playSimmer(level, pot.surfaceCenter());
            }
        });
    }

    private static void spawnParticles(ClientLevel level, Vec3 surface) {
        RandomSource random = level.random;
        if (random.nextFloat() < BUBBLE_CHANCE) {
            level.addParticle(ParticleTypes.BUBBLE_POP, spread(surface.x, random), surface.y, spread(surface.z, random), 0.0, 0.0, 0.0);
        }
        if (random.nextFloat() < STEAM_CHANCE) {
            level.addParticle(ParticleTypes.CLOUD, spread(surface.x, random), surface.y + 0.1, spread(surface.z, random), 0.0, STEAM_RISE_SPEED, 0.0);
        }
    }

    private static void playSimmer(ClientLevel level, Vec3 surface) {
        RandomSource random = level.random;
        if (random.nextFloat() >= SIMMER_CHANCE) {
            return;
        }
        float pitch = SIMMER_PITCH_BASE + random.nextFloat() * SIMMER_PITCH_SPREAD;
        level.playLocalSound(surface.x, surface.y, surface.z, FoodSounds.potSimmer(), SoundSource.BLOCKS, SIMMER_VOLUME, pitch, false);
    }

    private static double spread(double center, RandomSource random) {
        return center + (random.nextDouble() - 0.5) * SURFACE_SPREAD;
    }
}
