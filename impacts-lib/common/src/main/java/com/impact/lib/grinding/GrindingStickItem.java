package com.impact.lib.grinding;

import com.impact.lib.hold.HoldToUse;
import com.impact.lib.item.ConfiguredDurabilityItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** The grinding stick, held in the main hand: hold right-click over a loaded bowl in the off hand to grind one item per cycle. */
public final class GrindingStickItem extends Item implements ConfiguredDurabilityItem {
    private final GrinderTier tier;

    public GrindingStickItem(GrinderTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public GrinderTier tier() {
        return tier;
    }

    @Override
    public int configuredMaxDamage() {
        return tier.durability();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return HoldToUse.start(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stick, int remainingUseTicks) {
        HoldToUse.tick(level, user, stick);
    }

    @Override
    public void releaseUsing(ItemStack stick, Level level, LivingEntity user, int remainingUseTicks) {
        HoldToUse.release(level, user);
    }

    @Override
    public int getUseDuration(ItemStack stick) {
        return HoldToUse.USE_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stick) {
        return HoldToUse.USE_ANIMATION;
    }
}
