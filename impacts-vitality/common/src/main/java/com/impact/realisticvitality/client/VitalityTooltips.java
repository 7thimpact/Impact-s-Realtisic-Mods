package com.impact.realisticvitality.client;

import com.impact.lib.ImpactsLib;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.lang.VitalityTranslations;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.temperature.ArmorLining;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.WoodenBuckets;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Vitality's tooltip lines for its simpler items, the lining line on any lined armor, and the drinks in Crafting's Wooden Bucket. */
public final class VitalityTooltips {
    private VitalityTooltips() {
    }

    public static void addLines(ItemStack stack, List<Component> lines) {
        ArmorLining.of(stack).ifPresent(lining ->
                lines.add(Component.translatable(VitalityTranslations.liningTooltip(lining)).withStyle(ChatFormatting.BLUE)));
        VitalityItems.bottledWater(stack).ifPresent(water -> lines.add(gray(VitalityTranslations.waterTooltip(water))));
        if (stack.is(VitalityItems.charcoalFilter())) {
            lines.add(gray(VitalityTranslations.CHARCOAL_FILTER_TOOLTIP));
        }
        if (stack.is(VitalityItems.strawHat())) {
            lines.add(gray(VitalityTranslations.STRAW_HAT_TOOLTIP));
        }
        if (stack.is(VitalityItems.thermometer())) {
            lines.add(gray(VitalityTranslations.THERMOMETER_TOOLTIP));
        }
        if (stack.is(VitalityItems.thermometer()) && ImpactsLib.isLoaded(VitalityMod.CURIOS_MOD_ID)) {
            lines.add(gray(VitalityTranslations.THERMOMETER_CHARM_TOOLTIP));
        }
        if (stack.is(VitalityItems.bottledAir())) {
            lines.add(gray(VitalityTranslations.BOTTLED_AIR_TOOLTIP));
        }
        if (WoodenBuckets.isWaterBucket(stack)) {
            Component water = Component.translatable(VitalityTranslations.itemName(BottledWater.filledWith(WoodenBuckets.purity(stack))));
            lines.add(Component.translatable(VitalityTranslations.CONTAINER_DRINKS, WoodenBuckets.drinksLeft(stack),
                    VitalityServerConfig.get().thirst.woodenBucketDrinks.get(), water).withStyle(ChatFormatting.GRAY));
        }
    }

    private static Component gray(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.GRAY);
    }
}
