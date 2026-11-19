package com.impact.realisticfood.registry;

import com.impact.lib.grinding.Grinding;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.CreativeTabs;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.FoodBase;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.food.FoodStyle;
import com.impact.realisticfood.item.FixedFoodItem;
import com.impact.realisticfood.item.KnifeItem;
import com.impact.realisticfood.item.KnifeMaterial;
import com.impact.realisticfood.item.PieceFoodItem;
import com.impact.realisticfood.item.StyledFoodItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Food's items: knives, pieces, its own foods and every styled food from the {@link FoodBase} table, plus its creative tab
 * (which also lists Lib's grinder items, since Food ships their recipes).
 */
public final class FoodItems {
    private static final Map<KnifeMaterial, Supplier<KnifeItem>> KNIVES = new EnumMap<>(KnifeMaterial.class);
    private static final Map<FoodPiece, Supplier<Item>> PIECES = new EnumMap<>(FoodPiece.class);
    private static final Map<FixedFood, Supplier<FixedFoodItem>> FIXED_FOODS = new EnumMap<>(FixedFood.class);
    private static final Map<FoodStyle, Map<FoodBase, Supplier<StyledFoodItem>>> STYLED_FOODS = new EnumMap<>(FoodStyle.class);

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
        for (FixedFood fixedFood : FixedFood.values()) {
            FIXED_FOODS.put(fixedFood, registrar.register(Registries.ITEM, fixedFood.itemPath, () -> new FixedFoodItem(fixedFood)));
        }
        // Styled foods read their base's values when they are built, so they come after the pieces and fixed foods.
        for (FoodStyle style : FoodStyle.values()) {
            Map<FoodBase, Supplier<StyledFoodItem>> styledByBase = new EnumMap<>(FoodBase.class);
            Arrays.stream(FoodBase.values()).filter(base -> base.has(style)).forEach(base ->
                    styledByBase.put(base, registrar.register(Registries.ITEM, style.itemPath(base), () -> new StyledFoodItem(base, style))));
            STYLED_FOODS.put(style, styledByBase);
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

    public static Item fixedFood(FixedFood fixedFood) {
        return FIXED_FOODS.get(fixedFood).get();
    }

    /** The base food's version in the style; empty for dried kelp, which has no dried version. */
    public static Optional<Item> styled(FoodBase base, FoodStyle style) {
        return Optional.ofNullable(STYLED_FOODS.get(style).get(base)).map(Supplier::get);
    }

    public static List<Item> styledFoods(FoodStyle style) {
        return STYLED_FOODS.get(style).values().stream().<Item>map(Supplier::get).toList();
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
        Arrays.stream(FixedFood.values()).map(FoodItems::fixedFood).forEach(items::add);
        for (FoodBase base : FoodBase.values()) {
            Arrays.stream(FoodStyle.values()).map(style -> styled(base, style)).flatMap(Optional::stream).forEach(items::add);
        }
        return List.copyOf(items);
    }
}
