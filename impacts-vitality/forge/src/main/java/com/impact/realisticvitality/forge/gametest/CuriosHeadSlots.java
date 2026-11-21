package com.impact.realisticvitality.forge.gametest;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/** For tests run with Curios installed: puts an item in the player's Curios head slot. Touched only when Curios is loaded. */
final class CuriosHeadSlots {
    private static final String HEAD_SLOT_TYPE = "head";
    private static final int FIRST_SLOT = 0;

    private CuriosHeadSlots() {
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
}
