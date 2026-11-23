package com.impact.realisticinventory.pack;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;

/**
 * The Ender Pack, worn or set down, opens the Ender Chest of whoever opens it, exactly as an ender chest block would
 * (the same 27 slots, shared with every ender chest), instead of holding items of its own.
 */
public final class EnderPacks {
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);
    private static final Component ENDER_CHEST_TITLE = Component.translatable("container.enderchest");
    private static final float SOUND_VOLUME = 0.5F;

    private EnderPacks() {
    }

    /** Opens the player's Ender Chest; {@code at} is where the sound plays (their back, or the placed pack). */
    public static void openEnderChest(ServerPlayer player, BlockPos at, String source) {
        PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
        // No ender chest block is open for this, so nothing else decides whether the menu stays valid.
        enderChest.setActiveChest(null);
        player.openMenu(new SimpleMenuProvider((containerId, playerInventory, menuPlayer) -> ChestMenu.threeRows(containerId, playerInventory, enderChest),
                ENDER_CHEST_TITLE));
        player.awardStat(Stats.OPEN_ENDERCHEST);
        player.level().playSound(null, at, SoundEvents.ENDER_CHEST_OPEN, SoundSource.PLAYERS, SOUND_VOLUME, player.getRandom().nextFloat() * 0.1F + 0.9F);
        LOG.debug(InventoryLogCategories.PACKS, "ender pack opened", "player", player.getGameProfile().getName(), "source", source, "pos", at);
    }
}
