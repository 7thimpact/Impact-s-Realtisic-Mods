package com.impact.realisticvitality.item;

import com.impact.realisticvitality.VitalityMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** The materials of the breathing masks worn on the head. Only the Rebreathers give armor. */
public enum BreathingArmorMaterial implements ArmorMaterial {
    CLOTH("cloth_mask", 55, 0, 0.0F, 0.0F, SoundEvents.ARMOR_EQUIP_LEATHER, Items.WHITE_WOOL),
    RESPIRATOR("respirator", 110, 0, 0.0F, 0.0F, SoundEvents.ARMOR_EQUIP_LEATHER, Items.LEATHER),
    REBREATHER("rebreather", 165, 2, 0.0F, 0.0F, SoundEvents.ARMOR_EQUIP_GOLD, Items.GOLD_INGOT),
    NETHERITE_REBREATHER("netherite_rebreather", 407, 3, 3.0F, 0.1F, SoundEvents.ARMOR_EQUIP_NETHERITE, Items.NETHERITE_INGOT);

    private static final int ENCHANTABILITY = 12;

    private final String textureName;
    private final int durability;
    private final int defense;
    private final float toughness;
    private final float knockbackResistance;
    private final SoundEvent equipSound;
    private final ItemLike repairItem;

    BreathingArmorMaterial(String textureName, int durability, int defense, float toughness, float knockbackResistance, SoundEvent equipSound,
                           ItemLike repairItem) {
        this.textureName = textureName;
        this.durability = durability;
        this.defense = defense;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.equipSound = equipSound;
        this.repairItem = repairItem;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return durability;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return defense;
    }

    @Override
    public int getEnchantmentValue() {
        return ENCHANTABILITY;
    }

    @Override
    public SoundEvent getEquipSound() {
        return equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(repairItem);
    }

    /** The worn texture is {@code impactsvitality:textures/models/armor/<name>_layer_1.png}. */
    @Override
    public String getName() {
        return VitalityMod.MOD_ID + ":" + textureName;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
