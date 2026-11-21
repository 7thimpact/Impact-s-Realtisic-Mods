package com.impact.realisticvitality.item;

import com.impact.lib.backslot.BackSlot;
import com.impact.realisticvitality.lang.VitalityTranslations;
import com.impact.realisticvitality.thirst.WaterContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** The Hydration Pack: worn in the back slot when a mod provides one (Inventory), otherwise in the chest slot, and sipped with a key. */
public final class HydrationPackItem extends WaterContainerItem implements Equipable {
    public HydrationPackItem() {
        super(WaterContainer.HYDRATION_PACK, new Item.Properties().stacksTo(1));
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return BackSlot.isAvailable() ? EquipmentSlot.MAINHAND : EquipmentSlot.CHEST;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String wornKey = BackSlot.isAvailable() ? VitalityTranslations.PACK_WORN_ON_BACK : VitalityTranslations.PACK_WORN_ON_CHEST;
        tooltip.add(Component.translatable(wornKey, Component.keybind(VitalityTranslations.SIP_PACK_KEY)).withStyle(ChatFormatting.BLUE));
    }
}
