package com.impact.realisticcrafting.forge.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.OverhaulCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * {@code impactscrafting:overhaul_enabled}: true while a catalog category's switch is on in Crafting's common config,
 * for example {@code {"type": "impactscrafting:overhaul_enabled", "category": "wood"}}. Crafting's replacement recipes
 * load under it, and the vanilla recipes they replace under its {@code forge:not}.
 */
public record OverhaulEnabledCondition(OverhaulCategory category) implements ICondition {
    public static final ResourceLocation ID = ResourceIds.of(CraftingMod.MOD_ID, "overhaul_enabled");
    private static final String CATEGORY_FIELD = "category";

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return category.isOn();
    }

    public static final class Serializer implements IConditionSerializer<OverhaulEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public void write(JsonObject json, OverhaulEnabledCondition condition) {
            json.addProperty(CATEGORY_FIELD, condition.category().configKey);
        }

        @Override
        public OverhaulEnabledCondition read(JsonObject json) {
            String categoryName = GsonHelper.getAsString(json, CATEGORY_FIELD);
            OverhaulCategory category = OverhaulCategory.byConfigKey(categoryName)
                    .orElseThrow(() -> new JsonParseException("Unknown Impact's Realistic Crafting catalog category: " + categoryName));
            return new OverhaulEnabledCondition(category);
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
