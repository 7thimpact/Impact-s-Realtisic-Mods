package com.impact.realisticcrafting.forge.compat.jei;

import com.impact.realisticcrafting.lang.CraftingTranslations;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Times as JEI shows them: ticks in seconds, to one decimal place. */
final class JeiText {
    private static final float TICKS_PER_SECOND = 20.0F;

    private JeiText() {
    }

    static Component seconds(int ticks) {
        return Component.translatable(CraftingTranslations.JEI_SECONDS, String.format(Locale.ROOT, "%.1f", ticks / TICKS_PER_SECOND));
    }
}
