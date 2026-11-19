package com.impact.realisticfood.forge.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.impact.lib.ResourceIds;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodFeature;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * {@code impactsfood:feature_enabled}: true while one of Food's features is switched on in its common config, for
 * example {@code {"type": "impactsfood:feature_enabled", "feature": "cutting"}}. Recipes load before a world's server
 * config, so the switches live in the common file.
 */
public record FeatureEnabledCondition(FoodFeature feature) implements ICondition {
    public static final ResourceLocation ID = ResourceIds.of(FoodMod.MOD_ID, "feature_enabled");
    private static final String FEATURE_FIELD = "feature";

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return feature.isOn();
    }

    public static final class Serializer implements IConditionSerializer<FeatureEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public void write(JsonObject json, FeatureEnabledCondition condition) {
            json.addProperty(FEATURE_FIELD, condition.feature().conditionName);
        }

        @Override
        public FeatureEnabledCondition read(JsonObject json) {
            String featureName = GsonHelper.getAsString(json, FEATURE_FIELD);
            FoodFeature feature = FoodFeature.byConditionName(featureName)
                    .orElseThrow(() -> new JsonParseException("Unknown Impact's Realistic Food feature: " + featureName));
            return new FeatureEnabledCondition(feature);
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
