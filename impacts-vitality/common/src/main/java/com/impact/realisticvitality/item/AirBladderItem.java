package com.impact.realisticvitality.item;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.BreathingGear;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.lang.VitalityTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** The Air Bladder: filled in good air (3 uses), then each use in thin air gives back 4 bubbles. Uses are {@code impactsvitality:air_uses}. */
public final class AirBladderItem extends Item {
    public static final String USES_KEY = VitalityMod.MOD_ID + ":air_uses";
    private static final float FULL_BAR = 13.0F;
    private static final int AIR_COLOR = 0xA0D8F0;

    public AirBladderItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static int uses(ItemStack bladder) {
        return bladder.hasTag() ? bladder.getTag().getInt(USES_KEY) : 0;
    }

    public static void setUses(ItemStack bladder, int uses) {
        bladder.getOrCreateTag().putInt(USES_KEY, Math.max(0, uses));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bladder = player.getItemInHand(hand);
        return BreathingGear.useAirBladder(level, player, bladder) ? InteractionResultHolder.sidedSuccess(bladder, level.isClientSide)
                : InteractionResultHolder.pass(bladder);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int maximum = VitalityServerConfig.get().breathing.airBladderUses.get();
        return Math.round(FULL_BAR * Mth.clamp((float) uses(stack) / maximum, 0.0F, 1.0F));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return AIR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int maximum = VitalityServerConfig.get().breathing.airBladderUses.get();
        tooltip.add(Component.translatable(VitalityTranslations.BLADDER_USES, uses(stack), maximum).withStyle(ChatFormatting.GRAY));
    }
}
