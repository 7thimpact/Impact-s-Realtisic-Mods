package com.impact.realisticinventory.pack;

import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.InventoryMod;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The back slot in the player's inventory menu. It is appended to every player's menu, on both sides, even with the
 * back slot switched off (it then hides and refuses items), so client and server menus always have the same size.
 */
public final class BackMenuSlot extends Slot {
    /** Where the slot sits in the inventory screen: vanilla's off-hand frame, beside the player model. */
    public static final int SCREEN_X = 77;
    public static final int SCREEN_Y = 62;
    private static final int ONLY_INDEX = 0;
    private static final int MAXIMUM_STACK_SIZE = 1;
    private static final ResourceLocation EMPTY_SLOT_ICON = ResourceIds.of(InventoryMod.MOD_ID, "item/empty_back_slot");

    public BackMenuSlot(Player wearer) {
        super(new BackSlotContainer(wearer), ONLY_INDEX, SCREEN_X, SCREEN_Y);
    }

    @Override
    public boolean isActive() {
        return BackSlotStorage.isEnabled();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return BackSlotStorage.accepts(stack);
    }

    @Override
    public int getMaxStackSize() {
        return MAXIMUM_STACK_SIZE;
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_SLOT_ICON);
    }
}
