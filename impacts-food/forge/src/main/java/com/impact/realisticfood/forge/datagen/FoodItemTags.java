package com.impact.realisticfood.forge.datagen;

import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.food.FoodStyle;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.registry.FoodTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/** Food's item tags: knives, meat cuts, fish fillets, the three styles, styled foods and finished foods. */
final class FoodItemTags extends ItemTagsProvider {
    FoodItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), FoodMod.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(FoodTags.KNIVES).add(FoodItems.knives().toArray(Item[]::new));
        tag(FoodTags.MEAT_CUTS).add(piecesWhere(FoodPiece::isMeatCut));
        tag(FoodTags.FISH_FILLETS).add(piecesWhere(FoodPiece::isFishFillet));
        for (FoodStyle style : FoodStyle.values()) {
            tag(FoodTags.styledFoods(style)).add(FoodItems.styledFoods(style).toArray(Item[]::new));
        }
        tag(FoodTags.STYLED_FOODS).addTag(FoodTags.DRIED_FOODS).addTag(FoodTags.TOASTED_FOODS).addTag(FoodTags.BOILED_FOODS);
        tag(FoodTags.FINISHED_FOODS).addTag(FoodTags.STYLED_FOODS)
                .add(fixedFoods(FixedFood.FRIED_EGG, FixedFood.BOILED_EGG, FixedFood.BURNT_FOOD, FixedFood.MUSH));
    }

    private static Item[] fixedFoods(FixedFood... foods) {
        return Arrays.stream(foods).map(FoodItems::fixedFood).toArray(Item[]::new);
    }

    private static Item[] piecesWhere(Predicate<FoodPiece> filter) {
        return Arrays.stream(FoodPiece.values()).filter(filter).map(FoodItems::piece).toArray(Item[]::new);
    }
}
