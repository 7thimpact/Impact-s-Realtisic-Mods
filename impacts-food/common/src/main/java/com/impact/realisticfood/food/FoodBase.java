package com.impact.realisticfood.food;

import com.impact.realisticfood.registry.FoodItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * The spec's FoodVariants table: every base food, its raw form, and the names of its dried, toasted and boiled
 * versions. A single loop registers every styled item from it. Bread's raw form, Loaf Dough, arrives with baking (and
 * its doughs follow their own table).
 */
public enum FoodBase {
    BREAD("bread", () -> Items.BREAD, null, "Dry Bread", "Toasted Bread", "Boiled Bread", BoilTime.BREAD),
    COOKED_BEEF("cooked_beef", () -> Items.COOKED_BEEF, () -> Items.BEEF, "Beef Jerky", "Fire-Roasted Beef", "Boiled Beef", BoilTime.MEAT),
    COOKED_PORKCHOP("cooked_porkchop", () -> Items.COOKED_PORKCHOP, () -> Items.PORKCHOP, "Pork Jerky", "Fire-Roasted Pork", "Boiled Pork", BoilTime.MEAT),
    COOKED_MUTTON("cooked_mutton", () -> Items.COOKED_MUTTON, () -> Items.MUTTON, "Mutton Jerky", "Fire-Roasted Mutton", "Boiled Mutton", BoilTime.MEAT),
    COOKED_CHICKEN("cooked_chicken", () -> Items.COOKED_CHICKEN, () -> Items.CHICKEN, "Chicken Jerky", "Fire-Roasted Chicken", "Boiled Chicken",
            BoilTime.MEAT),
    COOKED_RABBIT("cooked_rabbit", () -> Items.COOKED_RABBIT, () -> Items.RABBIT, "Rabbit Jerky", "Fire-Roasted Rabbit", "Boiled Rabbit", BoilTime.MEAT),
    COOKED_COD("cooked_cod", () -> Items.COOKED_COD, () -> Items.COD, "Dried Cod", "Fire-Roasted Cod", "Poached Cod", BoilTime.MEAT),
    COOKED_SALMON("cooked_salmon", () -> Items.COOKED_SALMON, () -> Items.SALMON, "Dried Salmon", "Fire-Roasted Salmon", "Poached Salmon",
            BoilTime.MEAT),
    COOKED_TROPICAL_FISH("cooked_tropical_fish", () -> FoodItems.fixedFood(FixedFood.COOKED_TROPICAL_FISH), () -> Items.TROPICAL_FISH,
            "Dried Tropical Fish", "Fire-Roasted Tropical Fish", "Poached Tropical Fish", BoilTime.MEAT),
    COOKED_ROTTEN_FLESH("cooked_rotten_flesh", () -> FoodItems.fixedFood(FixedFood.COOKED_ROTTEN_FLESH), () -> Items.ROTTEN_FLESH,
            "Rotten Jerky", "Fire-Roasted Rotten Flesh", "Boiled Rotten Flesh", BoilTime.MEAT),
    COOKED_BEEF_CUT(FoodPiece.COOKED_BEEF_CUT, FoodPiece.RAW_BEEF_CUT, "Beef Jerky Strip", "Fire-Roasted Beef Cut", "Boiled Beef Cut", BoilTime.MEAT),
    COOKED_PORK_CUT(FoodPiece.COOKED_PORK_CUT, FoodPiece.RAW_PORK_CUT, "Pork Jerky Strip", "Fire-Roasted Pork Cut", "Boiled Pork Cut", BoilTime.MEAT),
    COOKED_MUTTON_CUT(FoodPiece.COOKED_MUTTON_CUT, FoodPiece.RAW_MUTTON_CUT, "Mutton Jerky Strip", "Fire-Roasted Mutton Cut", "Boiled Mutton Cut",
            BoilTime.MEAT),
    COOKED_CHICKEN_PIECE(FoodPiece.COOKED_CHICKEN_PIECE, FoodPiece.RAW_CHICKEN_PIECE, "Chicken Jerky Strip", "Fire-Roasted Chicken Piece",
            "Boiled Chicken Piece", BoilTime.MEAT),
    COOKED_RABBIT_PIECE(FoodPiece.COOKED_RABBIT_PIECE, FoodPiece.RAW_RABBIT_PIECE, "Rabbit Jerky Strip", "Fire-Roasted Rabbit Piece",
            "Boiled Rabbit Piece", BoilTime.MEAT),
    COOKED_COD_FILLET(FoodPiece.COOKED_COD_FILLET, FoodPiece.RAW_COD_FILLET, "Dried Cod Fillet", "Fire-Roasted Cod Fillet", "Poached Cod Fillet",
            BoilTime.MEAT),
    COOKED_SALMON_FILLET(FoodPiece.COOKED_SALMON_FILLET, FoodPiece.RAW_SALMON_FILLET, "Dried Salmon Fillet", "Fire-Roasted Salmon Fillet",
            "Poached Salmon Fillet", BoilTime.MEAT),
    BAKED_POTATO("baked_potato", () -> Items.BAKED_POTATO, () -> Items.POTATO, "Potato Crisps", "Fire-Roasted Potato", "Boiled Potato",
            BoilTime.OTHER),
    FRIED_POTATO_SLICES(FoodPiece.FRIED_POTATO_SLICES, FoodPiece.POTATO_SLICES, "Potato Chips", "Toasted Potato Slices", "Boiled Potato Slices",
            BoilTime.OTHER),
    CARROT("carrot", () -> Items.CARROT, null, "Dried Carrot", "Roasted Carrot", "Boiled Carrot", BoilTime.OTHER),
    CARROT_SLICES(FoodPiece.CARROT_SLICES, null, "Dried Carrot Slices", "Roasted Carrot Slices", "Boiled Carrot Slices", BoilTime.OTHER),
    BEETROOT("beetroot", () -> Items.BEETROOT, null, "Dried Beetroot", "Roasted Beetroot", "Boiled Beetroot", BoilTime.OTHER),
    APPLE("apple", () -> Items.APPLE, null, "Dried Apple", "Roasted Apple", "Stewed Apple", BoilTime.OTHER),
    MELON_SLICE("melon_slice", () -> Items.MELON_SLICE, null, "Dried Melon", "Grilled Melon", "Stewed Melon", BoilTime.OTHER),
    SWEET_BERRIES("sweet_berries", () -> Items.SWEET_BERRIES, null, "Dried Berries", "Roasted Berries", "Stewed Berries", BoilTime.OTHER),
    GLOW_BERRIES("glow_berries", () -> Items.GLOW_BERRIES, null, "Dried Glow Berries", "Roasted Glow Berries", "Stewed Glow Berries", BoilTime.OTHER),
    // Dried kelp is already dried: a furnace burns it instead.
    DRIED_KELP("dried_kelp", () -> Items.DRIED_KELP, () -> Items.KELP, null, "Roasted Kelp", "Boiled Kelp", BoilTime.OTHER),
    COOKIE("cookie", () -> Items.COOKIE, null, "Crunchy Cookie", "Toasted Cookie", "Soggy Cookie", BoilTime.OTHER),
    PUMPKIN_PIE("pumpkin_pie", () -> Items.PUMPKIN_PIE, null, "Crusty Pumpkin Pie", "Toasted Pumpkin Pie", "Soggy Pumpkin Pie", BoilTime.OTHER);

