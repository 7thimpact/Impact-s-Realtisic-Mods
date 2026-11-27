package com.impact.realisticinventory.forge.curios;

import com.impact.realisticinventory.pack.WornPack;
import com.impact.realisticinventory.pack.WornSlots;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * How Curios treats Inventory's packs: they fit the back slot and the chest (Curios's body slot) while packs and that
 * slot are switched on, and sound like leather. Right-click wearing stays Inventory's own (it swaps with the pack worn
 * there), so Curios's is off.
 */
public final class PackCurio implements ICurioItem {
    private static final float EQUIP_VOLUME = 1.0F;
    private static final float EQUIP_PITCH = 1.0F;

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CuriosWornSlots.placeOf(slotContext.identifier()).filter(WornSlots::isEnabled).isPresent() && WornPack.arePacksEnabled();
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_LEATHER, EQUIP_VOLUME, EQUIP_PITCH);
    }
}
