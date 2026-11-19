package com.impact.realisticfood.cutting;

import com.impact.lib.ResourceIds;
import com.impact.lib.crafting.CompletedToolCraft;
import com.impact.lib.crafting.ToolCrafting;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.debug.FoodLogCategories;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Knife cutting: Food's config overrides for its tool-crafting recipes, and the {@code cutting} log line for each cut. */
public final class Cutting {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);

    private Cutting() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ToolCrafting.registerOverrides(FoodMod.MOD_ID, new CuttingOverrides());
        ToolCrafting.onCrafted(Cutting::logCut);
    }

    private static void logCut(CompletedToolCraft craft) {
        boolean isFoodCut = craft.recipe().getId().getNamespace().equals(FoodMod.MOD_ID) && !craft.toolsBefore().isEmpty();
        if (!isFoodCut || !LOG.isEnabled(FoodLogCategories.CUTTING)) {
            return;
        }
        ItemStack knifeBefore = craft.toolsBefore().get(0);
        ItemStack knifeAfter = craft.toolsAfter().get(0);
        LOG.debug(FoodLogCategories.CUTTING, "cut",
                "player", craft.player().getGameProfile().getName(),
                "input", CuttingOverrides.inputIdOf(craft.recipe()).orElse("unknown"),
                "output", ResourceIds.itemId(craft.result().getItem()),
                "count", craft.result().getCount(),
                "knife", ResourceIds.itemId(knifeBefore.getItem()),
                "usesLeft", usesLeft(knifeAfter),
                "byproducts", itemIds(craft.byproducts()));
    }

    private static int usesLeft(ItemStack knife) {
        return knife.isEmpty() ? 0 : knife.getMaxDamage() - knife.getDamageValue();
    }

    private static List<String> itemIds(List<ItemStack> stacks) {
        return stacks.stream().map(stack -> ResourceIds.itemId(stack.getItem()).toString()).toList();
    }
}
