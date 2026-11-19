package com.impact.realisticinventory.forge;

import com.impact.realisticinventory.hotbar.HandSwitch;
import com.impact.realisticinventory.hotbar.HotbarRespawn;
import com.impact.realisticinventory.hotbar.SwitchSource;
import com.impact.realisticinventory.inventory.InventoryLocks;
import com.impact.realisticinventory.inventory.LockedSlotOverflow;
import com.impact.realisticinventory.pack.PackEffects;
import com.impact.realisticinventory.pack.PlacedPacks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingSwapItemsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Connects Inventory's game logic to Forge's game events. */
final class InventoryForgeEvents {
    private InventoryForgeEvents() {
    }

    static void register() {
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeEvents::onSwapHands);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeEvents::onLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeEvents::onRespawn);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeEvents::onChangedDimension);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeEvents::onClone);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeEvents::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(InventoryForgeEvents::onRightClickBlock);
    }

    /** The swap-hands key (F) switches slots, the same as scrolling. */
    private static void onSwapHands(LivingSwapItemsEvent.Hands event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !InventoryLocks.isActiveFor(player)) {
            return;
        }
        event.setCanceled(true);
        HandSwitch.switchToOtherSlot(player, SwitchSource.SWAP_HANDS_KEY);
    }

    private static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HotbarRespawn.syncToClient(player);
        }
    }

    private static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HotbarRespawn.syncToClient(player);
        }
    }

    private static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HotbarRespawn.syncToClient(player);
        }
    }

    private static void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HotbarRespawn.afterRespawn(player, event.isWasDeath());
        }
    }

    /**
     * Sneak-right-click on a placed pack with an empty main hand picks it up. Vanilla skips blocks while sneaking with
     * anything in either hand, so this runs before it.
     */
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        boolean isMainHand = event.getHand() == InteractionHand.MAIN_HAND;
        if (isMainHand && PlacedPacks.pickUp(event.getEntity(), event.getPos())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        }
    }

    private static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            LockedSlotOverflow.tick(player);
            PackEffects.tick(player);
        }
    }
}
