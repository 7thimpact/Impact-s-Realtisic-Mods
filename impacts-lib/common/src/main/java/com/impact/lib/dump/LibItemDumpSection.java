package com.impact.lib.dump;

import com.impact.lib.ResourceIds;
import com.impact.lib.item.UsesDurability;
import com.impact.lib.item.UsesTrackedItem;
import com.impact.lib.wood.WoodData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/** Lib's part of the held-item dump: the item's ID, count, NBT, tags, wood type and uses. */
public final class LibItemDumpSection implements ItemDumpSection {
    public static final String SECTION_NAME = "lib";
    private static final String NONE = "none";

    @Override
    public Map<String, Object> describe(ItemStack stack, ServerPlayer player) {
        Map<String, Object> entries = ImpactsDump.entries(
                "item", ResourceIds.itemId(stack.getItem()),
                "count", stack.getCount() + "/" + stack.getMaxStackSize(),
                "nbt", stack.hasTag() ? stack.getTag() : NONE,
                "tags", stack.getTags().map(tag -> "#" + tag.location()).toList(),
                "wood", WoodData.get(stack).map(Object::toString).orElse(NONE));
        if (stack.getItem() instanceof UsesTrackedItem tracked) {
            entries.put("uses", UsesDurability.get(stack) + "/" + tracked.maxUses(stack) + " left");
        } else if (stack.isDamageableItem()) {
            entries.put("durability", (stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage() + " left");
        }
        return entries;
    }
}
