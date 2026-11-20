package com.impact.realisticvitality.item;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.BreathingMask;
import com.impact.realisticvitality.air.MaskFilters;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.lang.VitalityTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * A breathing mask worn on the head. A Cloth Mask used on water gets damp ({@code impactsvitality:damp}, ticks left);
 * Respirators and Rebreathers show their filter's life.
 */
public final class BreathingMaskItem extends ArmorItem {
    public static final String DAMP_KEY = VitalityMod.MOD_ID + ":damp";
    private static final int SECONDS = 20;

    public final BreathingMask mask;

    public BreathingMaskItem(BreathingMask mask, Item.Properties properties) {
        super(mask.material, ArmorItem.Type.HELMET, properties);
        this.mask = mask;
    }

    public static int dampTicks(ItemStack mask) {
        return mask.hasTag() ? mask.getTag().getInt(DAMP_KEY) : 0;
    }

    public static void setDampTicks(ItemStack mask, int ticks) {
        if (ticks <= 0) {
            mask.removeTagKey(DAMP_KEY);
            return;
        }
        mask.getOrCreateTag().putInt(DAMP_KEY, ticks);
    }

    /** A Cloth Mask used while looking at water gets damp; otherwise a mask is put on, like any helmet. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (mask != BreathingMask.CLOTH_MASK || !isLookingAtWater(level, player)) {
            return super.use(level, player, hand);
        }
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 0.6F, 1.4F);
        if (!level.isClientSide) {
            setDampTicks(held, VitalityServerConfig.get().breathing.clothMaskDampTicks.get());
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (mask == BreathingMask.CLOTH_MASK) {
            int damp = dampTicks(stack);
            tooltip.add(damp > 0 ? Component.translatable(VitalityTranslations.MASK_DAMP, damp / SECONDS).withStyle(ChatFormatting.BLUE)
                    : Component.translatable(VitalityTranslations.MASK_DRY).withStyle(ChatFormatting.GRAY));
        }
        if (mask.takesFilter) {
            ItemStack filter = MaskFilters.filterIn(stack);
            tooltip.add(filter.isEmpty() ? Component.translatable(VitalityTranslations.MASK_NO_FILTER).withStyle(ChatFormatting.RED)
                    : Component.translatable(VitalityTranslations.MASK_FILTER, filter.getMaxDamage() - filter.getDamageValue()).withStyle(ChatFormatting.BLUE));
        }
        if (mask.blocksSmokeAlways) {
            tooltip.add(Component.translatable(VitalityTranslations.MASK_NEVER_SMOKE).withStyle(ChatFormatting.BLUE));
        }
    }

    private static boolean isLookingAtWater(Level level, Player player) {
        BlockHitResult hit = WaterBottleItem.lookedAtWater(level, player, ClipContext.Fluid.SOURCE_ONLY);
        return hit.getType() == HitResult.Type.BLOCK && level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER);
    }
}
