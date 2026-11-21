package com.impact.realisticvitality.item;

import com.impact.realisticvitality.air.AirTank;
import com.impact.realisticvitality.lang.VitalityTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** An Air Tank: its bar shows the air left. It works from anywhere in the inventory, or the back slot, with a Respirator on. */
public final class AirTankItem extends Item {
    private static final float FULL_BAR = 13.0F;
    private static final int AIR_COLOR = 0xA0D8F0;
    private static final int SECONDS = 20;

    public final AirTank tank;

    public AirTankItem(AirTank tank, Item.Properties properties) {
        super(properties.stacksTo(1));
        this.tank = tank;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return tank.airIn(stack) < tank.capacity();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(FULL_BAR * Mth.clamp((float) tank.airIn(stack) / tank.capacity(), 0.0F, 1.0F));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return AIR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(VitalityTranslations.TANK_AIR, tank.airIn(stack) / SECONDS, tank.capacity() / SECONDS)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(VitalityTranslations.TANK_NEEDS_RESPIRATOR).withStyle(ChatFormatting.BLUE));
    }
}
