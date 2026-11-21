package com.impact.realisticcrafting.advancement;

import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.CraftingMod;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.DyeColor;

import java.util.Optional;

/** {@code impactscrafting:bed_assembled}: a sheet finished or recolored a bed. Optional {@code color}: the sheet's color ("Full Spectrum"). */
public final class BedAssembledTrigger extends SimpleCriterionTrigger<BedAssembledTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceIds.of(CraftingMod.MOD_ID, "bed_assembled");
    private static final String COLOR_FIELD = "color";

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        Optional<DyeColor> color = json.has(COLOR_FIELD) ? Optional.ofNullable(DyeColor.byName(GsonHelper.getAsString(json, COLOR_FIELD), null))
                : Optional.empty();
        return new TriggerInstance(player, color);
    }

    public void trigger(ServerPlayer player, DyeColor color) {
        trigger(player, instance -> instance.color.map(color::equals).orElse(true));
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<DyeColor> color;

        public TriggerInstance(ContextAwarePredicate player, Optional<DyeColor> color) {
            super(ID, player);
            this.color = color;
        }

        public static TriggerInstance anyColor() {
            return new TriggerInstance(ContextAwarePredicate.ANY, Optional.empty());
        }

        public static TriggerInstance inColor(DyeColor color) {
            return new TriggerInstance(ContextAwarePredicate.ANY, Optional.of(color));
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            color.ifPresent(sheetColor -> json.addProperty(COLOR_FIELD, sheetColor.getName()));
            return json;
        }
    }
}
