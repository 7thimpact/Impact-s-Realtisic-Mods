package com.impact.realisticinventory.pack;

import com.impact.realisticinventory.config.InventoryServerConfig;
import com.impact.realisticinventory.lang.InventoryTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A pack's tooltip: how full it is, its first few stacks (like a shulker box), the Frame Pack's slowdown, the Netherite
 * Pack's fireproofing, and how to use it (on the back or the chest). An Ender Pack says what it opens instead.
 */
final class PackTooltip {
    private static final int LISTED_STACKS = 5;
    private static final double PERCENT = 100.0;

    private PackTooltip() {
    }

    static void append(ItemStack pack, PackKind kind, List<Component> lines) {
        if (kind.opensEnderChest()) {
            lines.add(Component.translatable(InventoryTranslations.ENDER_PACK_TOOLTIP).withStyle(ChatFormatting.DARK_PURPLE));
            appendHints(lines);
            return;
        }
        List<ItemStack> contents = PackContents.stacksIn(pack);
        lines.add(Component.translatable(InventoryTranslations.SLOTS_USED_TOOLTIP, contents.size(), kind.slotCount()).withStyle(ChatFormatting.GRAY));
        contents.stream().limit(LISTED_STACKS).forEach(stack ->
                lines.add(Component.translatable(InventoryTranslations.STACK_TOOLTIP, stack.getHoverName(), stack.getCount()).withStyle(ChatFormatting.GRAY)));
        int unlistedStacks = contents.size() - LISTED_STACKS;
        if (unlistedStacks > 0) {
            lines.add(Component.translatable(InventoryTranslations.MORE_STACKS_TOOLTIP, unlistedStacks).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        double slowness = InventoryServerConfig.get().packs.framePackSlowness.get();
        if (kind.isFireproof()) {
            lines.add(Component.translatable(InventoryTranslations.FIREPROOF_TOOLTIP).withStyle(ChatFormatting.BLUE));
        }
        if (kind.slowsWalking() && slowness > 0) {
            long slownessPercent = Math.round(slowness * PERCENT);
            lines.add(Component.translatable(InventoryTranslations.FRAME_PACK_SLOWNESS_TOOLTIP, slownessPercent).withStyle(ChatFormatting.BLUE));
        }
        appendHints(lines);
    }

    private static void appendHints(List<Component> lines) {
        lines.add(Component.translatable(InventoryTranslations.PACK_HINT_TOOLTIP).withStyle(ChatFormatting.DARK_GRAY));
        if (WornSlots.isEnabled(WornOn.CHEST)) {
            lines.add(Component.translatable(InventoryTranslations.CHEST_PACK_HINT_TOOLTIP).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
