package com.impact.lib.advancement;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Builds an advancement the way every Impact mod's achievement tab does it (Lib's spec, "Tab conventions"):
 * translation keys {@code advancements.<modid>.<path>.title} and {@code .description}, challenges give 50 XP, joke
 * achievements can be hidden until earned, and nothing is reported to Mojang's optional telemetry.
 */
public final class ImpactsAdvancementBuilder {
    public static final int CHALLENGE_EXPERIENCE = 50;
    private static final String TRANSLATION_PREFIX = "advancements.";
    private static final String TITLE_SUFFIX = ".title";
    private static final String DESCRIPTION_SUFFIX = ".description";
    private static final String PATH_SEPARATOR = "/";
    private static final String KEY_SEPARATOR = ".";

    private final ResourceLocation id;
    private final ItemStack icon;
    // recipeAdvancement() is vanilla's builder without the telemetry flag.
    private final Advancement.Builder builder = Advancement.Builder.recipeAdvancement();
    private FrameType frame = FrameType.TASK;
    private ResourceLocation background;
    private boolean isHidden;

    private ImpactsAdvancementBuilder(ResourceLocation id, ItemLike icon) {
        this.id = id;
        this.icon = new ItemStack(icon);
    }

    public static ImpactsAdvancementBuilder of(ResourceLocation id, ItemLike icon) {
        return new ImpactsAdvancementBuilder(id, icon);
    }

    /** Makes this the tab's root advancement, drawn over the given background texture. */
    public ImpactsAdvancementBuilder root(ResourceLocation tabBackground) {
        background = tabBackground;
        return this;
    }

    public ImpactsAdvancementBuilder parent(ResourceLocation parentId) {
        builder.parent(parentId);
        return this;
    }

    /** A big step in learning the mod. */
    public ImpactsAdvancementBuilder goal() {
        frame = FrameType.GOAL;
        return this;
    }

    /** A long grind, rewarded with 50 XP. */
    public ImpactsAdvancementBuilder challenge() {
        frame = FrameType.CHALLENGE;
        builder.rewards(AdvancementRewards.Builder.experience(CHALLENGE_EXPERIENCE));
        return this;
    }

    /** Hidden in the tab until earned, so the joke lands. */
    public ImpactsAdvancementBuilder hidden() {
        isHidden = true;
        return this;
    }

    public ImpactsAdvancementBuilder criterion(String criterionName, CriterionTriggerInstance trigger) {
        builder.addCriterion(criterionName, trigger);
        return this;
    }

    /** Earned when any one criterion is met, instead of all of them. */
    public ImpactsAdvancementBuilder anyCriterion() {
        builder.requirements(RequirementsStrategy.OR);
        return this;
    }

    public ResourceLocation id() {
        return id;
    }

    public static String titleKey(ResourceLocation advancementId) {
        return translationKey(advancementId) + TITLE_SUFFIX;
    }

    public static String descriptionKey(ResourceLocation advancementId) {
        return translationKey(advancementId) + DESCRIPTION_SUFFIX;
    }

    public Advancement.Builder build() {
        // Challenges announce in chat like goals; hidden advancements stay quiet until earned.
        builder.display(icon, Component.translatable(titleKey(id)), Component.translatable(descriptionKey(id)),
                background, frame, true, true, isHidden);
        return builder;
    }

    private static String translationKey(ResourceLocation advancementId) {
        return TRANSLATION_PREFIX + advancementId.getNamespace() + KEY_SEPARATOR + advancementId.getPath().replace(PATH_SEPARATOR, KEY_SEPARATOR);
    }
}
