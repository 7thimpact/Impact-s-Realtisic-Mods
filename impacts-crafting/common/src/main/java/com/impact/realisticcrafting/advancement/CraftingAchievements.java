package com.impact.realisticcrafting.advancement;

import com.impact.lib.advancement.EventTrigger;
import com.impact.lib.counter.ImpactsCounters;
import com.impact.lib.crafting.CompletedToolCraft;
import com.impact.lib.crafting.ToolCrafting;
import com.impact.lib.grinding.CompletedGrind;
import com.impact.lib.grinding.Grinding;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.bed.BedEvents;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Arrays;

/** The checks behind Crafting's achievements that happen in the world: tables, timers, grinds, dips, beds and the two counters. */
public final class CraftingAchievements {
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);
    private static final int MIXED_DYE_COUNT = 2;

    private CraftingAchievements() {
    }

    /** Listens to Lib's tool crafts and grinds and to finished beds. Called once, at mod construction. */
    public static void register() {
        ToolCrafting.onCrafted(CraftingAchievements::onToolCrafted);
        Grinding.onGround(CraftingAchievements::onGround);
        BedEvents.onBedAssembled(CraftingAchievements::onBedAssembled);
    }

    /** A makeshift table fell apart under this player's craft ("Falling Apart"). */
    public static void onTableBroken(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            fire(serverPlayer, CraftingTriggers.TABLE_BROKEN, "table broken");
        }
    }

    /** A timed craft ran to the end ("Patience Is a Virtue" for long ones). */
    public static void onCraftFinished(Player player, int craftTicks) {
        if (player instanceof ServerPlayer serverPlayer && craftTicks >= CraftingServerConfig.get().achievements.longCraftTicks.get()) {
            fire(serverPlayer, CraftingTriggers.LONG_CRAFT, "long craft");
        }
    }

    /** An item came out of a dyed vat ("Dip It"). */
    public static void onVatDyed(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            fire(serverPlayer, CraftingTriggers.VAT_DYED, "vat dyed");
        }
    }

    /** A crafting result was taken: crafts from Crafting's own recipes count toward "Master Craftsman". */
    public static void onCraftTaken(Player player, Recipe<?> recipe, ItemStack result, int craftedCount) {
        if (!(player instanceof ServerPlayer serverPlayer) || !recipe.getId().getNamespace().equals(CraftingMod.MOD_ID) || craftedCount <= 0) {
            return;
        }
        int crafted = ImpactsCounters.add(serverPlayer, CraftingTriggers.ITEMS_CRAFTED_COUNTER, result.getItem(), craftedCount);
        if (crafted >= CraftingServerConfig.get().achievements.masterCraftsmanCrafts.get()) {
            CraftingTriggers.MASTER_CRAFTSMAN.trigger(serverPlayer);
        }
    }

    private static void onToolCrafted(CompletedToolCraft craft) {
        int scraps = craft.byproducts().stream().filter(byproduct -> byproduct.is(CraftingItems.part(CraftingPart.WOOD_SCRAPS)))
                .mapToInt(ItemStack::getCount).sum();
        if (scraps == 0) {
            return;
        }
        int collected = ImpactsCounters.add(craft.player(), CraftingTriggers.WOOD_SCRAPS_COUNTER, null, scraps);
        if (collected >= CraftingServerConfig.get().achievements.scrapCollectorScraps.get()) {
            CraftingTriggers.SCRAPS_COLLECTED.trigger(craft.player());
        }
    }

    private static void onGround(CompletedGrind grind) {
        if (!(grind.result().getItem() instanceof DyeItem)) {
            return;
        }
        fire(grind.player(), CraftingTriggers.DYE_GROUND, "dye ground");
        boolean isMix = grind.recipe().getIngredients().size() >= MIXED_DYE_COUNT
                && grind.recipe().getIngredients().stream().allMatch(CraftingAchievements::acceptsOnlyDyes);
        if (isMix) {
            fire(grind.player(), CraftingTriggers.DYES_MIXED, "dyes mixed");
        }
    }

    private static void onBedAssembled(Player player, DyeColor color) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        CraftingTriggers.BED_ASSEMBLED.trigger(serverPlayer, color);
        LOG.debug(CraftingLogCategories.ACHIEVEMENTS, "bed assembled", "player", serverPlayer.getScoreboardName(), "color", color.getName());
    }

    private static boolean acceptsOnlyDyes(Ingredient ingredient) {
        ItemStack[] accepted = ingredient.getItems();
        return accepted.length > 0 && Arrays.stream(accepted).allMatch(stack -> stack.getItem() instanceof DyeItem);
    }

    private static void fire(ServerPlayer player, EventTrigger trigger, String event) {
        trigger.trigger(player);
        LOG.debug(CraftingLogCategories.ACHIEVEMENTS, event, "player", player.getScoreboardName(), "trigger", trigger.getId());
    }
}
