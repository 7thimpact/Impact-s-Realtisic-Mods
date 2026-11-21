package com.impact.realisticcrafting.menu;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.recipe.TierResolver;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Optional;

/**
 * Whether a menu's crafting grid may craft a recipe. A station refuses any recipe above its tier: the result slot
 * stays empty and the screen names the station that's needed.
 */
public final class TableTierCheck {
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);

    private TableTierCheck() {
    }

    /** Server, while the result slot is worked out. Logs each refusal. */
    public static boolean allows(AbstractContainerMenu menu, Recipe<?> recipe, Player player) {
        Optional<CraftingStation> neededStation = neededStation(menu, recipe);
        neededStation.ifPresent(station -> LOG.debug(CraftingLogCategories.TIER, "craft refused", "player", player.getScoreboardName(),
                "recipe", recipe.getId(), "tier", TierResolver.tierOf(recipe).serializedName, "station", CraftingStation.of(menu).serializedName,
                "needs", station.serializedName));
        return neededStation.isEmpty();
    }

    /** The station a refused recipe needs, or empty when this menu's station accepts it. */
    public static Optional<CraftingStation> neededStation(AbstractContainerMenu menu, Recipe<?> recipe) {
        CraftingTier tier = TierResolver.tierOf(recipe);
        if (!CraftingStation.of(menu).refuses(tier)) {
            return Optional.empty();
        }
        return Optional.of(CraftingStation.lowestAccepting(tier));
    }
}
