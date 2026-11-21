package com.impact.realisticcrafting.forge.datagen;

import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.forge.loot.BedPartsLootModifier;
import com.impact.realisticcrafting.forge.loot.PebbleLootModifier;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

/** Global loot modifiers: pebbles from gravel and stone, and beds' parts. Each checks the block itself, so neither needs conditions. */
final class CraftingLootModifiers extends GlobalLootModifierProvider {
    CraftingLootModifiers(PackOutput output) {
        super(output, CraftingMod.MOD_ID);
    }

    @Override
    protected void start() {
        add(PebbleLootModifier.PATH, new PebbleLootModifier(new LootItemCondition[0]));
        add(BedPartsLootModifier.PATH, new BedPartsLootModifier(new LootItemCondition[0]));
    }
}
