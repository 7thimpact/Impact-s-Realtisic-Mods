package com.impact.realisticvitality.forge.compat.curios;

import com.impact.realisticvitality.item.AirTankItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * How Curios treats Vitality's gear in its slots: equipping sounds like the gear (a mask or hat like its armor
 * material, a tank like iron, the Hydration Pack like leather). Right-clicking an Air Tank straps it into an empty back
 * slot; everything else keeps its own right-click (drinking, wearing on the head), so Curios's right-click equip stays
 * off for it.
 */
final class VitalityCurio implements ICurioItem {
    private static final float EQUIP_VOLUME = 1.0F;
    private static final float EQUIP_PITCH = 1.0F;

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(equipSoundOf(stack), EQUIP_VOLUME, EQUIP_PITCH);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return stack.getItem() instanceof AirTankItem;
    }

    private static SoundEvent equipSoundOf(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getMaterial().getEquipSound();
        }
        return stack.getItem() instanceof AirTankItem ? SoundEvents.ARMOR_EQUIP_IRON : SoundEvents.ARMOR_EQUIP_LEATHER;
    }
}
