package com.impact.realisticinventory.pack;

import com.impact.lib.ResourceIds;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Items left in slots a pack no longer has (its configured size shrank) go to the player through Give or drop. */
final class PackOverflow {
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private PackOverflow() {
    }

    static void handOut(Player player, List<ItemStack> itemsBeyondSize) {
        for (ItemStack stack : itemsBeyondSize) {
            LOG.debug(InventoryLogCategories.PACKS, "item beyond pack size handed out", "player", player.getGameProfile().getName(),
                    "item", ResourceIds.itemId(stack.getItem()), "count", stack.getCount());
            ImpactsItems.giveOrDrop(player, stack);
        }
    }
}
