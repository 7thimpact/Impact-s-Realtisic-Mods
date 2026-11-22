package com.impact.realisticvitality.thirst;

import com.impact.lib.ResourceIds;
import com.impact.lib.event.WaterFilled;
import com.impact.lib.item.UsesDurability;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.player.VitalityState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Drinking from Impact's Realistic Crafting's Water-filled Wooden Bucket. Crafting owns the item and pours it on blocks;
 * Vitality takes the clicks at the air (Lib's shared interactions): hold use to drink, sneak-use to pour it over
 * yourself. A full bucket holds {@code thirst.woodenBucketDrinks} drinks, counted in {@code impactsvitality:drinks},
 * with the purity Vitality stamps when Lib's water-filled event says the bucket filled. Drinking costs no bucket uses.
 * Once it's empty it becomes Crafting's empty Wooden Bucket, keeping only its uses. The items are found by registry ID.
 */
public final class WoodenBuckets {
    public static final ResourceLocation WATER_BUCKET = ResourceIds.of(VitalityMod.CRAFTING_MOD_ID, "wooden_water_bucket");
    public static final ResourceLocation EMPTY_BUCKET = ResourceIds.of(VitalityMod.CRAFTING_MOD_ID, "wooden_bucket");
    public static final String DRINKS_KEY = VitalityMod.MOD_ID + ":drinks";
    public static final String PURITY_KEY = VitalityMod.MOD_ID + ":purity";
    public static final int DRINK_TICKS = 32;
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final int POUR_SPLASH_PARTICLES = 12;
    // Vanilla's reach, which is also how far Crafting looks for a block to pour onto.
    private static final double SURVIVAL_REACH_BLOCKS = 4.5;
    private static final double CREATIVE_REACH_BLOCKS = 5.0;

    private WoodenBuckets() {
    }

    public static boolean isWaterBucket(ItemStack stack) {
        return !stack.isEmpty() && ResourceIds.itemId(stack.getItem()).equals(WATER_BUCKET);
    }

    /** Drinks left; a bucket Vitality never stamped (a command's) is full. */
    public static int drinksLeft(ItemStack bucket) {
        CompoundTag tag = bucket.getTag();
        boolean isStamped = tag != null && tag.contains(DRINKS_KEY);
        return isStamped ? tag.getInt(DRINKS_KEY) : VitalityServerConfig.get().thirst.woodenBucketDrinks.get();
    }

    /** The water's purity; a bucket Vitality never stamped holds unclean water. */
    public static WaterPurity purity(ItemStack bucket) {
        CompoundTag tag = bucket.getTag();
        Optional<WaterPurity> stamped = tag == null ? Optional.empty() : WaterPurity.bySerializedName(tag.getString(PURITY_KEY));
        return stamped.orElse(WaterPurity.UNCLEAN);
    }

    /**
     * Lib's water-filled event: a Wooden Bucket just filled (Crafting scooped it, or Food handed it back from a
     * stone-boiling pot). Purified if it boiled, salty from ocean and beach biomes, otherwise unclean; and full.
     */
    public static void onWaterFilled(WaterFilled event) {
        if (!isWaterBucket(event.filledStack())) {
            return;
        }
        WaterPurity purity = event.boiled() ? WaterPurity.PURIFIED
                : WaterSources.isSalty(event.level().getBiome(event.position())) ? WaterPurity.SALT : WaterPurity.UNCLEAN;
        CompoundTag tag = event.filledStack().getOrCreateTag();
        tag.putString(PURITY_KEY, purity.serializedName);
        tag.putInt(DRINKS_KEY, VitalityServerConfig.get().thirst.woodenBucketDrinks.get());
        LOG.debug(VitalityLogCategories.PURITY, "wooden bucket stamped", "player", event.player().getScoreboardName(),
                "purity", purity.serializedName, "boiled", event.boiled(), "pos", event.position().toShortString());
    }

    /** Whether a use click is aimed at the air (nothing Crafting would pour onto), where Vitality drinks or pours. */
    public static boolean isAimedAtAir(Level level, Player player) {
        Vec3 eyes = player.getEyePosition();
        double reachBlocks = player.getAbilities().instabuild ? CREATIVE_REACH_BLOCKS : SURVIVAL_REACH_BLOCKS;
        Vec3 reach = eyes.add(player.getViewVector(1.0F).scale(reachBlocks));
        BlockHitResult target = level.clip(new ClipContext(eyes, reach, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return target.getType() == HitResult.Type.MISS;
    }

    /**
     * A use click with the bucket aimed at the air: sneaking pours it over yourself, otherwise drinking starts (while
     * thirst is on and the bucket has a drink). Clicks aimed at a block pass, so Crafting pours the water there.
     */
    public static InteractionResult tryUseAtAir(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!isWaterBucket(held) || !isAimedAtAir(level, player)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            ItemStack poured = pourOverSelf(level, player, held);
            player.setItemInHand(hand, poured);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!ThirstDrain.isOn() || drinksLeft(held) <= 0) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    /** One drink taken after drinking finishes (thirst comes through Vitality's finished-using hook, as for every drink). */
    public static ItemStack finishDrink(ItemStack bucket, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return bucket;
        }
        int drinksLeft = drinksLeft(bucket) - 1;
        serverPlayer.awardStat(Stats.ITEM_USED.get(bucket.getItem()));
        LOG.debug(VitalityLogCategories.DRINK, "wooden bucket drink", "player", serverPlayer.getScoreboardName(),
                "purity", purity(bucket).serializedName, "drinksLeft", drinksLeft);
        if (drinksLeft <= 0) {
            return emptied(bucket);
        }
        bucket.getOrCreateTag().putInt(DRINKS_KEY, drinksLeft);
        return bucket;
    }

    /** Sneak-use at the air: the whole bucket over yourself, to cool off. It comes back empty. */
    public static ItemStack pourOverSelf(Level level, Player player, ItemStack bucket) {
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (!(level instanceof ServerLevel serverLevel)) {
            return bucket;
        }
        VitalityState.of(player).temperature.setWetTicks(VitalityServerConfig.get().temperature.wetTicks.get());
        serverLevel.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getEyeY(), player.getZ(), POUR_SPLASH_PARTICLES, 0.4, 0.4, 0.4, 0.1);
        LOG.debug(VitalityLogCategories.DRINK, "poured over self", "player", player.getScoreboardName(), "item", WATER_BUCKET);
        return emptied(bucket);
    }

    /** Crafting's empty Wooden Bucket with only the bucket's uses: the water's data leaves with the water. */
    public static ItemStack emptied(ItemStack bucket) {
        Optional<Item> emptyBucket = ResourceIds.item(EMPTY_BUCKET);
        if (emptyBucket.isEmpty()) {
            return bucket;
        }
        ItemStack empty = new ItemStack(emptyBucket.get());
        int spent = UsesDurability.usesSpent(bucket);
        if (spent > 0) {
            empty.getOrCreateTag().putInt(UsesDurability.USES_KEY, spent);
        }
        return empty;
    }
}
