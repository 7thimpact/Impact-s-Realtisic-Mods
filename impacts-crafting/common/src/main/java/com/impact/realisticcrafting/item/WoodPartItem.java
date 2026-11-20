package com.impact.realisticcrafting.item;

import com.impact.lib.wood.WoodData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.IntSupplier;

/**
 * A part that keeps its wood type ({@code wood} NBT through Lib's WoodData): Trimmed Planks now, and later the Wooden
 * Panel, Wooden Frame, Fence Post and Boat Hull. Its name carries the wood ("Oak Trimmed Planks"); its sprite is tinted
 * by the wood; it burns unless the wood is crimson or warped.
 */
public final class WoodPartItem extends Item {
    private static final String WOOD_NAME_SUFFIX = ".wood";

    private final IntSupplier burnTicks;

    public WoodPartItem(IntSupplier burnTicks) {
        super(new Item.Properties());
        this.burnTicks = burnTicks;
    }

    /** The translation key of the name with the wood in it ({@code %s Trimmed Planks}). */
    public String woodNameKey() {
        return getDescriptionId() + WOOD_NAME_SUFFIX;
    }

    @Override
    public Component getName(ItemStack stack) {
        return WoodData.get(stack)
                .map(wood -> (Component) Component.translatable(woodNameKey(), WoodData.displayName(wood)))
                .orElseGet(() -> super.getName(stack));
    }

    public int burnTicks(ItemStack stack) {
        Optional<Boolean> isFlammable = WoodData.get(stack).map(WoodData::isFlammable);
        return isFlammable.orElse(true) ? burnTicks.getAsInt() : 0;
    }
}
