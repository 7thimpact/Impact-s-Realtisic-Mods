package com.impact.realisticfood.registry;

import com.impact.lib.grinding.Grinding;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.CreativeTabs;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.food.FinishedFood;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.item.FinishedFoodItem;
import com.impact.realisticfood.item.KnifeItem;
import com.impact.realisticfood.item.KnifeMaterial;
import com.impact.realisticfood.item.PieceFoodItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** Food's items, and its creative tab (which also lists Lib's grinder items, since Food ships their recipes). */
public final class FoodItems {
    private static final Map<KnifeMaterial, Supplier<KnifeItem>> KNIVES = new EnumMap<>(KnifeMaterial.class);
    private static final Map<FoodPiece, Supplier<Item>> PIECES = new EnumMap<>(FoodPiece.class);
    private static final Map<FinishedFood, Supplier<FinishedFoodItem>> FINISHED_FOODS = new EnumMap<>(FinishedFood.class);

    private FoodItems() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(FoodMod.MOD_ID);
        for (KnifeMaterial material : KnifeMaterial.values()) {
            KNIVES.put(material, registrar.register(Registries.ITEM, material.itemPath, () -> new KnifeItem(material)));
        }
        for (FoodPiece piece : FoodPiece.values()) {
            PIECES.put(piece, registrar.register(Registries.ITEM, piece.path, () -> newPieceItem(piece)));
        }
        for (FinishedFood finishedFood : FinishedFood.values()) {
            FINISHED_FOODS.put(finishedFood, registrar.register(Registries.ITEM, finishedFood.itemPath, () -> new FinishedFoodItem(finishedFood)));
        }
        CreativeTabs.register(FoodMod.MOD_ID, () -> new ItemStack(knife(KnifeMaterial.FLINT)), FoodItems::creativeTabItems);
    }

    public static KnifeItem knife(KnifeMaterial material) {
        return KNIVES.get(material).get();
    }

    public static List<Item> knives() {
        return KNIVES.values().stream().<Item>map(Supplier::get).toList();
    }

    public static Item piece(FoodPiece piece) {
        return PIECES.get(piece).get();
    }

    public static Item finishedFood(FinishedFood finishedFood) {
        return FINISHED_FOODS.get(finishedFood).get();
    }

    private static Item newPieceItem(FoodPiece piece) {
        return piece.isEdible ? new PieceFoodItem(piece) : new Item(new Item.Properties());
    }

    // Tools first, then Lib's grinder, then each raw piece followed by the piece it cooks into.
    private static List<ItemLike> creativeTabItems() {
        Set<Item> items = new LinkedHashSet<>(knives());
        items.addAll(Grinding.grinderItems());
        for (FoodPiece piece : FoodPiece.values()) {
            if (piece.cooks()) {
                items.add(piece(piece));
                items.add(piece(piece.cooked));
            }
        }
        Arrays.stream(FoodPiece.values()).map(FoodItems::piece).forEach(items::add);
        Arrays.stream(FinishedFood.values()).map(FoodItems::finishedFood).forEach(items::add);
        return List.copyOf(items);
    }
}
