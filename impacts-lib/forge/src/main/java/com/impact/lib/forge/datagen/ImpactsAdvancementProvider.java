package com.impact.lib.forge.datagen;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Base for every Impact mod's advancement data. Subclasses add advancements, optionally with Forge load conditions
 * (for example {@code forge:mod_loaded} for an achievement that needs another Impact mod).
 */
public abstract class ImpactsAdvancementProvider implements DataProvider {
    private static final String ADVANCEMENTS_FOLDER = "advancements";
    // Forge skips loading an advancement whose "conditions" aren't all met.
    private static final String CONDITIONS_KEY = "conditions";

    private final PackOutput.PathProvider advancementPaths;
    private final String providerName;

    protected ImpactsAdvancementProvider(PackOutput output, String providerName) {
        this.advancementPaths = output.createPathProvider(PackOutput.Target.DATA_PACK, ADVANCEMENTS_FOLDER);
        this.providerName = providerName;
    }

    /** Adds every advancement this mod ships. */
    protected abstract void addAdvancements(AdvancementOutput output);

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        addAdvancements((id, builder, conditions) -> writes.add(write(cache, id, builder, conditions)));
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return providerName;
    }

    private CompletableFuture<?> write(CachedOutput cache, ResourceLocation id, Advancement.Builder builder, ICondition[] conditions) {
        JsonObject advancement = builder.serializeToJson();
        if (conditions.length > 0) {
            advancement.add(CONDITIONS_KEY, CraftingHelper.serialize(conditions));
        }
        return DataProvider.saveStable(cache, advancement, advancementPaths.json(id));
    }

    /** Receives each advancement with its ID and any load conditions. */
    @FunctionalInterface
    public interface AdvancementOutput {
        void accept(ResourceLocation id, Advancement.Builder builder, ICondition... conditions);
    }
}
