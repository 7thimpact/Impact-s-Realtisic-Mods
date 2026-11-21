package com.impact.realisticfood.food;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.item.FixedFoodItem;
import com.impact.realisticfood.item.StyledFoodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** The {@code styles} log line: which style each cook produced (cooked, dried, toasted or boiled), or Burnt Food or Mush. */
public final class StyleLog {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final String COOKED = "cooked";
    private static final String BURNT = "burnt";
    private static final String MUSH = "mush";
    private static volatile Set<Item> baseItems;

    private StyleLog() {
    }

    /** Logs a cook whose result is a base food, a styled food, Burnt Food or Mush; other cooks are left out. */
    public static void onCooked(String method, ItemStack input, ItemStack result) {
        if (!LOG.isEnabled(FoodLogCategories.STYLES)) {
            return;
        }
        styleOf(result.getItem()).ifPresent(style -> LOG.debug(FoodLogCategories.STYLES, "cooked", "method", method,
                "input", ResourceIds.itemId(input.getItem()), "result", ResourceIds.itemId(result.getItem()), "style", style));
    }

    private static Optional<String> styleOf(Item item) {
        if (item instanceof StyledFoodItem styled) {
            return Optional.of(styled.style().prefix);
        }
        if (item instanceof FixedFoodItem fixed && fixed.fixedFood() == FixedFood.BURNT_FOOD) {
            return Optional.of(BURNT);
        }
        if (item instanceof FixedFoodItem fixed && fixed.fixedFood() == FixedFood.MUSH) {
            return Optional.of(MUSH);
        }
        return baseItems().contains(item) ? Optional.of(COOKED) : Optional.empty();
    }

    private static Set<Item> baseItems() {
        Set<Item> known = baseItems;
        if (known == null) {
            known = Arrays.stream(FoodBase.values()).map(FoodBase::baseItem).collect(Collectors.toUnmodifiableSet());
            baseItems = known;
        }
        return known;
    }
}
