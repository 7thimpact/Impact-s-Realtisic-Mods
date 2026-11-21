package com.impact.realisticvitality.network;

import com.impact.lib.network.MessageCodec;
import com.impact.realisticvitality.config.VitalityCommonConfig;
import com.impact.realisticvitality.config.VitalityNeed;
import net.minecraft.network.FriendlyByteBuf;

/** Server to client, at login: which needs the server has on, so the HUD matches the server rather than the client's own file. */
public record NeedSwitchesMessage(boolean isThirstOn, boolean isTemperatureOn, boolean isAirOn) {
    public static final MessageCodec<NeedSwitchesMessage> CODEC = new MessageCodec<>() {
        @Override
        public void encode(NeedSwitchesMessage message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.isThirstOn());
            buffer.writeBoolean(message.isTemperatureOn());
            buffer.writeBoolean(message.isAirOn());
        }

        @Override
        public NeedSwitchesMessage decode(FriendlyByteBuf buffer) {
            return new NeedSwitchesMessage(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
        }
    };

    public static NeedSwitchesMessage fromConfig() {
        VitalityCommonConfig config = VitalityCommonConfig.get();
        return new NeedSwitchesMessage(config.isOn(VitalityNeed.THIRST), config.isOn(VitalityNeed.TEMPERATURE), config.isOn(VitalityNeed.AIR));
    }

    public boolean isOn(VitalityNeed need) {
        return switch (need) {
            case THIRST -> isThirstOn;
            case TEMPERATURE -> isTemperatureOn;
            case AIR -> isAirOn;
        };
    }
}
