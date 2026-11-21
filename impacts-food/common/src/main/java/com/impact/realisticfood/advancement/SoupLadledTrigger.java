package com.impact.realisticfood.advancement;

import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.realisticfood.FoodMod;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * {@code impactsfood:soup_ladled}: a player took a serving of soup from a pot, into a bowl or eaten straight from it.
 * The optional {@code soup} field names the soup's item ID ("Master Chef", "Watered Down").
 */
public final class SoupLadledTrigger extends SimpleCriterionTrigger<SoupLadledTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceIds.of(FoodMod.MOD_ID, "soup_ladled");
    private static final String SOUP_FIELD = "soup";

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        Optional<ResourceLocation> soup = json.has(SOUP_FIELD) ? ResourceIds.tryParse(GsonHelper.getAsString(json, SOUP_FIELD)) : Optional.empty();
        return new TriggerInstance(player, soup);
    }

    public void trigger(ServerPlayer player, ItemStack serving) {
        ResourceLocation servedId = ResourceIds.itemId(serving.getItem());
        trigger(player, instance -> instance.soup.map(servedId::equals).orElse(true));
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ResourceLocation> soup;

        public TriggerInstance(ContextAwarePredicate player, Optional<ResourceLocation> soup) {
            super(ID, player);
            this.soup = soup;
        }

        public static TriggerInstance anySoup() {
            return new TriggerInstance(ContextAwarePredicate.ANY, Optional.empty());
        }

        public static TriggerInstance soup(ResourceLocation soupItemId) {
            return new TriggerInstance(ContextAwarePredicate.ANY, Optional.of(soupItemId));
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            soup.ifPresent(soupId -> json.addProperty(SOUP_FIELD, soupId.toString()));
            return json;
        }
    }
}
