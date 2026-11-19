package com.impact.realisticfood.registry;

import com.impact.lib.ResourceIds;
import com.impact.realisticfood.FoodMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Food's item tags (spec, "Tags"). */
public final class FoodTags {
    public static final TagKey<Item> KNIVES = itemTag("knives");
    /** Raw meat cuts and pieces, so a soup can ask for "any cut". */
    public static final TagKey<Item> MEAT_CUTS = itemTag("meat_cuts");
    /** Raw fish fillets, so a soup can ask for "any fillet". */
    public static final TagKey<Item> FISH_FILLETS = itemTag("fish_fillets");

    private FoodTags() {
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceIds.of(FoodMod.MOD_ID, path));
    }
}
