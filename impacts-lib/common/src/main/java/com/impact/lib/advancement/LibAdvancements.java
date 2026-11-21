package com.impact.lib.advancement;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.lang.LibTranslations;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

/**
 * Lib's one advancement, "Why am I here?". It is its own tab, so Minecraft names the tab after it. Data
 * generation adds the {@code impactslib:no_impact_mods} condition, so with any other Impact mod installed the
 * advancement and its tab don't exist at all.
 */
public final class LibAdvancements {
    public static final ResourceLocation WHY_AM_I_HERE_ID = ResourceIds.of(ImpactsLib.MOD_ID, "why_am_i_here");
    public static final String JOINED_ALONE_CRITERION = "joined_alone";
    private static final ResourceLocation TAB_BACKGROUND = ResourceIds.of("minecraft", "textures/gui/advancements/backgrounds/stone.png");
    private static final boolean IS_TOAST_SHOWN = true;
    private static final boolean IS_ANNOUNCED_IN_CHAT = true;
    private static final boolean IS_HIDDEN = false;

    private LibAdvancements() {
    }

    public static Advancement.Builder whyAmIHere() {
        // recipeAdvancement() is vanilla's builder without the flag that reports the advancement to Mojang's
        // optional telemetry, which a mod's advancements shouldn't do.
        return Advancement.Builder.recipeAdvancement()
                .display(Items.BOOK,
                        Component.translatable(LibTranslations.WHY_AM_I_HERE_TITLE),
                        Component.translatable(LibTranslations.WHY_AM_I_HERE_DESCRIPTION),
                        TAB_BACKGROUND, FrameType.TASK, IS_TOAST_SHOWN, IS_ANNOUNCED_IN_CHAT, IS_HIDDEN)
                .addCriterion(JOINED_ALONE_CRITERION, JoinedAloneTrigger.TriggerInstance.anyPlayer());
    }
}
