package com.impact.lib.advancement;

import com.google.gson.JsonObject;
import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * {@code impactslib:tool_crafted}: a player took a tool-crafting result. Optional item predicates: {@code result}, {@code tool}
 * (any tool used) and {@code byproduct} (any byproduct given, for Food's "Bone Appetit").
 */
public final class ToolCraftedTrigger extends SimpleCriterionTrigger<ToolCraftedTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceIds.of(ImpactsLib.MOD_ID, "tool_crafted");
    private static final String RESULT_FIELD = "result";
    private static final String TOOL_FIELD = "tool";
    private static final String BYPRODUCT_FIELD = "byproduct";

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player, ItemPredicate.fromJson(json.get(RESULT_FIELD)), ItemPredicate.fromJson(json.get(TOOL_FIELD)),
                ItemPredicate.fromJson(json.get(BYPRODUCT_FIELD)));
    }

    public void trigger(ServerPlayer player, ItemStack result, List<ItemStack> tools, List<ItemStack> byproducts) {
        trigger(player, instance -> instance.matches(result, tools, byproducts));
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate result;
        private final ItemPredicate tool;
        private final ItemPredicate byproduct;

        public TriggerInstance(ContextAwarePredicate player, ItemPredicate result, ItemPredicate tool, ItemPredicate byproduct) {
            super(ID, player);
            this.result = result;
            this.tool = tool;
            this.byproduct = byproduct;
        }

        public static TriggerInstance craftedWith(ItemPredicate tool) {
            return new TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY, tool, ItemPredicate.ANY);
        }

        public static TriggerInstance resultMatching(ItemPredicate result) {
            return new TriggerInstance(ContextAwarePredicate.ANY, result, ItemPredicate.ANY, ItemPredicate.ANY);
        }

        public static TriggerInstance byproductMatching(ItemPredicate byproduct) {
            return new TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, byproduct);
        }

        boolean matches(ItemStack craftedResult, List<ItemStack> tools, List<ItemStack> byproducts) {
            boolean isAnyByproduct = byproduct == ItemPredicate.ANY;
            return result.matches(craftedResult)
                    && (tool == ItemPredicate.ANY || tools.stream().anyMatch(tool::matches))
                    && (isAnyByproduct || byproducts.stream().anyMatch(byproduct::matches));
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.add(RESULT_FIELD, result.serializeToJson());
            json.add(TOOL_FIELD, tool.serializeToJson());
            json.add(BYPRODUCT_FIELD, byproduct.serializeToJson());
            return json;
        }
    }
}