    /** The base item's ID path, which every styled ID ends with ({@code toasted_cooked_beef}). */
    public final String path;
    private final Supplier<Item> baseItem;
    private final Supplier<Item> rawItem;
    private final String driedName;
    private final String toastedName;
    private final String boiledName;
    public final BoilTime boilTime;

    FoodBase(String path, Supplier<Item> baseItem, Supplier<Item> rawItem, String driedName, String toastedName, String boiledName, BoilTime boilTime) {
        this.path = path;
        this.baseItem = baseItem;
        this.rawItem = rawItem;
        this.driedName = driedName;
        this.toastedName = toastedName;
        this.boiledName = boiledName;
        this.boilTime = boilTime;
    }

    FoodBase(FoodPiece base, FoodPiece raw, String driedName, String toastedName, String boiledName, BoilTime boilTime) {
        this(base.path, () -> FoodItems.piece(base), raw == null ? null : () -> FoodItems.piece(raw), driedName, toastedName, boiledName, boilTime);
    }

    public Item baseItem() {
        return baseItem.get();
    }

    /** The raw form, which cooks into the base food and boils straight into its boiled version. */
    public Optional<Item> rawItem() {
        return rawItem == null ? Optional.empty() : Optional.of(rawItem.get());
    }

    /** Whether this base has a version in the style (dried kelp has no dried version). */
    public boolean has(FoodStyle style) {
        return name(style).isPresent();
    }

    public Optional<String> name(FoodStyle style) {
        return Optional.ofNullable(switch (style) {
            case DRIED -> driedName;
            case TOASTED -> toastedName;
            case BOILED -> boiledName;
        });
    }

    /** Boil times from the spec: raw meat and fish 300 ticks, bread 100, everything else 200 (and cooked food 200). */
    public enum BoilTime {
        MEAT(300),
        BREAD(100),
        OTHER(200);

        private static final int COOKED_FOOD_BOIL_TICKS = 200;

        public final int rawBoilTicks;

        BoilTime(int rawBoilTicks) {
            this.rawBoilTicks = rawBoilTicks;
        }

        /** Boiling food that is already cooked: bread is quick, everything else takes 200 ticks. */
        public int cookedBoilTicks() {
            return this == BREAD ? rawBoilTicks : COOKED_FOOD_BOIL_TICKS;
        }
    }
}
