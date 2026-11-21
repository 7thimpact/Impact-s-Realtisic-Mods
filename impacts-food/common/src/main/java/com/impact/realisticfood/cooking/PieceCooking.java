package com.impact.realisticfood.cooking;

import com.impact.lib.ResourceIds;
import com.impact.lib.config.ConfigValue;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.food.FoodPiece;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

/**
 * Raw pieces cook in half a whole item's time: 100 ticks in a furnace, 50 in a smoker and 300 on a campfire, from
 * {@code pieces.*} in the server config.
 */
public final class PieceCooking {
    private static final String RECIPE_FOLDER = "cooking/";
    private static final String FROM = "_from_";

    private PieceCooking() {
    }

    /** Called once, during mod construction: each piece recipe's time follows the config. */
    public static void register() {
        Arrays.stream(FoodPiece.values()).filter(FoodPiece::cooks).forEach(piece -> {
            for (CookingMethod method : CookingMethod.values()) {
                CookingTimes.register(recipeId(piece, method), () -> ticksSetting(method).get());
            }
        });
    }

    /** {@code impactsfood:cooking/<cooked piece>_from_<method>}. */
    public static ResourceLocation recipeId(FoodPiece rawPiece, CookingMethod method) {
        return ResourceIds.of(FoodMod.MOD_ID, RECIPE_FOLDER + rawPiece.cooked.path + FROM + method.recipeSuffix);
    }

    public static ConfigValue<Integer> ticksSetting(CookingMethod method) {
        FoodServerConfig.PieceSettings settings = FoodServerConfig.get().pieces;
        return switch (method) {
            case SMELTING -> settings.furnaceTicks;
            case SMOKING -> settings.smokerTicks;
            case CAMPFIRE -> settings.campfireTicks;
        };
    }
}
