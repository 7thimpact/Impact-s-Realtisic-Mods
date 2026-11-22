package com.impact.realisticfood.food;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.registry.FoodItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Styled foods join every item tag their base food is in, so "any cooked meat" recipes from other mods still take them.
 * Tags of the Impact family are left out: Food's own, and the other Impact mods', which describe a food as it is (a
 * moist melon slice isn't a moist dried melon slice). Runs as item tags are built, at each data load.
 */
public final class StyledTagInheritance {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    // impactsfood, impactsvitality, impactscrafting ...
    private static final String IMPACT_NAMESPACE_PREFIX = "impacts";

    private StyledTagInheritance() {
    }

    /** Adds each styled food to the tags that hold its base food. The map is the item tags vanilla just built. */
    @SuppressWarnings("unchecked")
    public static <T> void addStyledFoods(Map<ResourceLocation, Collection<T>> itemTags) {
        Map<Item, List<Holder<Item>>> styledByBase = styledFoodsByBase();
        int additions = 0;
        for (Map.Entry<ResourceLocation, Collection<T>> tag : itemTags.entrySet()) {
            if (tag.getKey().getNamespace().startsWith(IMPACT_NAMESPACE_PREFIX)) {
                continue;
            }
            List<Holder<Item>> inherited = new ArrayList<>();
            for (T entry : tag.getValue()) {
                inherited.addAll(styledByBase.getOrDefault(((Holder<Item>) entry).value(), List.of()));
            }
            if (inherited.isEmpty()) {
                continue;
            }
            Set<T> widened = new LinkedHashSet<>(tag.getValue());
            inherited.forEach(holder -> widened.add((T) holder));
            additions += widened.size() - tag.getValue().size();
            tag.setValue(widened);
        }
        LOG.debug(FoodLogCategories.STARTUP, "styled foods joined base tags", "additions", additions);
    }

    // Vanilla's registry is the one loader-independent code can reach; Forge deprecates it for its own.
    @SuppressWarnings("deprecation")
    private static Map<Item, List<Holder<Item>>> styledFoodsByBase() {
        Map<Item, List<Holder<Item>>> styledByBase = new HashMap<>();
        for (FoodBase base : FoodBase.values()) {
            List<Holder<Item>> styled = new ArrayList<>();
            for (FoodStyle style : FoodStyle.values()) {
                Optional<Item> styledItem = FoodItems.styled(base, style);
                styledItem.ifPresent(item -> styled.add(BuiltInRegistries.ITEM.wrapAsHolder(item)));
            }
            styledByBase.put(base.baseItem(), styled);
        }
        return styledByBase;
    }
}
