package com.impact.realisticfood.forge.compat.jei;

import com.impact.realisticfood.lang.FoodTranslations;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Recipe times as JEI shows them: ticks in seconds, to one decimal place. */
final class JeiTimes {
    private static final float TICKS_PER_SECOND = 20.0F;

    private JeiTimes() {
    }

    static Component seconds(double ticks) {
        return Component.translatable(FoodTranslations.JEI_SECONDS, String.format(Locale.ROOT, "%.1f", ticks / TICKS_PER_SECOND));
    }
}
