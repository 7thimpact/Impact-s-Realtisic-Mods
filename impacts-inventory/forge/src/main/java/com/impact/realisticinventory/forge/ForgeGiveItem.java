package com.impact.realisticinventory.forge;

import com.impact.lib.ResourceIds;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.inventory.CallerFinder;
import com.impact.realisticinventory.inventory.InventoryLayout;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Forge's {@code ItemHandlerHelper.giveItemToPlayer}, for players under Inventory's locks: the item goes to the hands,
 * pockets and worn pack through Lib's Give or drop (Forge's own version can't reach the pack). Forge's pickup sound is
 * kept. Server side only.
 */
public final class ForgeGiveItem {
    private static final float PICKUP_VOLUME = 0.2F;
    private static final float PICKUP_PITCH_SPREAD = 0.7F;
    private static final float PICKUP_PITCH_BASE = 2.0F;
    private static final double SOUND_HEIGHT_BLOCKS = 0.5;
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private ForgeGiveItem() {
    }

    public static void give(Player player, ItemStack stack, int preferredSlot) {
        Inventory inventory = player.getInventory();
        ItemStack remainder = stack.copy();
        boolean isPreferredSlotOpen = preferredSlot >= 0 && preferredSlot < inventory.items.size() && !InventoryLayout.isLocked(preferredSlot);
        if (isPreferredSlotOpen) {
            inventory.add(preferredSlot, remainder);
        }
        inventory.add(remainder);
        if (remainder.getCount() < stack.getCount()) {
            playPickupSound(player);
        }
        LOG.debug(InventoryLogCategories.LOCKS, "giveItemToPlayer routed through give or drop", "player", player.getGameProfile().getName(),
                "item", ResourceIds.itemId(stack.getItem()), "count", stack.getCount(), "left", remainder.getCount(),
                "caller", CallerFinder.outsideCaller());
        ImpactsItems.giveOrDrop(player, remainder);
    }

    private static void playPickupSound(Player player) {
        Level level = player.level();
        float pitch = ((level.random.nextFloat() - level.random.nextFloat()) * PICKUP_PITCH_SPREAD + 1.0F) * PICKUP_PITCH_BASE;
        level.playSound(null, player.getX(), player.getY() + SOUND_HEIGHT_BLOCKS, player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                PICKUP_VOLUME, pitch);
    }
}
