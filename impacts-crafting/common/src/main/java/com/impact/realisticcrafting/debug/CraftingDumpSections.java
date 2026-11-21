package com.impact.realisticcrafting.debug;

import com.impact.lib.dump.ImpactsDump;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.DyeVatBlockEntity;
import com.impact.realisticcrafting.block.MakeshiftTableBlock;
import com.impact.realisticcrafting.block.MakeshiftTableBlockEntity;
import com.impact.realisticcrafting.recipe.TierResolver;
import com.impact.realisticcrafting.timing.CraftDepthTable;
import com.impact.realisticcrafting.timing.CraftTimes;

import net.minecraft.world.item.DyeColor;

import java.util.Map;
import java.util.OptionalInt;

/** Crafting's parts of {@code /impacts dump held} (the item's tier, depth and craft time) and {@code dump block} (a makeshift table's wear, a Dye Vat's contents). */
public final class CraftingDumpSections {
    private static final String SECTION_NAME = "crafting";
    private static final String UNPLACED = "unplaced";

    private CraftingDumpSections() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ImpactsDump.registerItemSection(SECTION_NAME, (stack, player) -> {
            OptionalInt depth = CraftDepthTable.depthOf(stack.getItem());
            return ImpactsDump.entries("tier", TierResolver.tierOf(stack).serializedName,
                    "depth", depth.isPresent() ? depth.getAsInt() : UNPLACED,
                    "craftTicks", CraftTimes.ticksForDepth(depth).stream().boxed().findFirst().map(String::valueOf).orElse(UNPLACED));
        });
        ImpactsDump.registerBlockSection(SECTION_NAME, (level, position, state) -> {
            if (level.getBlockEntity(position) instanceof DyeVatBlockEntity vat) {
                return ImpactsDump.entries("water", vat.hasWater(), "color", vat.color().map(DyeColor::getName).orElse("none"),
                        "charges", vat.charges());
            }
            if (!(level.getBlockEntity(position) instanceof MakeshiftTableBlockEntity table)) {
                return Map.of();
            }
            return ImpactsDump.entries("table", table.table().path, "usesLeft", table.usesLeft() + "/" + table.table().maxUses(),
                    "wear", state.getValue(MakeshiftTableBlock.WEAR));
        });
    }
}
