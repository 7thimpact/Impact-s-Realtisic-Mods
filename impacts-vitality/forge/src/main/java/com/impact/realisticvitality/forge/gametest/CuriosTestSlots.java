package com.impact.realisticvitality.forge.gametest;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Optional;

/**
 * For tests run with Curios installed: puts an item in the player's Curios head slot, reads what's in a slot type, and
 * reads the sound Curios plays when an item is equipped. Touched only when Curios is loaded.
 */
final class CuriosTestSlots {
    private static final String HEAD_SLOT_TYPE = "head";
    private static final int FIRST_SLOT = 0;
    private static final boolean IS_COSMETIC = false;
    private static final boolean IS_VISIBLE = true;

    private CuriosTestSlots() {
    }

    /** Returns whether the player had a head slot to put it in. */
    static boolean putOnHead(Player player, ItemStack stack) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.getStacksHandler(HEAD_SLOT_TYPE))
                .map(ICurioStacksHandler::getStacks)
                .filter(stacks -> stacks.getSlots() > FIRST_SLOT)
                .map(stacks -> {
                    stacks.setStackInSlot(FIRST_SLOT, stack);
                    return true;
                })
                .orElse(false);
    }

    /** The first item in a Curios slot type, or nothing when the player doesn't have that slot. */
    static Optional<ItemStack> firstIn(Player player, String slotType) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.getStacksHandler(slotType))
                .map(ICurioStacksHandler::getStacks)
                .filter(stacks -> stacks.getSlots() > FIRST_SLOT)
                .map(stacks -> stacks.getStackInSlot(FIRST_SLOT));
    }

    /** The sound Curios plays when the item is equipped in the given slot type, if Curios knows the item as a curio. */
    static Optional<SoundEvent> equipSound(Player player, String slotType, ItemStack stack) {
        SlotContext slot = new SlotContext(slotType, player, FIRST_SLOT, IS_COSMETIC, IS_VISIBLE);
        return CuriosApi.getCurio(stack).resolve().map(curio -> curio.getEquipSound(slot).soundEvent());
    }
}
