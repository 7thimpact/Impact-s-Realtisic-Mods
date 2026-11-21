package com.impact.realisticcrafting.item;

import com.impact.lib.hold.HoldToUse;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * An unfinished item: stacks to 1, mines and hits like an empty hand, and can't be enchanted or repaired. With a hammer
 * in the off hand, holding right-click hammers it into its result (Crafting's hammering recipes). Every 10 ticks the
 * off hand swings with a knock and a few chips fly.
 */
public class LooseItem extends Item {
    public static final String HAMMER_HINT_KEY = "tooltip." + CraftingMod.MOD_ID + ".hammer_hint";
    private static final int BLOW_INTERVAL_TICKS = 10;
    private static final int CHIP_PARTICLES = 4;
    private static final float KNOCK_VOLUME = 0.6F;
    private static final double PARTICLE_SPREAD = 0.15;
    private static final double HANDS_REACH = 0.5;
    private static final double HANDS_BELOW_EYES = 0.4;

    private final HammerKnock knock;

    public LooseItem(HammerKnock knock) {
        super(new Item.Properties().stacksTo(1));
        this.knock = knock;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        return HoldToUse.start(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack loose, int remainingUseTicks) {
        HoldToUse.tick(level, user, loose);
        if (user instanceof Player player && player.isUsingItem() && player.getTicksUsingItem() % BLOW_INTERVAL_TICKS == 0) {
            strikeBlow(level, player);
        }
    }

    @Override
    public void releaseUsing(ItemStack loose, Level level, LivingEntity user, int remainingUseTicks) {
        HoldToUse.release(level, user);
    }

    @Override
    public int getUseDuration(ItemStack loose) {
        return HoldToUse.USE_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack loose) {
        return HoldToUse.USE_ANIMATION;
    }

    @Override
    public boolean isEnchantable(ItemStack loose) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack loose, ItemStack repairMaterial) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack loose, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (level != null && level.isClientSide() && CraftingClientConfig.get().areNextStepHintsOn.get()) {
            tooltip.add(Component.translatable(HAMMER_HINT_KEY).withStyle(ChatFormatting.GRAY));
        }
    }

    // Everyone sees the swing; the knock and chips are the hammering player's own, behind the client config.
    private void strikeBlow(Level level, Player player) {
        if (!level.isClientSide()) {
            player.swing(InteractionHand.OFF_HAND, true);
            level.playSound(player, player.getX(), player.getY(), player.getZ(), knock.sound(), SoundSource.PLAYERS, KNOCK_VOLUME, 1.0F);
            return;
        }
        if (!CraftingClientConfig.get().areHammeringEffectsOn.get()) {
            return;
        }
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), knock.sound(), SoundSource.PLAYERS, KNOCK_VOLUME, 1.0F, false);
        BlockParticleOption chips = new BlockParticleOption(ParticleTypes.BLOCK, knock.chips());
        double handsX = player.getX() + player.getLookAngle().x * HANDS_REACH;
        double handsY = player.getEyeY() - HANDS_BELOW_EYES;
        double handsZ = player.getZ() + player.getLookAngle().z * HANDS_REACH;
        for (int particle = 0; particle < CHIP_PARTICLES; particle++) {
            level.addParticle(chips, handsX + (level.random.nextDouble() - 0.5) * PARTICLE_SPREAD, handsY,
                    handsZ + (level.random.nextDouble() - 0.5) * PARTICLE_SPREAD, 0.0, 0.0, 0.0);
        }
    }

    /** What a hammer blow sounds like and what chips off: wood for wooden items, stone for stone ones, metal for iron. */
    public enum HammerKnock {
        WOOD,
        STONE,
        METAL;

        SoundEvent sound() {
            return switch (this) {
                case WOOD -> SoundEvents.WOOD_HIT;
                case STONE -> SoundEvents.STONE_HIT;
                case METAL -> SoundEvents.ANVIL_HIT;
            };
        }

        BlockState chips() {
            return switch (this) {
                case WOOD -> Blocks.OAK_PLANKS.defaultBlockState();
                case STONE -> Blocks.STONE.defaultBlockState();
                case METAL -> Blocks.IRON_BLOCK.defaultBlockState();
            };
        }
    }
}
