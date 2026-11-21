package com.impact.realisticfood.client;

import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.item.KnifeItem;
import com.impact.realisticfood.item.StyledFoodItem;
import com.impact.realisticfood.lang.FoodTranslations;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.registry.FoodTags;
import com.impact.realisticfood.spit.Spits;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/** Food's tooltip lines, for its own items and for vanilla raw meat and fish: what a style does, how to use a tool, and the raw-food risk. */
public final class FoodTooltips {
    private FoodTooltips() {
    }

    /** Adds the item's lines, in gray, under its name. */
    public static void addLines(ItemStack stack, List<Component> lines) {
        if (stack.getItem() instanceof StyledFoodItem styled) {
            lines.add(gray(FoodTranslations.styleTooltip(styled.style())));
        }
        if (stack.getItem() instanceof KnifeItem) {
            lines.add(gray(FoodTranslations.KNIFE_TOOLTIP));
        }
        if (stack.is(Spits.spitItem())) {
            lines.add(gray(FoodTranslations.SPIT_TOOLTIP));
        }
        if (isRawPiece(stack)) {
            lines.add(gray(FoodTranslations.PIECE_TOOLTIP));
        }
        if (stack.is(FoodTags.RAW_RISKY)) {
            lines.add(Component.translatable(FoodTranslations.RAW_RISK_TOOLTIP).withStyle(ChatFormatting.DARK_RED));
        }
    }

    // A raw piece is one that cooks into another piece.
    private static boolean isRawPiece(ItemStack stack) {
        return Arrays.stream(FoodPiece.values()).filter(FoodPiece::cooks).anyMatch(piece -> stack.is(FoodItems.piece(piece)));
    }

    private static Component gray(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.GRAY);
    }
}
