package com.impact.realisticfood.cooking;

import com.impact.lib.ResourceIds;
import com.impact.lib.config.ConfigValue;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.food.BakingIngredient;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.FoodBase;
import com.impact.realisticfood.food.FoodStyle;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Recipe IDs and cooking times for the cooking styles: cooked food dries in a furnace or smoker and toasts on a
 * campfire, dried and toasted food burns, and Food's new raw foods (tropical fish, rotten flesh, eggs) cook. Data
 * generation names its recipes with these IDs, and each recipe's time follows the server config.
 */
public final class StyleCooking {
    private static final String STYLES_FOLDER = "styles/";
    private static final String COOKING_FOLDER = "cooking/";
    private static final String BAKING_FOLDER = "baking/";
    /** The doughs, which bake like raw food: Dough into a Dough Bun or Flatbread, Loaf Dough into Bread or 3 Flatbread. */
    public static final List<BakingIngredient> DOUGHS = List.of(BakingIngredient.DOUGH, BakingIngredient.LOAF_DOUGH);
    private static final String FROM = "_from_";
    /** The new raw foods: tropical fish and rotten flesh cook into Food's own cooked versions. */
    public static final List<FixedFood> COOKED_FROM_RAW = List.of(FixedFood.COOKED_TROPICAL_FISH, FixedFood.COOKED_ROTTEN_FLESH);

    private StyleCooking() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        for (FoodBase base : FoodBase.values()) {
            if (base.has(FoodStyle.DRIED)) {
                CookingTimes.register(styledRecipeId(base, FoodStyle.DRIED, CookingMethod.SMELTING), () -> dryHeatTicks(CookingMethod.SMELTING).get());
                CookingTimes.register(styledRecipeId(base, FoodStyle.DRIED, CookingMethod.SMOKING), () -> dryHeatTicks(CookingMethod.SMOKING).get());
            }
            CookingTimes.register(styledRecipeId(base, FoodStyle.TOASTED, CookingMethod.CAMPFIRE), () -> dryHeatTicks(CookingMethod.CAMPFIRE).get());
        }
        for (CookingMethod method : CookingMethod.values()) {
            CookingTimes.register(burntRecipeId(method), () -> dryHeatTicks(method).get());
            COOKED_FROM_RAW.forEach(cooked -> CookingTimes.register(rawRecipeId(cooked.itemPath, method), () -> rawTicks(method).get()));
        }
        CookingTimes.register(rawRecipeId(FixedFood.FRIED_EGG.itemPath, CookingMethod.CAMPFIRE), () -> rawTicks(CookingMethod.CAMPFIRE).get());
        for (BakingIngredient dough : DOUGHS) {
            for (CookingMethod method : CookingMethod.values()) {
                CookingTimes.register(bakingRecipeId(dough, method), () -> rawTicks(method).get());
            }
        }
    }

    /** {@code impactsfood:baking/<dough>_<method>}: Dough and Loaf Dough baking in a furnace or smoker, or on a campfire. */
    public static ResourceLocation bakingRecipeId(BakingIngredient dough, CookingMethod method) {
        return ResourceIds.of(FoodMod.MOD_ID, BAKING_FOLDER + dough.itemPath + "_" + method.recipeSuffix);
    }

    /** {@code impactsfood:styles/<styled item>_from_<method>}. */
    public static ResourceLocation styledRecipeId(FoodBase base, FoodStyle style, CookingMethod method) {
        return ResourceIds.of(FoodMod.MOD_ID, STYLES_FOLDER + style.itemPath(base) + FROM + method.recipeSuffix);
    }

    /** {@code impactsfood:styles/burnt_food_from_<method>}. */
    public static ResourceLocation burntRecipeId(CookingMethod method) {
        return ResourceIds.of(FoodMod.MOD_ID, STYLES_FOLDER + FixedFood.BURNT_FOOD.itemPath + FROM + method.recipeSuffix);
    }

    /** {@code impactsfood:cooking/<result>_from_<method>}, for Food's new raw foods. */
    public static ResourceLocation rawRecipeId(String resultPath, CookingMethod method) {
        return ResourceIds.of(FoodMod.MOD_ID, COOKING_FOLDER + resultPath + FROM + method.recipeSuffix);
    }

    /** Drying and burning in a furnace or smoker, toasting and burning on a campfire. */
    public static ConfigValue<Integer> dryHeatTicks(CookingMethod method) {
        FoodServerConfig.StyleSettings styles = FoodServerConfig.get().styles;
        return switch (method) {
            case SMELTING -> styles.dryTicks;
            case SMOKING -> styles.smokerDryTicks;
            case CAMPFIRE -> styles.toastTicks;
        };
    }

    public static ConfigValue<Integer> rawTicks(CookingMethod method) {
        FoodServerConfig.StyleSettings styles = FoodServerConfig.get().styles;
        return switch (method) {
            case SMELTING -> styles.rawFurnaceTicks;
            case SMOKING -> styles.rawSmokerTicks;
            case CAMPFIRE -> styles.rawCampfireTicks;
        };
    }
}
