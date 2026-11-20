package com.impact.realisticvitality.air;

import com.impact.realisticvitality.config.VitalityGear;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.item.BreathingArmorMaterial;

/** The masks worn on the head, and what each does against smoke and thin air. */
public enum BreathingMask {
    /** Damp, it halves smoky air's bubble loss. */
    CLOTH_MASK(VitalityGear.CLOTH_MASK, BreathingArmorMaterial.CLOTH, false, false, false),
    /** With a Charcoal Filter, smoky air counts as good. Lets an Air Tank supply air. */
    RESPIRATOR(VitalityGear.RESPIRATOR, BreathingArmorMaterial.RESPIRATOR, true, false, false),
    /** A Respirator whose filters and tanks last twice as long. */
    REBREATHER(VitalityGear.REBREATHER, BreathingArmorMaterial.REBREATHER, true, false, true),
    /** Never needs a filter: smoke never affects you. Fire-resistant. */
    NETHERITE_REBREATHER(VitalityGear.NETHERITE_REBREATHER, BreathingArmorMaterial.NETHERITE_REBREATHER, false, true, true);

    public final VitalityGear gear;
    public final String itemPath;
    public final BreathingArmorMaterial material;
    public final boolean takesFilter;
    public final boolean blocksSmokeAlways;
    public final boolean isRebreather;

    BreathingMask(VitalityGear gear, BreathingArmorMaterial material, boolean takesFilter, boolean blocksSmokeAlways, boolean isRebreather) {
        this.gear = gear;
        this.itemPath = gear.itemPath;
        this.material = material;
        this.takesFilter = takesFilter;
        this.blocksSmokeAlways = blocksSmokeAlways;
        this.isRebreather = isRebreather;
    }

    /** Respirators and Rebreathers carry the hose an Air Tank needs; a Cloth Mask doesn't. */
    public boolean connectsTanks() {
        return this != CLOTH_MASK;
    }

    /** How many times longer a filter lasts in this mask. */
    public double filterLifeMultiplier() {
        return isRebreather ? VitalityServerConfig.get().breathing.rebreatherFilterMultiplier.get() : 1.0;
    }

    /** How many times longer an Air Tank lasts with this mask. */
    public double tankLifeMultiplier() {
        return isRebreather ? VitalityServerConfig.get().breathing.rebreatherTankMultiplier.get() : 1.0;
    }
}
