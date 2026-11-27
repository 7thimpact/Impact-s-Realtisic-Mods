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
 * {@code impactsinventory:chest_slot_enabled}: true while {@code packs.chestSlot} is on in Inventory's common config.
 * It gates Inventory's Curios data that gives every player the {@code body} slot, where a second pack is worn on the chest.
 */
public final class ChestSlotEnabledCondition implements ICondition {
    public static final ResourceLocation ID = ResourceIds.of(InventoryMod.MOD_ID, "chest_slot_enabled");
    public static final ChestSlotEnabledCondition INSTANCE = new ChestSlotEnabledCondition();

    private ChestSlotEnabledCondition() {
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return WornSlots.isEnabled(WornOn.CHEST);
    }

    /** Reads and writes the condition. It has no fields, so the JSON is only its type. */
    public static final class Serializer implements IConditionSerializer<ChestSlotEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public void write(JsonObject json, ChestSlotEnabledCondition condition) {
            // Nothing beyond the "type" key, which Forge writes.
        }

        @Override
        public ChestSlotEnabledCondition read(JsonObject json) {
            return ChestSlotEnabledCondition.INSTANCE;
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
