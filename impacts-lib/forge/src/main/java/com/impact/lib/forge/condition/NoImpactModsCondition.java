package com.impact.lib.forge.condition;

import com.google.gson.JsonObject;
import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.LoneLibAchievement;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * {@code impactslib:no_impact_mods}: true only when no other Impact mod registered with Lib. It gates
 * "Why am I here?", so with any Impact mod installed that advancement and its tab don't exist.
 */
public final class NoImpactModsCondition implements ICondition {
    public static final ResourceLocation ID = ResourceIds.of(ImpactsLib.MOD_ID, "no_impact_mods");
    public static final NoImpactModsCondition INSTANCE = new NoImpactModsCondition();

    private NoImpactModsCondition() {
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return LoneLibAchievement.isLibAlone();
    }

    /** Reads and writes the condition. It has no fields, so the JSON is only its type. */
    public static final class Serializer implements IConditionSerializer<NoImpactModsCondition> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public void write(JsonObject json, NoImpactModsCondition condition) {
            // Nothing beyond the "type" key, which Forge writes.
        }

        @Override
        public NoImpactModsCondition read(JsonObject json) {
            return NoImpactModsCondition.INSTANCE;
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
