package com.impact.realisticvitality.item;

import com.impact.realisticvitality.air.AirTicks;
import com.impact.realisticvitality.config.VitalityServerConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Bottled Air: breathing it in gives back 5 bubbles and leaves the glass bottle. Stacks to 16. */
public final class BottledAirItem extends Item {
    private static final int STACK_SIZE = 16;
    private static final int BREATHE_TICKS = 16;

    public BottledAirItem() {
        super(new Item.Properties().stacksTo(STACK_SIZE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity breather) {
        int bubbles = VitalityServerConfig.get().breathing.bottledAirBubbles.get();
        breather.setAirSupply(Math.min(breather.getMaxAirSupply(), breather.getAirSupply() + bubbles * AirTicks.AIR_PER_BUBBLE));
        if (breather instanceof ServerPlayer player) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (breather instanceof Player player && !player.getAbilities().instabuild) {
            return ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE));
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return BREATHE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }
}
