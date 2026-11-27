package com.impact.realisticinventory.forge.condition;

import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.WornOn;
import com.impact.realisticinventory.pack.WornSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * {@code impactsinventory:back_slot_enabled}: true while {@code packs.backSlot} is on in Inventory's common config. It
 * gates Inventory's Curios data that gives every player the {@code back} slot.
 */
public final class BackSlotEnabledCondition implements ICondition {
    public static final ResourceLocation ID = ResourceIds.of(InventoryMod.MOD_ID, "back_slot_enabled");
    public static final BackSlotEnabledCondition INSTANCE = new BackSlotEnabledCondition();

    private BackSlotEnabledCondition() {
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return WornSlots.isEnabled(WornOn.BACK);
    }

    /** Reads and writes the condition. It has no fields, so the JSON is only its type. */
    public static final class Serializer implements IConditionSerializer<BackSlotEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public void write(JsonObject json, BackSlotEnabledCondition condition) {
            // Nothing beyond the "type" key, which Forge writes.
        }

        @Override
        public BackSlotEnabledCondition read(JsonObject json) {
            return BackSlotEnabledCondition.INSTANCE;
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
