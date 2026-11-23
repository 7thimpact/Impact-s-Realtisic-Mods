package com.impact.realisticinventory.pack;

import com.impact.realisticinventory.config.InventoryServerConfig;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/** A worn Frame Pack slows walking by {@code packs.framePackSlowness} (10% by default). */
public final class PackEffects {
    private static final UUID FRAME_PACK_SLOWNESS_ID = UUID.fromString("7d0f2b8e-3c5a-4e61-9b1f-5a8c2d4e6f10");
    private static final String FRAME_PACK_SLOWNESS_NAME = "impactsinventory:frame_pack_slowness";

    private PackEffects() {
    }

    /** Server, once a tick per player: adds, updates or removes the slowdown to match what's worn. */
    public static void tick(Player player) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) {
            return;
        }
        double slowness = InventoryServerConfig.get().packs.framePackSlowness.get();
        boolean isSlowed = slowness > 0 && WornPack.kindOf(player).filter(PackKind::slowsWalking).isPresent();
        AttributeModifier current = movementSpeed.getModifier(FRAME_PACK_SLOWNESS_ID);
        boolean isUpToDate = isSlowed ? current != null && current.getAmount() == -slowness : current == null;
        if (isUpToDate) {
            return;
        }
        movementSpeed.removeModifier(FRAME_PACK_SLOWNESS_ID);
        if (isSlowed) {
            movementSpeed.addTransientModifier(new AttributeModifier(FRAME_PACK_SLOWNESS_ID, FRAME_PACK_SLOWNESS_NAME, -slowness,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }
}
