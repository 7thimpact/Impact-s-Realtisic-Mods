package com.impact.realisticvitality.client;

import com.impact.realisticvitality.lang.VitalityTranslations;
import com.impact.realisticvitality.temperature.ArmorLining;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** The lining line on lined armor's tooltip. */
public final class LiningTooltips {
    private LiningTooltips() {
    }

    public static void addLines(ItemStack stack, List<Component> lines) {
        ArmorLining.of(stack).ifPresent(lining ->
                lines.add(Component.translatable(VitalityTranslations.liningTooltip(lining)).withStyle(ChatFormatting.BLUE)));
    }
}
