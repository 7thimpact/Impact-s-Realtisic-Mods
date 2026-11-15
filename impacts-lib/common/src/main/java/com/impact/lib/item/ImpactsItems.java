package com.impact.lib.item;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Item handling every Impact mod shares. */
public final class ImpactsItems {
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private ImpactsItems() {
    }

    /**
     * Gives the stack to the player through vanilla's inventory, so Impact's Realistic Inventory's locked slots
     * apply, and drops whatever doesn't fit at the player's feet. Never deletes an item. Server side only.
     */
    public static void giveOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty() || player.level().isClientSide()) {
            return;
        }
        String itemId = ResourceIds.itemId(stack.getItem()).toString();
        int offeredCount = stack.getCount();
        ItemStack remainder = stack.copy();
        player.getInventory().add(remainder);
        int givenCount = offeredCount - remainder.getCount();
        if (givenCount > 0) {
            LOG.debug(LibLogCategories.GIVE_OR_DROP, "given", "player", player.getGameProfile().getName(), "item", itemId, "count", givenCount);
        }
        if (remainder.isEmpty()) {
            return;
        }
        dropAtFeet(player, remainder);
        LOG.debug(LibLogCategories.GIVE_OR_DROP, "dropped", "player", player.getGameProfile().getName(), "item", itemId,
                "count", remainder.getCount(), "pos", player.blockPosition().toShortString());
    }

    private static void dropAtFeet(Player player, ItemStack stack) {
        ItemEntity droppedItem = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack, 0, 0, 0);
        droppedItem.setDefaultPickUpDelay();
        player.level().addFreshEntity(droppedItem);
    }
}
