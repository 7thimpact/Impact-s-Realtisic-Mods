package com.impact.realisticvitality.item;

import com.impact.lib.hold.HoldToUse;
import com.impact.realisticvitality.lang.VitalityTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The Air Tank's plates riveted together but not yet sealed (only obtainable with Impact's Realistic Crafting): it
 * holds no air until it's hammered into an Air Tank. Its use methods hand over to Lib's hold-to-use, where Crafting
 * registers its hammering action for every hammering recipe's input, so it hammers like Crafting's own loose items.
 */
public final class LooseAirTankItem extends Item {
    public LooseAirTankItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        return HoldToUse.start(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack looseTank, int remainingUseTicks) {
        HoldToUse.tick(level, user, looseTank);
    }

    @Override
    public void releaseUsing(ItemStack looseTank, Level level, LivingEntity user, int remainingUseTicks) {
        HoldToUse.release(level, user);
    }

    @Override
    public int getUseDuration(ItemStack looseTank) {
        return HoldToUse.USE_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack looseTank) {
        return HoldToUse.USE_ANIMATION;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(VitalityTranslations.LOOSE_AIR_TANK_HINT).withStyle(ChatFormatting.GRAY));
    }
}
