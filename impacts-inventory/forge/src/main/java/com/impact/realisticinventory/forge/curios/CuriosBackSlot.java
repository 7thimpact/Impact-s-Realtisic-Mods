package com.impact.realisticinventory.forge.curios;

import com.impact.realisticinventory.pack.BackSlotBackend;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

/**
 * Inventory's back slot is the first slot of Curios's {@code back} slot type, which Inventory's data gives every
 * player. Curios saves it, syncs it to every client, drops it on death (keeping keepInventory and its own rules) and
 * shows it in its own screen.
 */
public final class CuriosBackSlot implements BackSlotBackend {
    public static final String SLOT_TYPE = "back";
    private static final int FIRST_SLOT = 0;

    @Override
    public boolean hasBackSlot(Player player) {
        return backStacksOf(player).isPresent();
    }

    @Override
    public ItemStack get(Player player) {
        return backStacksOf(player).map(stacks -> stacks.getStackInSlot(FIRST_SLOT)).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean set(Player player, ItemStack stack) {
        Optional<IDynamicStackHandler> backStacks = backStacksOf(player);
        backStacks.ifPresent(stacks -> stacks.setStackInSlot(FIRST_SLOT, stack));
        return backStacks.isPresent();
    }

    private static Optional<IDynamicStackHandler> backStacksOf(Player player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.getStacksHandler(SLOT_TYPE))
                .map(ICurioStacksHandler::getStacks)
                .filter(stacks -> stacks.getSlots() > FIRST_SLOT);
    }
}
