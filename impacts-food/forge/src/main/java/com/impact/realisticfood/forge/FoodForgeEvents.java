package com.impact.realisticfood.forge;

import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.advancement.FoodAchievements;
import com.impact.realisticfood.boiling.CauldronInteractions;
import com.impact.realisticfood.boiling.CauldronPots;
import com.impact.realisticfood.bucket.CampfireBuckets;
import com.impact.realisticfood.raw.RawFoodRisk;
import com.impact.realisticfood.stoneboiling.PebbleHeating;
import com.impact.realisticfood.stoneboiling.StoneBoilingPots;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;

/** Food's game events on Forge's event bus. */
final class FoodForgeEvents {
    private FoodForgeEvents() {
    }

    static void register() {
        MinecraftForge.EVENT_BUS.addListener(FoodForgeEvents::onLevelTick);
        MinecraftForge.EVENT_BUS.addListener(FoodForgeEvents::onItemUseFinished);
        MinecraftForge.EVENT_BUS.addListener(FoodForgeEvents::onItemSmelted);
        MinecraftForge.EVENT_BUS.addListener(FoodForgeEvents::onAdvancementEarned);
        MinecraftForge.EVENT_BUS.addListener(FoodForgeEvents::onPlayerLoggedOut);
        // Early, so no other mod fills a bottle from a soup or pours a bucket meant for a pot; other clicks pass.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, FoodForgeEvents::onRightClickBlock);
    }

    private static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            CauldronPots.tickAll(serverLevel);
        }
    }

    private static void onItemUseFinished(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RawFoodRisk.onEaten(player, event.getItem());
            FoodAchievements.onEaten(player, event.getItem());
        }
    }

    private static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FoodAchievements.onItemSmelted(player, event.getSmelting());
        }
    }

    private static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        boolean isFoodAdvancement = event.getAdvancement().getId().getNamespace().equals(FoodMod.MOD_ID);
        if (isFoodAdvancement && event.getEntity() instanceof ServerPlayer player) {
            FoodAchievements.checkWellFed(player);
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (CauldronInteractions.isBottleRefused(event.getLevel(), event.getPos(), event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }
        Direction face = event.getFace() == null ? Direction.UP : event.getFace();
        InteractionResult result = CampfireBuckets.tryPlace(event.getLevel(), event.getPos(), face, event.getEntity(), event.getHand());
        if (result == InteractionResult.PASS) {
            result = StoneBoilingPots.tryPlace(event.getLevel(), event.getPos(), face, event.getEntity(), event.getHand());
        }
        if (result == InteractionResult.PASS) {
            result = PebbleHeating.tryHeat(event.getLevel(), event.getPos(), event.getEntity(), event.getHand());
        }
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PebbleHeating.forget(event.getEntity());
    }
}
