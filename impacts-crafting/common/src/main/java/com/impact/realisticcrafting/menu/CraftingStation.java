package com.impact.realisticcrafting.menu;

import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingFeature;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.recipe.TierResolver;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * The places a crafting grid lives, each with the highest tier it accepts: the 2×2 inventory grid and the Makeshift
 * Table take non-metal recipes, the Makeshift Stone Table adds iron, copper and redstone, and the Crafting Table (or
 * any other mod's crafting grid) takes everything.
 */
public enum CraftingStation {
    INVENTORY_GRID("inventory_grid", CraftingTier.NON_METAL, () -> CraftingServerConfig.get().tableTiers.checksInventoryGrid.get()),
    MAKESHIFT_TABLE("makeshift_table", CraftingTier.NON_METAL, () -> CraftingServerConfig.get().tableTiers.checksMakeshiftTable.get()),
    MAKESHIFT_STONE_TABLE("makeshift_stone_table", CraftingTier.STONE, () -> CraftingServerConfig.get().tableTiers.checksMakeshiftStoneTable.get()),
    CRAFTING_TABLE("crafting_table", CraftingTier.ADVANCED, () -> false);

    private static final int INVENTORY_GRID_SIZE = 2;

    public final String serializedName;
    public final CraftingTier highestAccepted;
    private final Supplier<Boolean> isCheckedSwitch;

    CraftingStation(String serializedName, CraftingTier highestAccepted, Supplier<Boolean> isCheckedSwitch) {
        this.serializedName = serializedName;
        this.highestAccepted = highestAccepted;
        this.isCheckedSwitch = isCheckedSwitch;
    }

    /** The station a menu's crafting grid belongs to. Menus Crafting doesn't know are treated as a Crafting Table. */
    public static CraftingStation of(AbstractContainerMenu menu) {
        if (menu instanceof TieredCraftingMenu tieredMenu) {
            return tieredMenu.station();
        }
        return menu instanceof InventoryMenu ? INVENTORY_GRID : CRAFTING_TABLE;
    }

    /** The first station that accepts a tier: the one a refused craft's hint names. */
    public static CraftingStation lowestAccepting(CraftingTier tier) {
        return Arrays.stream(values()).filter(station -> !tier.isAbove(station.highestAccepted)).findFirst().orElse(CRAFTING_TABLE);
    }

    /** The first station a recipe can be made at: the lowest that accepts its tier, and a 3×3 grid when it doesn't fit the 2×2. */
    public static CraftingStation lowestFor(Recipe<?> recipe) {
        CraftingStation byTier = lowestAccepting(TierResolver.tierOf(recipe));
        boolean fitsInventoryGrid = recipe.canCraftInDimensions(INVENTORY_GRID_SIZE, INVENTORY_GRID_SIZE);
        return byTier == INVENTORY_GRID && !fitsInventoryGrid ? MAKESHIFT_TABLE : byTier;
    }

    /** The translation key of the station's name, as JEI shows it under a recipe. */
    public String nameTranslationKey() {
        return "jei." + CraftingMod.MOD_ID + ".station." + serializedName;
    }

    /** Whether this station refuses the tier right now, with every switch that turns tier checks on. */
    public boolean refuses(CraftingTier tier) {
        boolean areTiersOn = CraftingFeature.TABLE_TIERS.isOn() && CraftingServerConfig.get().tableTiers.isOn.get();
        return areTiersOn && isCheckedSwitch.get() && tier.isAbove(highestAccepted);
    }

    /** The translation key of "Needs a <station>", shown over a refused craft. */
    public String needsTranslationKey() {
        return "tooltip." + CraftingMod.MOD_ID + ".needs." + serializedName;
    }
}
