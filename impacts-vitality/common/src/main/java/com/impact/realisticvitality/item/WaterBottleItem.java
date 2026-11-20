package com.impact.realisticvitality.item;

import com.impact.realisticvitality.thirst.BottledWater;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A bottle of Vitality's water. It drinks like a water bottle (its thirst and Hydration come from the drink table when
 * drinking finishes), turns dirt into mud, pours into cauldrons and leaves a glass bottle in crafting grids.
 */
public final class WaterBottleItem extends Item {
    private static final int DRINK_TICKS = 32;
    private static final int MUD_SPLASH_PARTICLES = 5;

    public final BottledWater water;

    public WaterBottleItem(BottledWater water) {
        super(new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE));
        this.water = water;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity drinker) {
        if (drinker instanceof ServerPlayer player) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        drinker.gameEvent(GameEvent.DRINK);
        boolean isInfinite = drinker instanceof Player player && player.getAbilities().instabuild;
        if (isInfinite) {
            return stack;
        }
        stack.shrink(1);
        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        if (drinker instanceof Player player) {
            player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
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

    /** Pouring any of Vitality's water onto dirt makes mud, as a water bottle does. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos position = context.getClickedPos();
        boolean isMuddable = context.getClickedFace() != Direction.DOWN && level.getBlockState(position).is(BlockTags.CONVERTABLE_TO_MUD);
        Player player = context.getPlayer();
        if (!isMuddable || player == null) {
            return InteractionResult.PASS;
        }
        level.playSound(null, position, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.setItemInHand(context.getHand(), ItemUtils.createFilledResult(context.getItemInHand(), player, new ItemStack(Items.GLASS_BOTTLE)));
        player.awardStat(Stats.ITEM_USED.get(this));
        if (level instanceof ServerLevel serverLevel) {
            splash(serverLevel, position);
            level.setBlockAndUpdate(position, Blocks.MUD.defaultBlockState());
            level.gameEvent(null, GameEvent.FLUID_PLACE, position);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** The block of water the player is looking at, within reach, the way a glass bottle finds it. */
    public static BlockHitResult lookedAtWater(Level level, Player player, ClipContext.Fluid fluidMode) {
        return getPlayerPOVHitResult(level, player, fluidMode);
    }

    private static void splash(ServerLevel level, BlockPos position) {
        for (int particle = 0; particle < MUD_SPLASH_PARTICLES; particle++) {
            level.sendParticles(ParticleTypes.SPLASH, position.getX() + level.random.nextDouble(), position.getY() + 1,
                    position.getZ() + level.random.nextDouble(), 1, 0.0, 0.0, 0.0, 1.0);
        }
    }
}
