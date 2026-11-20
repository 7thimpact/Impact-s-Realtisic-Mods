package com.impact.realisticcrafting.item;

import com.impact.lib.wood.WoodData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** A loose item that keeps its wood (the loose boat): named for it ("Oak Loose Boat") and hammered into that wood's item. */
public final class LooseWoodItem extends LooseItem {
    private static final String WOOD_NAME_SUFFIX = ".wood";

    public LooseWoodItem() {
        super(HammerKnock.WOOD);
    }

    public String woodNameKey() {
        return getDescriptionId() + WOOD_NAME_SUFFIX;
    }

    @Override
    public Component getName(ItemStack stack) {
        return WoodData.get(stack)
                .map(wood -> (Component) Component.translatable(woodNameKey(), WoodData.displayName(wood)))
                .orElseGet(() -> super.getName(stack));
    }
}
