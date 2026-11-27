package com.impact.realisticinventory.advancement;

import com.impact.realisticinventory.hotbar.HandSwitch;
import com.impact.realisticinventory.hotbar.HotbarState;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.inventory.InventoryLocks;
import com.impact.realisticinventory.pack.WornPack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

import java.util.stream.IntStream;

/** The checks behind Inventory's achievements that no single game event covers. Server side. */
public final class AchievementChecks {
    /** "Quick Draw": a hit counts within this many ticks (1 second) of switching slots. */
    public static final long QUICK_DRAW_WINDOW_TICKS = 20;

    private AchievementChecks() {
    }

    /** "Quick Draw": a player hurt a mob; it counts when they switched slots a moment ago. */
    public static void onPlayerHurtEntity(ServerPlayer player, LivingEntity target) {
        HotbarState state = HandSwitch.stateOf(player);
        boolean isSoonAfterSwitch = state.hasEverSwitched() && player.level().getGameTime() - state.lastSwitchGameTime() <= QUICK_DRAW_WINDOW_TICKS;
        if (target instanceof Mob && isSoonAfterSwitch) {
            InventoryTriggers.QUICK_DRAW.trigger(player);
        }
    }

    /** "Pocket Space": checked once a tick; vanilla's trigger can't tell the pockets from other slots. */
    public static void checkPockets(ServerPlayer player) {
        if (InventoryLocks.isActiveFor(player) && countFilledPockets(player.getInventory()) == InventoryLayout.POCKET_COUNT) {
            InventoryTriggers.POCKETS_FILLED.trigger(player);
        }
    }

    /** "Travel Light": arriving in the Nether with empty pockets and no pack. */
    public static void onChangedDimension(ServerPlayer player, ResourceKey<Level> destination) {
        boolean isTravelingLight = destination == Level.NETHER && InventoryLocks.isActiveFor(player)
                && countFilledPockets(player.getInventory()) == 0 && WornPack.worn(player).isEmpty();
        if (isTravelingLight) {
            InventoryTriggers.TRAVEL_LIGHT.trigger(player);
        }
    }

    private static long countFilledPockets(Inventory inventory) {
        return IntStream.rangeClosed(InventoryLayout.FIRST_POCKET_INDEX, InventoryLayout.LAST_POCKET_INDEX)
                .filter(pocketIndex -> !inventory.getItem(pocketIndex).isEmpty())
                .count();
    }
}
