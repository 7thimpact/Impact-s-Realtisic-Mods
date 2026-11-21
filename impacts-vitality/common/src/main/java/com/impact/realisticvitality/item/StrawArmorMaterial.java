package com.impact.realisticvitality.item;

import com.impact.realisticvitality.VitalityMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/** Woven straw: no armor at all, only shade. The Straw Hat is its one piece. */
public final class StrawArmorMaterial implements ArmorMaterial {
    public static final StrawArmorMaterial INSTANCE = new StrawArmorMaterial();
    /** As durable as a leather cap. */
    private static final int DURABILITY = 55;
    private static final int ENCHANTABILITY = 12;

    private StrawArmorMaterial() {
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return DURABILITY;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return 0;
    }

    @Override
    public int getEnchantmentValue() {
        return ENCHANTABILITY;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Items.WHEAT);
    }

    /** Its worn texture is {@code impactsvitality:textures/models/armor/straw_layer_1.png}. */
    @Override
    public String getName() {
        return VitalityMod.MOD_ID + ":straw";
    }

    @Override
    public float getToughness() {
        return 0.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }
}
