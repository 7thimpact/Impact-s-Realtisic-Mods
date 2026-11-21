package com.impact.realisticinventory.pack;

import com.impact.realisticinventory.config.InventoryServerConfig;
import com.impact.realisticinventory.lang.InventoryTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** A pack's tooltip: how full it is, its first few stacks (like a shulker box), the frame pack's slowdown, and how to use it. */
final class PackTooltip {
    private static final int LISTED_STACKS = 5;
    private static final double PERCENT = 100.0;

    private PackTooltip() {
    }

    static void append(ItemStack pack, PackKind kind, List<Component> lines) {
        List<ItemStack> contents = PackContents.stacksIn(pack);
        lines.add(Component.translatable(InventoryTranslations.SLOTS_USED_TOOLTIP, contents.size(), kind.slotCount()).withStyle(ChatFormatting.GRAY));
        contents.stream().limit(LISTED_STACKS).forEach(stack ->
                lines.add(Component.translatable(InventoryTranslations.STACK_TOOLTIP, stack.getHoverName(), stack.getCount()).withStyle(ChatFormatting.GRAY)));
        int unlistedStacks = contents.size() - LISTED_STACKS;
        if (unlistedStacks > 0) {
            lines.add(Component.translatable(InventoryTranslations.MORE_STACKS_TOOLTIP, unlistedStacks).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        double slowness = InventoryServerConfig.get().packs.framePackSlowness.get();
        if (kind == PackKind.FRAME_PACK && slowness > 0) {
            long slownessPercent = Math.round(slowness * PERCENT);
            lines.add(Component.translatable(InventoryTranslations.FRAME_PACK_SLOWNESS_TOOLTIP, slownessPercent).withStyle(ChatFormatting.BLUE));
        }
        lines.add(Component.translatable(InventoryTranslations.PACK_HINT_TOOLTIP).withStyle(ChatFormatting.DARK_GRAY));
    }
}
