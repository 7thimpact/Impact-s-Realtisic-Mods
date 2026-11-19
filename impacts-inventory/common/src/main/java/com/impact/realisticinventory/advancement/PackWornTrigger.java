package com.impact.realisticinventory.advancement;

import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.PackKind;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;
import java.util.Optional;

/** {@code impactsinventory:pack_worn}: a player put a pack on. The optional {@code pack} field names its kind ({@code satchel}, {@code backpack}, {@code frame_pack}). */
public final class PackWornTrigger extends SimpleCriterionTrigger<PackWornTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceIds.of(InventoryMod.MOD_ID, "pack_worn");
    private static final String PACK_FIELD = "pack";

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        Optional<PackKind> kind = json.has(PACK_FIELD) ? kindByName(GsonHelper.getAsString(json, PACK_FIELD)) : Optional.empty();
        return new TriggerInstance(player, kind);
    }

    public void trigger(ServerPlayer player, PackKind worn) {
        trigger(player, instance -> instance.kind.map(kind -> kind == worn).orElse(true));
    }

    private static Optional<PackKind> kindByName(String name) {
        return Arrays.stream(PackKind.values()).filter(kind -> kind.getSerializedName().equals(name)).findFirst();
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<PackKind> kind;

        public TriggerInstance(ContextAwarePredicate player, Optional<PackKind> kind) {
            super(ID, player);
            this.kind = kind;
        }

        public static TriggerInstance wearing(PackKind kind) {
            return new TriggerInstance(ContextAwarePredicate.ANY, Optional.of(kind));
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            kind.ifPresent(packKind -> json.addProperty(PACK_FIELD, packKind.getSerializedName()));
            return json;
        }
    }
}
