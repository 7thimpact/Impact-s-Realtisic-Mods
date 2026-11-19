package com.impact.realisticfood.bucket;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.BoilingPot;
import com.impact.realisticfood.boiling.SoupState;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.network.FoodNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.stream.Stream;

/**
 * A water bucket sitting on a campfire as a small pot. It keeps the bucket that was set down (with any other mod's
 * NBT), its pot, and how many boils its water has left; the bucket itself never wears down.
 */
public final class CampfireBucketBlockEntity extends BlockEntity {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final String BUCKET_KEY = "Bucket";
    private static final String BOILS_LEFT_KEY = "BoilsLeft";
    private static final String HAS_BOILED_KEY = "HasBoiled";
    private static final String POT_KEY = "Pot";
    private static final int STATE_UPDATE_TICKS = 20;

    private ItemStack bucket = new ItemStack(Items.WATER_BUCKET);
    private int boilsLeft;
    private boolean hasBoiled;
    private BoilingPot pot = new BoilingPot();

    public CampfireBucketBlockEntity(BlockPos position, BlockState state) {
        super(CampfireBuckets.blockEntityType(), position, state);
        boilsLeft = FoodServerConfig.get().boiling.bucketBoils.get();
    }

    public BoilingPot pot() {
        return pot;
    }

    public int boilsLeft() {
        return boilsLeft;
    }

    public boolean hasWater() {
        return boilsLeft > 0;
    }

    /** Sets down the bucket from the player's hand (one water bucket, NBT and all). */
    void holdBucket(ItemStack waterBucket) {
        bucket = waterBucket.copyWithCount(1);
        boilsLeft = FoodServerConfig.get().boiling.bucketBoils.get();
        setChanged();
    }

    /** The bucket to give back: still a water bucket if any water is left, or an empty bucket if not, with its NBT kept. */
    public ItemStack bucketToReturn() {
        ItemStack returned = new ItemStack(hasWater() ? Items.WATER_BUCKET : Items.BUCKET);
        if (bucket.hasTag()) {
            returned.setTag(bucket.getTag().copy());
        }
        return returned;
    }

    public boolean hasBoiled() {
        return hasBoiled;
    }

    /** Takes the bucket and everything in it out, so breaking the block afterwards drops nothing twice. */
    List<ItemStack> emptyOut() {
        List<ItemStack> contents = pot.takeAllInputs();
        bucket = ItemStack.EMPTY;
        setChanged();
        return contents;
    }

    /** Everything the block drops when it's broken or pops off: the bucket and the unboiled items. */
    List<ItemStack> drops() {
        if (bucket.isEmpty()) {
            return List.of();
        }
        ItemStack returned = bucketToReturn();
        List<ItemStack> contents = emptyOut();
        return Stream.concat(Stream.of(returned), contents.stream()).toList();
    }

    /** One boil's worth of water used. */
    void useOneBoil(ServerLevel level) {
        if (!hasWater()) {
            return;
        }
        boilsLeft--;
        setChanged();
        LOG.debug(FoodLogCategories.CAMPFIRE_BUCKET, "boil used", "pos", worldPosition, "boilsLeft", boilsLeft);
        if (hasWater()) {
            return;
        }
        // The pot sees its water gone on its next tick: it stops boiling and the items still in it pop out.
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(CampfireBucketBlock.HAS_WATER, false));
        LOG.debug(FoodLogCategories.CAMPFIRE_BUCKET, "water used up", "pos", worldPosition, "bucket", ResourceIds.itemId(bucket.getItem()));
    }

    static void serverTick(Level level, BlockPos position, BlockState state, CampfireBucketBlockEntity bucketEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!CampfireBuckets.isOnLitCampfire(level, position)) {
            CampfireBuckets.popOff(serverLevel, position, "campfire out");
            return;
        }
        BucketPotHost host = new BucketPotHost(serverLevel, position, bucketEntity);
        bucketEntity.pot.tick(host);
        bucketEntity.hasBoiled |= bucketEntity.pot.isBoiling();
        bucketEntity.setChanged();
        boolean isBusy = bucketEntity.pot.isBoiling() || bucketEntity.pot.holdsSoup();
        if (isBusy && level.getGameTime() % STATE_UPDATE_TICKS == 0) {
            FoodNetwork.sendPotState(serverLevel, position, host.surfaceCenter(), bucketEntity.pot.isBoiling(), SoupState.of(bucketEntity.pot));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(BUCKET_KEY, bucket.save(new CompoundTag()));
        tag.putInt(BOILS_LEFT_KEY, boilsLeft);
        tag.putBoolean(HAS_BOILED_KEY, hasBoiled);
        tag.put(POT_KEY, pot.save());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        bucket = ItemStack.of(tag.getCompound(BUCKET_KEY));
        boilsLeft = tag.getInt(BOILS_LEFT_KEY);
        hasBoiled = tag.getBoolean(HAS_BOILED_KEY);
        pot = BoilingPot.load(tag.getCompound(POT_KEY));
    }
}
