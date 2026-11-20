package com.impact.realisticvitality.temperature;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.advancement.VitalityTriggers;
import com.impact.realisticvitality.config.VitalityFeatures;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.lang.VitalityTranslations;
import com.impact.realisticvitality.registry.VitalityTags;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Too hot for dry food: while Hot or worse, foods in {@code #impactsvitality:dry_foods} can't be eaten. */
public final class DryFoods {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);

    private DryFoods() {
    }

    /** Whether eating this is refused at this body temperature. Each side asks with its own copy of the body temperature. */
    public static boolean isTooHotToEat(float body, ItemStack food) {
        boolean isRuleOn = TemperatureTicks.isOn() && VitalityFeatures.byConditionName(VitalityFeatures.DRY_FOOD_RULE).orElseThrow().getAsBoolean();
        return isRuleOn && food.is(VitalityTags.DRY_FOODS) && BodyState.of(body).isHotOrWorse();
    }

    /** Tells the player why, on the action bar, and logs it. */
    public static void onRefused(ServerPlayer player, ItemStack food) {
        player.displayClientMessage(Component.translatable(VitalityTranslations.TOO_HOT_TO_EAT), true);
        VitalityTriggers.DRY_FOOD_REFUSED.trigger(player);
        LOG.debug(VitalityLogCategories.FOOD, "dry food refused", "player", player.getScoreboardName(), "item", ResourceIds.itemId(food.getItem()));
    }
}
