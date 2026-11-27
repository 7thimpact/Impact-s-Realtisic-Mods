package com.impact.realisticinventory.forge.curios;

import com.impact.realisticinventory.pack.WornOn;
import com.impact.realisticinventory.pack.WornSlotBackend;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Arrays;
import java.util.Optional;

/**
 * Inventory's worn slots are the first slots of Curios's {@code back} slot type (the back) and {@code body} slot type
 * (the chest), which Inventory's data gives every player. Curios saves them, syncs them to every client, drops them on
 * death (keeping keepInventory and its own rules) and shows them in its own screen.
 */
public final class CuriosWornSlots implements WornSlotBackend {
    public static final String BACK_SLOT_TYPE = "back";
    public static final String CHEST_SLOT_TYPE = "body";
    private static final int FIRST_SLOT = 0;

    /** Curios's slot type for a worn place. */
    public static String slotTypeOf(WornOn place) {
        return place == WornOn.BACK ? BACK_SLOT_TYPE : CHEST_SLOT_TYPE;
    }

    /** The worn place a Curios slot type stands for, if it's one of Inventory's. */
    public static Optional<WornOn> placeOf(String slotType) {
        return Arrays.stream(WornOn.values()).filter(place -> slotTypeOf(place).equals(slotType)).findFirst();
    }

    @Override
    public boolean hasSlot(Player player, WornOn place) {
        return stacksOf(player, place).isPresent();
    }

    @Override
    public ItemStack get(Player player, WornOn place) {
        return stacksOf(player, place).map(stacks -> stacks.getStackInSlot(FIRST_SLOT)).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean set(Player player, WornOn place, ItemStack stack) {
        Optional<IDynamicStackHandler> stacks = stacksOf(player, place);
        stacks.ifPresent(handler -> handler.setStackInSlot(FIRST_SLOT, stack));
        return stacks.isPresent();
    }

    private static Optional<IDynamicStackHandler> stacksOf(Player player, WornOn place) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.getStacksHandler(slotTypeOf(place)))
                .map(ICurioStacksHandler::getStacks)
                .filter(stacks -> stacks.getSlots() > FIRST_SLOT);
    }
}
