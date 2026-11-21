package com.impact.realisticcrafting.item;

import com.impact.lib.item.ConfiguredDurabilityItem;
import net.minecraft.world.item.Item;

/** A grid tool with vanilla durability from the server config: the Bone Needle, the chisels and, later, the iron tools. */
public final class CraftingToolItem extends Item implements ConfiguredDurabilityItem {
    // Any durability above 0 makes vanilla treat the item as damageable; Lib's mixin answers with the configured one.
    private static final int REGISTERED_DURABILITY = 1;

    private final CraftingTool tool;

    public CraftingToolItem(CraftingTool tool) {
        super(new Item.Properties().durability(REGISTERED_DURABILITY));
        this.tool = tool;
    }

    public CraftingTool tool() {
        return tool;
    }

    @Override
    public int configuredMaxDamage() {
        return tool.maxUses();
    }
}
