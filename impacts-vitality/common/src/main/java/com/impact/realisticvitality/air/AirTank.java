package com.impact.realisticvitality.air;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityGear;
import com.impact.realisticvitality.config.VitalityServerConfig;
import net.minecraft.world.item.ItemStack;

/** The two air tanks: carried anywhere (or worn in the back slot) with a Respirator on, they supply air in thin air. */
public enum AirTank {
    AIR_TANK(VitalityGear.AIR_TANK),
    SHULKER_AIR_TANK(VitalityGear.SHULKER_AIR_TANK);

    /** Ticks of air left in the tank. */
    public static final String AIR_KEY = VitalityMod.MOD_ID + ":air";

    public final VitalityGear gear;
    public final String itemPath;

    AirTank(VitalityGear gear) {
        this.gear = gear;
        this.itemPath = gear.itemPath;
    }

    public int capacity() {
        VitalityServerConfig.BreathingSettings settings = VitalityServerConfig.get().breathing;
        return this == AIR_TANK ? settings.airTankTicks.get() : settings.shulkerAirTankTicks.get();
    }

    /** A tank with no NBT yet is full: tanks come out of the crafting grid ready to use. */
    public int airIn(ItemStack tank) {
        return tank.hasTag() && tank.getTag().contains(AIR_KEY) ? tank.getTag().getInt(AIR_KEY) : capacity();
    }

    public void setAir(ItemStack tank, int ticks) {
        tank.getOrCreateTag().putInt(AIR_KEY, Math.max(0, Math.min(capacity(), ticks)));
    }
}
