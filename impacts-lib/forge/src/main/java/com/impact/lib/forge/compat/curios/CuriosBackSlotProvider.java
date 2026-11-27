package com.impact.lib.forge.compat.curios;

import com.impact.lib.backslot.BackSlotProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

/**
 * Lib's back slot when no Impact mod provides one (Inventory isn't installed) but Curios is: the first slot of Curios's
 * own {@code back} slot type. Curios saves it, syncs it, drops it on death and shows it in its screen. It's on while
 * players have that slot (Vitality's data gives it to them when Inventory isn't installed).
 */
final class CuriosBackSlotProvider implements BackSlotProvider {
    static final String BACK_SLOT_TYPE = "back";
    private static final int FIRST_SLOT = 0;

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

    /** Lib has no back-slot items of its own: everything comes from {@code #impactslib:back_slot_items}. */
    @Override
    public boolean acceptsOwnItem(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return CuriosApi.getPlayerSlots(EffectiveSide.get().isClient()).containsKey(BACK_SLOT_TYPE);
    }

    private static Optional<IDynamicStackHandler> backStacksOf(Player player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.getStacksHandler(BACK_SLOT_TYPE))
                .map(ICurioStacksHandler::getStacks)
                .filter(stacks -> stacks.getSlots() > FIRST_SLOT);
    }
}
