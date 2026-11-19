package com.impact.realisticinventory.forge.condition;

import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.WornPack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * {@code impactsinventory:packs_enabled}: true while {@code packs.enabled} is on in Inventory's common config. It gates
 * the pack recipes; the common file is read because recipes load before a world's server config.
 */
public final class PacksEnabledCondition implements ICondition {
    public static final ResourceLocation ID = ResourceIds.of(InventoryMod.MOD_ID, "packs_enabled");
    public static final PacksEnabledCondition INSTANCE = new PacksEnabledCondition();

    private PacksEnabledCondition() {
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return WornPack.arePacksEnabled();
    }

    /** Reads and writes the condition. It has no fields, so the JSON is only its type. */
    public static final class Serializer implements IConditionSerializer<PacksEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public void write(JsonObject json, PacksEnabledCondition condition) {
            // Nothing beyond the "type" key, which Forge writes.
        }

        @Override
        public PacksEnabledCondition read(JsonObject json) {
            return PacksEnabledCondition.INSTANCE;
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
