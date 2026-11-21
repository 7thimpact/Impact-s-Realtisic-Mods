package com.impact.realisticinventory.inventory;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticinventory.config.InventoryServerConfig;
import net.minecraft.world.entity.player.Player;

/** Whether Inventory's two-slot hotbar and locked slots apply to a player right now. */
public final class InventoryLocks {
    private InventoryLocks() {
    }

    /**
     * Applies to people in survival and adventure. Spectators are untouched, creative keeps vanilla's inventory while
     * {@code creativeVanilla} is on, and machines' stand-in players (Forge's FakePlayer) keep vanilla's slots.
     */
    public static boolean isActiveFor(Player player) {
        if (player == null || player.isSpectator()) {
            return false;
        }
        boolean isCreativeVanilla = player.isCreative() && InventoryServerConfig.get().isCreativeVanilla.get();
        return !isCreativeVanilla && !LoaderPlatform.current().isFakePlayer(player);
    }
}
