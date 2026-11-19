package com.impact.realisticfood.raw;

import com.impact.lib.ResourceIds;
import com.impact.lib.config.KeyValueEntries;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.registry.FoodTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalDouble;

/**
 * Eating raw meat or fish in {@code #impactsfood:raw_risky} can give Hunger: {@code rawFood.chance} (30%) for
 * {@code rawFood.durationTicks} (15 seconds), with per-food chances in {@code rawFood.overrides}. Applied once the food
 * is eaten, so vanilla's food values and other mods' food tweaks are untouched. Raw chicken keeps vanilla's own risk.
 */
public final class RawFoodRisk {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);

    private RawFoodRisk() {
    }

    /** Called when a player finishes eating. True when the food made them sick. */
    public static boolean onEaten(ServerPlayer player, ItemStack eaten) {
        if (!eaten.is(FoodTags.RAW_RISKY) || !FoodFeature.RAW_FOOD_RISK.isOn()) {
            return false;
        }
        FoodServerConfig.RawFoodSettings settings = FoodServerConfig.get().rawFood;
        String foodId = ResourceIds.itemId(eaten.getItem()).toString();
        OptionalDouble override = KeyValueEntries.findDouble(settings.overrides.get(), foodId);
        double chance = override.orElse(settings.chance.get());
        float roll = player.getRandom().nextFloat();
        boolean isSick = roll < chance;
        if (isSick) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, settings.durationTicks.get()));
        }
        LOG.debug(FoodLogCategories.RAW, "raw food eaten", "player", player.getGameProfile().getName(), "food", foodId,
                "chance", chance, "roll", roll, "hunger", isSick);
        return isSick;
    }
}
