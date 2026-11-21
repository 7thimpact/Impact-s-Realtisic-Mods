package com.impact.realisticcrafting.bed;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/** Listeners for finished beds (the achievements count sheet colors). */
public final class BedEvents {
    private static final List<BiConsumer<Player, DyeColor>> ASSEMBLED_LISTENERS = new CopyOnWriteArrayList<>();

    private BedEvents() {
    }

    public static void onBedAssembled(BiConsumer<Player, DyeColor> listener) {
        ASSEMBLED_LISTENERS.add(listener);
    }

    static void onBedAssembled(Player player, DyeColor color) {
        ASSEMBLED_LISTENERS.forEach(listener -> listener.accept(player, color));
    }
}
