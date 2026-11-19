package com.impact.realisticfood.advancement;

import com.impact.lib.ResourceIds;
import com.impact.lib.counter.ImpactsCounters;
import com.impact.lib.counter.PlayerCounters;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.registry.FoodTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

/** The checks behind Food's achievements that happen in the world: who saw a pot boil, what they ate, and "Well Fed". */
public final class FoodAchievements {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    /** How close a player must be to a pot to count as having seen it boil. */
    private static final double WITNESS_RANGE = 8.0;
    private static final int PROGRESS_LOG_STEP = 10;

    private FoodAchievements() {
    }

    /** A cauldron came to the boil: everyone nearby earns "Hot Water". */
    public static void onCauldronBoiling(ServerLevel level, BlockPos position) {
        playersNear(level, position).forEach(FoodTriggers.CAULDRON_BOILED::trigger);
    }

    /** Something finished boiling in a campfire bucket: everyone nearby earns "Camp Cook". */
    public static void onCampfireBucketBoiled(ServerLevel level, BlockPos position) {
        playersNear(level, position).forEach(FoodTriggers.CAMPFIRE_BUCKET_BOILED::trigger);
    }

    /** A player finished eating: styled food counts toward "Taste Tester", boiled and toasted food toward "Day in and Day out". */
    public static void onEaten(ServerPlayer player, ItemStack eaten) {
        if (!eaten.is(FoodTags.STYLED_FOODS)) {
            return;
        }
        FoodServerConfig.CounterSettings settings = FoodServerConfig.get().counters;
        ImpactsCounters.add(player, FoodTriggers.STYLED_FOOD_EATEN_COUNTER, eaten.getItem(), 1);
        PlayerCounters counters = ImpactsCounters.all(player);
        if (counters.distinctItemCount(FoodTriggers.STYLED_FOOD_EATEN_COUNTER) >= settings.tasteTesterFoods.get()) {
            FoodTriggers.TASTE_TESTER.trigger(player);
        }
        if (!eaten.is(FoodTags.BOILED_FOODS) && !eaten.is(FoodTags.TOASTED_FOODS)) {
            return;
        }
        ImpactsCounters.add(player, FoodTriggers.BOILED_OR_TOASTED_EATEN_COUNTER, eaten.getItem(), 1);
        int timesEaten = ImpactsCounters.get(player, FoodTriggers.BOILED_OR_TOASTED_EATEN_COUNTER, eaten.getItem());
        int goal = settings.dayInAndDayOutEats.get();
        if (timesEaten % PROGRESS_LOG_STEP == 0 || timesEaten == goal) {
            LOG.debug(FoodLogCategories.COUNTERS, timesEaten >= goal ? "day in and day out reached" : "day in and day out progress",
                    "player", player.getGameProfile().getName(), "food", ResourceIds.itemId(eaten.getItem()), "eaten", timesEaten, "goal", goal);
        }
        if (timesEaten >= goal) {
            FoodTriggers.DAY_IN_AND_DAY_OUT.trigger(player);
        }
    }

    /** A player turned a spit. */
    public static void onSpitTurned(ServerPlayer player) {
        int turns = ImpactsCounters.add(player, FoodTriggers.SPIT_TURNS_COUNTER, null, 1);
        if (turns >= FoodServerConfig.get().counters.roundAndRoundTurns.get()) {
            FoodTriggers.SPIT_TURNED_OFTEN.trigger(player);
        }
    }

    /** A player took something out of a furnace or smoker: bread there can only have been baked from Loaf Dough. */
    public static void onItemSmelted(ServerPlayer player, ItemStack smelted) {
        if (smelted.is(Items.BREAD)) {
            FoodTriggers.BREAD_BAKED.trigger(player);
        }
    }

    /** After any advancement: "Well Fed" once every other Realistic Food achievement that exists is earned. */
    public static void checkWellFed(ServerPlayer player) {
        boolean hasEverything = FoodAdvancements.wellFedRequirements().stream()
                .map(advancementId -> advancement(player, advancementId))
                .flatMap(Optional::stream)
                .allMatch(advancement -> player.getAdvancements().getOrStartProgress(advancement).isDone());
        if (hasEverything) {
            FoodTriggers.WELL_FED.trigger(player);
        }
    }

    // Missing advancements (Stone Soup without Crafting) don't count against "Well Fed".
    private static Optional<Advancement> advancement(ServerPlayer player, ResourceLocation advancementId) {
        return Optional.ofNullable(player.server.getAdvancements().getAdvancement(advancementId));
    }

    private static List<ServerPlayer> playersNear(ServerLevel level, BlockPos position) {
        return level.getEntitiesOfClass(ServerPlayer.class, new AABB(position).inflate(WITNESS_RANGE));
    }
}
