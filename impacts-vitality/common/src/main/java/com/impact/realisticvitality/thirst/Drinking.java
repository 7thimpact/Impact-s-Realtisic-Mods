package com.impact.realisticvitality.thirst;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.advancement.VitalityAchievements;
import com.impact.realisticvitality.advancement.VitalityTriggers;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.player.ThirstState;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityEffects;
import com.impact.realisticvitality.temperature.BodyTemperature;
import com.impact.realisticvitality.temperature.TemperatureTicks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

import java.util.Map;
import java.util.Optional;

/** Applies drinks and foods to thirst once they are finished: thirst, Hydration, and the Parched roll for unclean water. */
public final class Drinking {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final String NO_PURITY = "none";

    private Drinking() {
    }

    /** Called when any item finishes being used (eaten or drunk), with the stack as it was before: thirst, then warmth or cooling. */
    public static void onUseFinished(LivingEntity entity, ItemStack used) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        if (TemperatureTicks.isOn()) {
            BodyTemperature.onConsumed(player, DrinkValues.asDrink(used));
        }
        if (!ThirstDrain.isOn()) {
            return;
        }
        Optional<DrinkValue> value = DrinkValues.of(used);
        if (value.isEmpty()) {
            return;
        }
        String itemId = ResourceIds.itemId(used.getItem()).toString();
        // Drinks are what's drunk (water, milk, honey, potions), whatever food they also give; everything else is eaten.
        String category = used.getUseAnimation() == UseAnim.DRINK ? VitalityLogCategories.DRINK : VitalityLogCategories.FOOD;
        drink(player, value.get(), DrinkValues.purityOf(used), itemId, category);
    }

    /** Gives the player a drink's thirst and Hydration, rolls Parched for unclean water, and logs it. */
    public static void drink(ServerPlayer player, DrinkValue value, Optional<WaterPurity> purity, String source, String logCategory) {
        ThirstState thirst = VitalityState.of(player).thirst;
        int thirstBefore = thirst.level();
        float hydrationBefore = thirst.hydration();
        thirst.setLevel(thirstBefore + value.thirst());
        thirst.setHydration(hydrationBefore + value.hydration());
        boolean isParched = purity.filter(WaterPurity::canMakeParched).isPresent() && rollParched(player);
        String event = logCategory.equals(VitalityLogCategories.FOOD) ? "food thirst" : "drink";
        if (logCategory.equals(VitalityLogCategories.DRINK)) {
            VitalityTriggers.DRANK.trigger(player, Map.of(
                    "water", VitalityTriggers.fact(purity.isPresent()),
                    "purity", purity.map(drinkPurity -> drinkPurity.serializedName).orElse(NO_PURITY),
                    "parched", VitalityTriggers.fact(isParched)));
            VitalityAchievements.onDrink(player);
        }
        LOG.debug(logCategory, event, "player", player.getScoreboardName(), "item", source,
                "purity", purity.map(drinkPurity -> drinkPurity.serializedName).orElse(NO_PURITY),
                "thirst", thirst.level() - thirstBefore, "hydration", thirst.hydration() - hydrationBefore, "parched", isParched);
    }

    private static boolean rollParched(ServerPlayer player) {
        VitalityServerConfig.ThirstSettings settings = VitalityServerConfig.get().thirst;
        if (player.getRandom().nextDouble() >= settings.parchedChance.get()) {
            return false;
        }
        player.addEffect(new MobEffectInstance(VitalityEffects.parched(), settings.parchedTicks.get()));
        return true;
    }
}
