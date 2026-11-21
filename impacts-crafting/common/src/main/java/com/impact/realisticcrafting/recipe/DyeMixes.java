package com.impact.realisticcrafting.recipe;

import net.minecraft.world.item.DyeColor;

import java.util.Map;
import java.util.Optional;

/**
 * The dye mixes, shared by the grinder's dye mixing and the Dye Vat: red + yellow → orange, red + white → pink, blue +
 * white → light blue, blue + green → cyan, blue + red → purple, purple + pink → magenta, green + white → lime, black +
 * white → gray, gray + white → light gray. Mixing works either way round.
 */
public final class DyeMixes {
    private static final Map<DyeColor, Map<DyeColor, DyeColor>> MIXES = Map.of(
            DyeColor.RED, Map.of(DyeColor.YELLOW, DyeColor.ORANGE, DyeColor.WHITE, DyeColor.PINK, DyeColor.BLUE, DyeColor.PURPLE),
            DyeColor.BLUE, Map.of(DyeColor.WHITE, DyeColor.LIGHT_BLUE, DyeColor.GREEN, DyeColor.CYAN),
            DyeColor.PURPLE, Map.of(DyeColor.PINK, DyeColor.MAGENTA),
            DyeColor.GREEN, Map.of(DyeColor.WHITE, DyeColor.LIME),
            DyeColor.BLACK, Map.of(DyeColor.WHITE, DyeColor.GRAY),
            DyeColor.GRAY, Map.of(DyeColor.WHITE, DyeColor.LIGHT_GRAY));

    private DyeMixes() {
    }

    public static Optional<DyeColor> mix(DyeColor first, DyeColor second) {
        DyeColor oneWay = MIXES.getOrDefault(first, Map.of()).get(second);
        return Optional.ofNullable(oneWay != null ? oneWay : MIXES.getOrDefault(second, Map.of()).get(first));
    }
}
