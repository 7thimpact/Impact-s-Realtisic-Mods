package com.impact.lib.forge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.impact.lib.advancement.LibAdvancements;
import com.impact.lib.forge.condition.NoImpactModsCondition;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.crafting.CraftingHelper;

import java.util.concurrent.CompletableFuture;

/** Writes "Why am I here?" with Forge's load condition, so it only exists when Lib is the only Impact mod. */
final class LibAdvancementProvider implements DataProvider {
    private static final String ADVANCEMENTS_FOLDER = "advancements";
    // Forge skips loading an advancement whose "conditions" aren't all met.
    private static final String CONDITIONS_KEY = "conditions";

    private final PackOutput.PathProvider advancementPaths;

    LibAdvancementProvider(PackOutput output) {
        this.advancementPaths = output.createPathProvider(PackOutput.Target.DATA_PACK, ADVANCEMENTS_FOLDER);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonObject advancement = LibAdvancements.whyAmIHere().serializeToJson();
        JsonArray conditions = new JsonArray();
        conditions.add(CraftingHelper.serialize(NoImpactModsCondition.INSTANCE));
        advancement.add(CONDITIONS_KEY, conditions);
        return DataProvider.saveStable(cache, advancement, advancementPaths.json(LibAdvancements.WHY_AM_I_HERE_ID));
    }

    @Override
    public String getName() {
        return "Impact's Lib advancements";
    }
}
