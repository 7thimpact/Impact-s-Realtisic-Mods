package com.impact.realisticvitality.forge.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import java.util.function.BooleanSupplier;

/**
 * {@code impactsvitality:feature_enabled}: true while a need, a piece or a gear item is switched on in Vitality's common
 * config, for example {@code {"type": "impactsvitality:feature_enabled", "feature": "canteen"}}. Recipes load before a
 * world's server config, so the switches live in the common file. The accepted names are in {@link VitalityFeatures}.
 */
public record FeatureEnabledCondition(String featureName, BooleanSupplier featureSwitch) implements ICondition {
    public static final ResourceLocation ID = ResourceIds.of(VitalityMod.MOD_ID, "feature_enabled");
    private static final String FEATURE_FIELD = "feature";

    public static FeatureEnabledCondition of(String featureName) {
        BooleanSupplier featureSwitch = VitalityFeatures.byConditionName(featureName)
                .orElseThrow(() -> new JsonParseException("Unknown Impact's Realistic Vitality feature: " + featureName));
        return new FeatureEnabledCondition(featureName, featureSwitch);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return featureSwitch.getAsBoolean();
    }

    public static final class Serializer implements IConditionSerializer<FeatureEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public void write(JsonObject json, FeatureEnabledCondition condition) {
            json.addProperty(FEATURE_FIELD, condition.featureName());
        }

        @Override
        public FeatureEnabledCondition read(JsonObject json) {
            return FeatureEnabledCondition.of(GsonHelper.getAsString(json, FEATURE_FIELD));
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
