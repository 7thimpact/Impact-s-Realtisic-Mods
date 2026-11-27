package com.impact.lib.backslot;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The player's back slot. Impact's Realistic Inventory provides it; without Inventory, Lib uses Curios's back slot when
 * Curios is installed. Other mods read and write it only through this class. Without a provider,
 * {@link #isAvailable()} is false, {@link #get} returns an empty stack and {@link #set} changes nothing.
 */
public final class BackSlot {
    /** Items that may go in the back slot besides the provider's own (for example Vitality's Air Tanks). */
    public static final TagKey<Item> BACK_SLOT_ITEMS = TagKey.create(Registries.ITEM, ResourceIds.of(ImpactsLib.MOD_ID, "back_slot_items"));

    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static volatile BackSlotProvider provider;
    private static volatile String providerModId;

    private BackSlot() {
    }

    /** Registers the mod that stores the back slot. Only the first provider is used. */
    public static void registerBackSlotProvider(String modId, BackSlotProvider newProvider) {
        if (provider != null) {
            LOG.warn(LibLogCategories.PROVIDERS, "second back slot provider ignored", "mod", modId, "kept", providerModId);
            return;
        }
        provider = newProvider;
        providerModId = modId;
        LOG.debug(LibLogCategories.PROVIDERS, "back slot provider registered", "mod", modId);
    }

    /** Whether a mod registered a back-slot provider (switched on or not). */
    public static boolean hasProvider() {
        return provider != null;
    }

    /** Whether a mod provides the back slot and has it switched on. */
    public static boolean isAvailable() {
        BackSlotProvider currentProvider = provider;
        return currentProvider != null && currentProvider.isEnabled();
    }

    public static ItemStack get(Player player) {
        BackSlotProvider currentProvider = provider;
        return currentProvider == null ? ItemStack.EMPTY : currentProvider.get(player);
    }

    /**
     * Puts the stack in the back slot. Returns false, changing nothing, when no mod provides a back slot, it is off, or
     * this player has none; the caller keeps the stack then.
     */
    public static boolean set(Player player, ItemStack stack) {
        return isAvailable() && provider.set(player, stack);
    }

    /** Whether the item may go in the back slot: the provider's own items, or anything in {@link #BACK_SLOT_ITEMS}. */
    public static boolean accepts(ItemStack stack) {
        return isAvailable() && (stack.is(BACK_SLOT_ITEMS) || provider.acceptsOwnItem(stack));
    }
}
