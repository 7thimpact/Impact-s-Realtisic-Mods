package com.impact.realisticinventory.forge.gametest;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Finds items dropped at a player's feet during a test. GameTests share one world, so a test counts only the item
 * entities that appeared after it took its "before" snapshot.
 */
final class DroppedItems {
    private static final double SEARCH_RADIUS_BLOCKS = 2.0;

    private DroppedItems() {
    }

    static Set<UUID> idsNear(ServerPlayer player) {
        return player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(SEARCH_RADIUS_BLOCKS)).stream()
                .map(ItemEntity::getUUID)
                .collect(Collectors.toSet());
    }

    static int countNewNear(ServerPlayer player, Item expectedItem, Set<UUID> idsBefore) {
        return player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(SEARCH_RADIUS_BLOCKS)).stream()
                .filter(itemEntity -> !idsBefore.contains(itemEntity.getUUID()))
                .map(ItemEntity::getItem)
                .filter(dropped -> dropped.is(expectedItem))
                .mapToInt(ItemStack::getCount)
                .sum();
    }
}
