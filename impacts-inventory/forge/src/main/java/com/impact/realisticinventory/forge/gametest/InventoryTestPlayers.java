package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.gametest.MockPlayers;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

/** Mock players for Inventory's GameTests. The GameTest world defaults to creative, which keeps vanilla's inventory. */
final class InventoryTestPlayers {
    private InventoryTestPlayers() {
    }

    static ServerPlayer joinInSurvival(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }
}
