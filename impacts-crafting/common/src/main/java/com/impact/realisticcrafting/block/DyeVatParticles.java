package com.impact.realisticcrafting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.Optional;

/** The Dye Vat's particles, sent from the server: a puff of the new color when dye goes in, and a splash with each dip. */
final class DyeVatParticles {
    private static final double WATER_SURFACE_Y = 0.85;
    private static final double SPREAD = 0.2;
    private static final float DUST_SIZE = 1.0F;
    private static final int BURST_DUST = 12;
    private static final int DIP_SPLASHES = 8;
    private static final int DIP_DUST = 4;
    private static final double SPLASH_SPEED = 0.1;

    private DyeVatParticles() {
    }

    static void colorBurst(Level level, BlockPos position, Optional<DyeColor> color) {
        if (level instanceof ServerLevel serverLevel && color.isPresent()) {
            sendDust(serverLevel, position, color.get(), BURST_DUST);
        }
    }

    static void dipSplash(Level level, BlockPos position, Optional<DyeColor> color) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(ParticleTypes.SPLASH, position.getX() + 0.5, position.getY() + WATER_SURFACE_Y, position.getZ() + 0.5,
                DIP_SPLASHES, SPREAD, 0.0, SPREAD, SPLASH_SPEED);
        color.ifPresent(dyeColor -> sendDust(serverLevel, position, dyeColor, DIP_DUST));
    }

    private static void sendDust(ServerLevel level, BlockPos position, DyeColor color, int count) {
        float[] rgb = color.getTextureDiffuseColors();
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(rgb[0], rgb[1], rgb[2]), DUST_SIZE);
        level.sendParticles(dust, position.getX() + 0.5, position.getY() + WATER_SURFACE_Y, position.getZ() + 0.5, count, SPREAD, 0.05, SPREAD, 0.0);
    }
}
