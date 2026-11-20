package com.impact.realisticvitality.item;

import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.lang.VitalityTranslations;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.ContainerContents;
import com.impact.realisticvitality.thirst.WaterContainer;
import com.impact.realisticvitality.thirst.WaterContainers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A drink container. Use it on water to fill it, use it otherwise to drink one drink, and sneak-use it to pour one
 * over yourself. Its bar shows how full it is, in the color of what's inside.
 */
public class WaterContainerItem extends Item {
    private static final int DRINK_TICKS = 32;
    private static final float FULL_BAR = 13.0F;
    private static final int SOUP_COLOR = 0xB5763A;

    public final WaterContainer container;

    public WaterContainerItem(WaterContainer container) {
        this(container, new Item.Properties().stacksTo(1));
    }

    protected WaterContainerItem(WaterContainer container, Item.Properties properties) {
        super(properties);
        this.container = container;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return resultOf(WaterContainers.pourOverSelf(level, player, stack), stack);
        }
        InteractionResult filling = WaterContainers.tryFillFromWorld(level, player, hand, container);
        if (filling != InteractionResult.PASS) {
            return resultOf(filling, stack);
        }
        if (ContainerContents.read(stack).isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity drinker) {
        if (drinker instanceof ServerPlayer player) {
            WaterContainers.finishDrink(player, stack);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return DRINK_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity carrier, int slot, boolean isSelected) {
        if (container.refillsItself && !level.isClientSide) {
            WaterContainers.refillTick(stack, carrier, level.getGameTime());
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !ContainerContents.read(stack).isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        float share = (float) ContainerContents.read(stack).drinks() / WaterContainers.capacity(container);
        return Math.round(FULL_BAR * Mth.clamp(share, 0.0F, 1.0F));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        ContainerContents contents = ContainerContents.read(stack);
        return switch (contents.kind()) {
            case COLD_WATER -> BottledWater.COLD.liquidColor;
            case SOUP -> SOUP_COLOR;
            case WATER -> BottledWater.filledWith(contents.purity()).liquidColor;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        ContainerContents contents = ContainerContents.read(stack);
        int capacity = WaterContainers.capacity(container);
        if (contents.isEmpty()) {
            tooltip.add(Component.translatable(VitalityTranslations.CONTAINER_EMPTY, capacity).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable(VitalityTranslations.CONTAINER_DRINKS, contents.drinks(), capacity, contentsName(contents))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (container.keepsTemperature) {
            tooltip.add(Component.translatable(VitalityTranslations.CONTAINER_KEEPS_TEMPERATURE).withStyle(ChatFormatting.BLUE));
        }
        if (container.refillsItself) {
            tooltip.add(Component.translatable(VitalityTranslations.CONTAINER_REFILLS).withStyle(ChatFormatting.BLUE));
        }
    }

    private static Component contentsName(ContainerContents contents) {
        return switch (contents.kind()) {
            case COLD_WATER -> Component.translatable(VitalityTranslations.itemName(BottledWater.COLD));
            case SOUP -> contents.soup().flatMap(ResourceIds::item).map(Item::getDescription)
                    .orElse(Component.translatable(VitalityTranslations.CONTAINER_SOUP));
            case WATER -> Component.translatable(VitalityTranslations.itemName(BottledWater.filledWith(contents.purity())));
        };
    }

    private static InteractionResultHolder<ItemStack> resultOf(InteractionResult result, ItemStack stack) {
        return new InteractionResultHolder<>(result, stack);
    }
}
