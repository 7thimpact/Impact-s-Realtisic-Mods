package com.impact.realisticvitality.forge;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.client.ClientVitality;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.health.HealthCap;
import com.impact.realisticvitality.network.VitalityNetwork;
import com.impact.realisticvitality.player.VitalityPlayerTicks;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.player.VitalitySync;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.temperature.ArmorLining;
import com.impact.realisticvitality.temperature.BlockTemperatures;
import com.impact.realisticvitality.temperature.BodyState;
import com.impact.realisticvitality.temperature.DryFoods;
import com.impact.realisticvitality.temperature.Linings;
import com.impact.realisticvitality.temperature.TemperatureTicks;
import com.impact.realisticvitality.thirst.BottleFilling;
import com.impact.realisticvitality.thirst.Drinking;
import com.impact.realisticvitality.thirst.Sipping;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
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
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onLivingHeal);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onTagsUpdated);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onItemUseStart);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onBreakSpeed);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onAnvilUpdate);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onAnvilRepair);
    }

    // Without Crafting, linings are sewn at an anvil. The armor keeps all its NBT; the player's new name, if any, applies too.
    private static void onAnvilUpdate(AnvilUpdateEvent event) {
        Linings.anvilResult(event.getLeft(), event.getRight()).ifPresent(lining -> {
            ItemStack output = lining.lined();
            String newName = event.getName();
            if (newName != null && !newName.isBlank() && !newName.equals(event.getLeft().getHoverName().getString())) {
                output.setHoverName(Component.literal(newName));
            }
            event.setOutput(output);
            event.setCost(lining.levels());
            event.setMaterialCost(lining.materialCount());
        });
    }

    private static void onAnvilRepair(AnvilRepairEvent event) {
        boolean isNewLining = ArmorLining.of(event.getOutput()).isPresent() && !ArmorLining.of(event.getOutput()).equals(ArmorLining.of(event.getLeft()));
        if (isNewLining) {
            Linings.logSewn(event.getEntity(), event.getOutput(), "anvil");
        }
    }

    // Both sides refuse, each with its own copy of the body temperature, so the client never starts the eating animation.
    private static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player) || !DryFoods.isTooHotToEat(bodyTemperature(player), event.getItem())) {
            return;
        }
        event.setCanceled(true);
        if (player instanceof ServerPlayer serverPlayer) {
            DryFoods.onRefused(serverPlayer, event.getItem());
        }
    }

    // Mining is predicted on the client, so both sides slow it the same way.
    private static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        boolean isCold = TemperatureTicks.isOn() && BodyState.of(bodyTemperature(event.getEntity())).isColdOrWorse();
        if (isCold) {
            event.setNewSpeed(event.getNewSpeed() * VitalityServerConfig.get().temperature.coldMiningSpeed.get().floatValue());
        }
    }

    private static float bodyTemperature(Player player) {
        return player.level().isClientSide ? ClientVitality.state().bodyTemperature() : VitalityState.of(player).temperature.body();
    }

    private static void onTagsUpdated(TagsUpdatedEvent event) {
        BlockTemperatures.clearCache();
    }

    private static void onLivingHeal(LivingHealEvent event) {
        float limited = HealthCap.limitHealing(event.getEntity(), event.getAmount());
        if (limited <= 0.0F) {
            event.setCanceled(true);
            return;
        }
        event.setAmount(limited);
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
