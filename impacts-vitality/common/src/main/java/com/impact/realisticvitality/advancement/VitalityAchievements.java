package com.impact.realisticvitality.advancement;

import com.impact.lib.counter.ImpactsCounters;
import com.impact.realisticvitality.config.TaggedEntries;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.temperature.ArmorLining;
import com.impact.realisticvitality.temperature.BodyState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Map;

/** Achievement checks that aren't a single event: once a second, and after each drink. */
public final class VitalityAchievements {
    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    private static final long DAY_TICKS = 24000L;
    private static final long NOON_TICKS = 6000L;
    /** "Noon" is the hour around midday. */
    private static final long NOON_HALF_WIDTH = 500L;
    private static final int LOW_THIRST = 6;

    private VitalityAchievements() {
    }

    /** Once a second, after body temperature has moved: the temperature_state trigger, worn gear and "In Balance". */
    public static void onSecond(ServerPlayer player) {
        VitalityState state = VitalityState.of(player);
        BodyState bodyState = BodyState.of(state.temperature.body());
        VitalityTriggers.TEMPERATURE_STATE.trigger(player, Map.of(
                "state", bodyState.serializedName,
                "cold_or_worse", VitalityTriggers.fact(bodyState.isColdOrWorse()),
                "hot_or_worse", VitalityTriggers.fact(bodyState.isHotOrWorse()),
                "on_fire", VitalityTriggers.fact(player.isOnFire()),
                "thirst_full", VitalityTriggers.fact(state.thirst.level() >= 20),
                "air_full", VitalityTriggers.fact(player.getAirSupply() >= player.getMaxAirSupply())));
        if (isWearingWarmLinings(player)) {
            gearUsed(player, "warm_linings");
        }
        if (isWearingStrawHatInDesertNoon(player)) {
            gearUsed(player, "straw_hat_in_desert_noon");
        }
        countBalance(player, state, bodyState);
    }

    /** After every drink (not food): the drink counter behind "Well Watered". */
    public static void onDrink(ServerPlayer player) {
        int drinks = ImpactsCounters.add(player, VitalityTriggers.DRINKS_COUNTER, null, 1);
        if (drinks >= VitalityServerConfig.get().counters.wellWateredDrinks.get()) {
            VitalityTriggers.WELL_WATERED.trigger(player);
        }
    }

    public static void gearUsed(ServerPlayer player, String gear) {
        VitalityTriggers.GEAR_USED.trigger(player, Map.of("gear", gear));
    }

    private static boolean isWearingWarmLinings(ServerPlayer player) {
        return ARMOR_SLOTS.stream().allMatch(slot -> ArmorLining.of(player.getItemBySlot(slot))
                .filter(lining -> lining == ArmorLining.WOOL || lining == ArmorLining.FUR).isPresent());
    }

    private static boolean isWearingStrawHatInDesertNoon(ServerPlayer player) {
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(VitalityItems.strawHat())) {
            return false;
        }
        long timeOfDay = player.level().getDayTime() % DAY_TICKS;
        boolean isNoon = Math.abs(timeOfDay - NOON_TICKS) <= NOON_HALF_WIDTH;
        Holder<Biome> biome = player.level().getBiome(player.blockPosition());
        boolean isDesert = TaggedEntries.contains(VitalityServerConfig.get().temperature.desertBiomes.get(), biome::is,
                tagId -> biome.is(TagKey.create(Registries.BIOME, tagId)));
        return isNoon && isDesert && player.level().canSeeSky(player.blockPosition().above());
    }

    // A whole day with thirst above 6, air never empty and body temperature never past Cold or Hot; any break starts over.
    private static void countBalance(ServerPlayer player, VitalityState state, BodyState bodyState) {
        boolean isInBalance = state.thirst.level() > LOW_THIRST && player.getAirSupply() > 0 && !bodyState.isColdOrWorse() && !bodyState.isHotOrWorse();
        if (!isInBalance) {
            if (ImpactsCounters.get(player, VitalityTriggers.BALANCE_SECONDS_COUNTER, null) > 0) {
                ImpactsCounters.reset(player, VitalityTriggers.BALANCE_SECONDS_COUNTER);
            }
            return;
        }
        int seconds = ImpactsCounters.add(player, VitalityTriggers.BALANCE_SECONDS_COUNTER, null, 1);
        if (seconds >= VitalityServerConfig.get().counters.inBalanceSeconds.get()) {
            VitalityTriggers.IN_BALANCE.trigger(player);
        }
    }
}
