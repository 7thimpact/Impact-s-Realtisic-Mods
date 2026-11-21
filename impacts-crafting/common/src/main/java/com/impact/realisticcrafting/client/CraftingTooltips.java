package com.impact.realisticcrafting.client;

import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingClientConfig;
import com.impact.realisticcrafting.item.LooseItem;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.lang.CraftingHints;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/** Adds each Crafting item's next step and its parent part to its tooltip, behind the client config's {@code nextStepHints}. */
public final class CraftingTooltips {
    private static final String WOODEN_TOOL_HEAD = "wooden_tool_head";
    private static final String STONE_TOOL_HEAD = "stone_tool_head";
    private static final String SHEET = "sheet";
    private static final String BANNER_CLOTH = "banner_cloth";

    private CraftingTooltips() {
    }

    public static void appendHints(ItemStack stack, List<Component> tooltip) {
        if (stack.isEmpty() || !CraftingClientConfig.get().areNextStepHintsOn.get()) {
            return;
        }
        hintNameOf(stack.getItem()).ifPresent(hintName -> {
            tooltip.add(Component.translatable(CraftingHints.NEXT_KEY, Component.translatable(CraftingHints.nextKey(hintName))).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(CraftingHints.MADE_FROM_KEY, Component.translatable(CraftingHints.madeFromKey(hintName)))
                    .withStyle(ChatFormatting.DARK_GRAY));
        });
    }

    /** The item's hint, if it has one. Loose items explain hammering themselves, and other mods' items get none. */
    public static Optional<String> hintNameOf(Item item) {
        ResourceLocation itemId = ResourceIds.itemId(item);
        if (!itemId.getNamespace().equals(CraftingMod.MOD_ID) || item instanceof LooseItem) {
            return Optional.empty();
        }
        String hintName = familyOf(item).orElse(itemId.getPath());
        return CraftingHints.has(hintName) ? Optional.of(hintName) : Optional.empty();
    }

    private static Optional<String> familyOf(Item item) {
        if (CraftingItems.sheets().contains(item)) {
            return Optional.of(SHEET);
        }
        if (CraftingItems.bannerCloths().contains(item)) {
            return Optional.of(BANNER_CLOTH);
        }
        for (ToolKind kind : ToolKind.values()) {
            if (CraftingItems.toolPart(ToolMaterial.WOODEN, kind) == item) {
                return Optional.of(WOODEN_TOOL_HEAD);
            }
            if (CraftingItems.toolPart(ToolMaterial.STONE, kind) == item) {
                return Optional.of(STONE_TOOL_HEAD);
            }
        }
        return Optional.empty();
    }
}
