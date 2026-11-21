package com.impact.realisticvitality.temperature;

import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.registry.VitalityTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Everything that insulates a player right now: armor by its tags (warm, cooling, conductive), linings, the Straw Hat
 * in the sun, and being Wet.
 */
public final class Insulations {
    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    private Insulations() {
    }

    public static Insulation of(Player player) {
        VitalityServerConfig.InsulationSettings settings = VitalityServerConfig.get().insulation;
        Insulation total = Insulation.NONE;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            total = total.plus(ofPiece(player.getItemBySlot(slot), slot));
        }
        if (isStrawHatInTheSun(player)) {
            total = total.plus(new Insulation(0.0, settings.strawHatCooling.get()));
        }
        if (VitalityState.of(player).temperature.isWet()) {
            total = total.plus(new Insulation(0.0, settings.wetCooling.get()));
        }
        return total;
    }

    /** One worn piece: its material's insulation, plus its lining's. */
    public static Insulation ofPiece(ItemStack piece, EquipmentSlot slot) {
        if (piece.isEmpty()) {
            return Insulation.NONE;
        }
        VitalityServerConfig.InsulationSettings settings = VitalityServerConfig.get().insulation;
        Insulation total = Insulation.NONE;
        if (piece.is(VitalityTags.WARM_ARMOR)) {
            double warmth = slot == EquipmentSlot.CHEST ? settings.warmArmorChestWarmth.get() : settings.warmArmorWarmth.get();
            total = total.plus(new Insulation(warmth, 0.0));
        }
        if (piece.is(VitalityTags.COOLING_ARMOR)) {
            total = total.plus(new Insulation(0.0, settings.coolingArmorCooling.get()));
        }
        if (piece.is(VitalityTags.CONDUCTIVE_ARMOR)) {
            double penalty = settings.conductiveArmorPenalty.get();
            total = total.plus(new Insulation(-penalty, -penalty));
        }
        return ArmorLining.of(piece).map(lining -> lining.insulation()).map(total::plus).orElse(total);
    }

    // Shade only helps under the sun: open sky, daytime and no rain.
    private static boolean isStrawHatInTheSun(Player player) {
        boolean isWearingHat = player.getItemBySlot(EquipmentSlot.HEAD).is(VitalityItems.strawHat());
        return isWearingHat && player.level().isDay() && !player.level().isRaining() && player.level().canSeeSky(player.blockPosition().above());
    }
}
