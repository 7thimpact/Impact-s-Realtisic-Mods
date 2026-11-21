package com.impact.realisticvitality.forge.brewing;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.thirst.BottledWater;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;

/**
 * Purified (and Cold) Water brews exactly like a water bottle: for any ingredient, the result is whatever vanilla brews
 * from a water bottle with it (Awkward, Mundane, Thick, Weakness, splash and lingering water bottles, and any brew
 * another mod adds from water).
 */
public final class PurifiedWaterBrewing implements IBrewingRecipe {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);

    private PurifiedWaterBrewing() {
    }

    public static void register() {
        BrewingRecipeRegistry.addRecipe(new PurifiedWaterBrewing());
        LOG.debug(VitalityLogCategories.STARTUP, "brewing recipes registered", "count", 1, "brewsLike", "minecraft:water");
    }

    @Override
    public boolean isInput(ItemStack input) {
        return VitalityItems.bottledWater(input).filter(water -> water.purity == BottledWater.PURIFIED.purity).isPresent();
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return PotionBrewing.isIngredient(ingredient);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (!isInput(input) || !isIngredient(ingredient)) {
            return ItemStack.EMPTY;
        }
        return BrewingRecipeRegistry.getOutput(vanillaWaterBottle(), ingredient);
    }

    private static ItemStack vanillaWaterBottle() {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
    }
}
