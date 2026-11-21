package com.impact.realisticvitality.player;

import com.impact.lib.ResourceIds;
import com.impact.lib.attachment.DeathCopyRule;
import com.impact.lib.attachment.PlayerAttachment;
import com.impact.lib.attachment.PlayerAttachmentData;
import com.impact.lib.attachment.PlayerAttachments;
import com.impact.realisticvitality.VitalityMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Everything Vitality keeps on a player, as one Lib attachment: {@code vitality.thirst}, {@code vitality.temperature}
 * and {@code vitality.air}. Saved with the player; like hunger, it resets on death and is kept when returning from the End.
 */
public final class VitalityState implements PlayerAttachmentData {
    public static final PlayerAttachment<VitalityState> ATTACHMENT = new PlayerAttachment<>(
            ResourceIds.of(VitalityMod.MOD_ID, "vitality"), VitalityState::new, DeathCopyRule.NEVER);
    private static final String THIRST_KEY = "Thirst";
    private static final String TEMPERATURE_KEY = "Temperature";
    private static final String AIR_KEY = "Air";

    public final ThirstState thirst = new ThirstState();
    public final TemperatureState temperature = new TemperatureState();
    public final AirState air = new AirState();
    public final HealthState health = new HealthState();

    /** Called once, during mod construction. */
    public static void register() {
        PlayerAttachments.register(ATTACHMENT);
    }

    public static VitalityState of(Player player) {
        return PlayerAttachments.get(player, ATTACHMENT);
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put(THIRST_KEY, thirst.save());
        tag.put(TEMPERATURE_KEY, temperature.save());
        tag.put(AIR_KEY, air.save());
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        thirst.load(tag.getCompound(THIRST_KEY));
        temperature.load(tag.getCompound(TEMPERATURE_KEY));
        air.load(tag.getCompound(AIR_KEY));
    }
}
