package com.impact.lib.registry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** Builds the client's copy of a menu from the extra data the server sent when it opened the menu. */
@FunctionalInterface
public interface MenuWithDataFactory<M extends AbstractContainerMenu> {
    M create(int containerId, Inventory playerInventory, FriendlyByteBuf extraData);
}
