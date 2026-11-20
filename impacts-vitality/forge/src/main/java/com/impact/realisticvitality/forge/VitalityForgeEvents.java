package com.impact.realisticvitality.forge;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.network.VitalityNetwork;
import com.impact.realisticvitality.player.VitalityPlayerTicks;
import com.impact.realisticvitality.player.VitalitySync;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.thirst.BottleFilling;
import com.impact.realisticvitality.thirst.Drinking;
import com.impact.realisticvitality.thirst.Sipping;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;

/** Vitality's game events on Forge's event bus. */
final class VitalityForgeEvents {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);

    private VitalityForgeEvents() {
    }

    static void register() {
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onItemUseFinished);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onItemSmelted);
        // Early, so no other mod hands out a vanilla water bottle first; other items pass straight through.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, VitalityForgeEvents::onRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onRightClickEmpty);
    }

    private static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            VitalityPlayerTicks.tick(player);
        }
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            VitalitySync.onLogin(player);
        }
    }

    private static void onItemUseFinished(LivingEntityUseItemEvent.Finish event) {
        Drinking.onUseFinished(event.getEntity(), event.getItem());
    }

    private static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        boolean isPurification = VitalityItems.bottledWater(event.getSmelting()).isPresent();
        if (isPurification) {
            LOG.debug(VitalityLogCategories.PURITY, "smelting conversion", "player", event.getEntity().getScoreboardName(),
                    "result", ResourceIds.itemId(event.getSmelting().getItem()), "count", event.getSmelting().getCount());
        }
    }

    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        InteractionResult result = BottleFilling.fillFromWorld(event.getLevel(), event.getEntity(), event.getHand());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    // Sneak-using water with an empty hand: when there's a block behind the water, the click arrives here on both sides.
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !Sipping.canSip(event.getEntity())) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            Sipping.trySip(player);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    // Looking at open water with nothing behind it in reach: only the client sees this click, so it asks the server.
    private static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (event.getHand() == InteractionHand.MAIN_HAND && Sipping.canSip(event.getEntity())) {
            VitalityNetwork.sendSip();
        }
    }
}
