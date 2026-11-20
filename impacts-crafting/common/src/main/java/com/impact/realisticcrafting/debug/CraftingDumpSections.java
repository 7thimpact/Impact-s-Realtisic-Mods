package com.impact.realisticcrafting.debug;

import com.impact.lib.dump.ImpactsDump;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTableBlock;
import com.impact.realisticcrafting.block.MakeshiftTableBlockEntity;
import com.impact.realisticcrafting.recipe.TierResolver;

import java.util.Map;

/** Crafting's parts of {@code /impacts dump held} (the item's tier) and {@code dump block} (a makeshift table's wear). */
public final class CraftingDumpSections {
    private static final String SECTION_NAME = "crafting";

    private CraftingDumpSections() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ImpactsDump.registerItemSection(SECTION_NAME, (stack, player) -> ImpactsDump.entries("tier", TierResolver.tierOf(stack).serializedName));
        ImpactsDump.registerBlockSection(SECTION_NAME, (level, position, state) -> {
            if (!(level.getBlockEntity(position) instanceof MakeshiftTableBlockEntity table)) {
                return Map.of();
            }
            return ImpactsDump.entries("table", table.table().path, "usesLeft", table.usesLeft() + "/" + table.table().maxUses(),
                    "wear", state.getValue(MakeshiftTableBlock.WEAR));
        });
    }
}
