package com.impact.realisticfood.stoneboiling;

import com.impact.lib.ResourceIds;
import com.impact.lib.item.UsesDurability;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.BoilingPot;
import com.impact.realisticfood.boiling.PotBlockEntity;
import com.impact.realisticfood.boiling.SoupState;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.item.HotPebbleItem;
import com.impact.realisticfood.network.FoodNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Crafting's Water-filled Wooden Bucket set down for stone boiling. It keeps the bucket that was set down (its uses and
 * every other mod's NBT), its pot, how many boils its water has left, the heat its Hot Pebbles still give, and those
 * pebbles, which come back out cooled once their heat is spent. Each Hot Pebble costs the bucket 1 use.
 */
public final class StoneBoilingPotBlockEntity extends BlockEntity implements PotBlockEntity {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final String BUCKET_KEY = "Bucket";
    private static final String BOILS_LEFT_KEY = "BoilsLeft";
    private static final String HAS_BOILED_KEY = "HasBoiled";
    private static final String HEAT_TICKS_LEFT_KEY = "HeatTicksLeft";
    private static final String PEBBLES_KEY = "Pebbles";
    private static final String POT_KEY = "Pot";
    private static final int STATE_UPDATE_TICKS = 20;

    private ItemStack bucket = ItemStack.EMPTY;
    private int boilsLeft;
    private boolean hasBoiled;
    private int heatTicksLeft;
    private final List<ItemStack> pebbles = new ArrayList<>();
    private BoilingPot pot = new BoilingPot();

    public StoneBoilingPotBlockEntity(BlockPos position, BlockState state) {
        super(StoneBoilingPots.blockEntityType(), position, state);
        boilsLeft = FoodServerConfig.get().stoneBoiling.potBoils.get();
    }

    @Override
    public BoilingPot pot() {
        return pot;
    }

    public int boilsLeft() {
        return boilsLeft;
    }

    public boolean hasWater() {
        return boilsLeft > 0;
    }

    public boolean hasBoiled() {
        return hasBoiled;
    }

    public int heatTicksLeft() {
        return heatTicksLeft;
    }

    public List<ItemStack> pebbles() {
        return List.copyOf(pebbles);
    }

    /** Sets down the bucket from the player's hand. */
    void holdBucket(ItemStack waterBucket) {
        bucket = waterBucket.copyWithCount(1);
        boilsLeft = FoodServerConfig.get().stoneBoiling.potBoils.get();
        setChanged();
    }

    /**
     * The bucket to give back, with its uses and NBT: still the Water-filled Wooden Bucket if any water is left, or
     * Crafting's empty Wooden Bucket if not.
     */
    public ItemStack bucketToReturn() {
        if (hasWater() || bucket.isEmpty()) {
            return bucket.copy();
        }
        Optional<Item> emptyBucket = ResourceIds.item(StoneBoilingPots.CRAFTING_WOODEN_BUCKET);
        if (emptyBucket.isEmpty()) {
            return bucket.copy();
        }
        ItemStack returned = new ItemStack(emptyBucket.get());
        if (bucket.hasTag()) {
            returned.setTag(bucket.getTag().copy());
        }
        return returned;
    }

    /** What happened to a Hot Pebble dropped in. */
    enum PebbleResult {
        BOILING, BUCKET_BROKE, NO_WATER
    }

    /** Drops a Hot Pebble in: the water boils for the pebble's heat, and the bucket spends 1 use. */
    PebbleResult addHotPebble(ServerLevel level, ItemStack hotPebble) {
        if (!hasWater()) {
            return PebbleResult.NO_WATER;
        }
        pebbles.add(HotPebbleItem.cooled(hotPebble));
        heatTicksLeft += FoodServerConfig.get().stoneBoiling.boilTicks.get();
        StonePotHost host = new StonePotHost(level, worldPosition, this);
        pot.boilNow(host);
        boolean isWornOut = UsesDurability.use(bucket, 1);
        setChanged();
        LOG.debug(FoodLogCategories.STONE_BOILING, "pebble dropped in", "pos", worldPosition, "heatTicksLeft", heatTicksLeft,
                "bucketUsesLeft", UsesDurability.get(bucket), "broke", isWornOut);
        if (isWornOut) {
            bucket = ItemStack.EMPTY;
            return PebbleResult.BUCKET_BROKE;
        }
        return PebbleResult.BOILING;
    }

    /** Takes the bucket, the pebbles and everything in the pot out, so breaking the block afterwards drops nothing twice. */
    List<ItemStack> emptyOut() {
        List<ItemStack> contents = Stream.concat(pot.takeAllInputs().stream(), pebbles.stream()).filter(stack -> !stack.isEmpty()).toList();
        pebbles.clear();
        bucket = ItemStack.EMPTY;
        heatTicksLeft = 0;
        setChanged();
        return contents;
    }

    /** Everything the block drops when it's broken or pops off: the bucket (unless it broke), the pebbles and the unboiled items. */
    List<ItemStack> drops() {
        ItemStack returned = bucketToReturn();
        List<ItemStack> contents = emptyOut();
        return Stream.concat(Stream.of(returned).filter(stack -> !stack.isEmpty()), contents.stream()).toList();
    }

    /** One boil's worth of water used. */
    void useOneBoil(ServerLevel level) {
        if (!hasWater()) {
            return;
        }
        boilsLeft--;
        setChanged();
        LOG.debug(FoodLogCategories.STONE_BOILING, "boil used", "pos", worldPosition, "boilsLeft", boilsLeft);
        if (!hasWater()) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(StoneBoilingPotBlock.HAS_WATER, false));
        }
    }

    static void serverTick(Level level, BlockPos position, BlockState state, StoneBoilingPotBlockEntity potEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        StonePotHost host = new StonePotHost(serverLevel, position, potEntity);
        potEntity.coolPebbles(host);
        potEntity.pot.tick(host);
        potEntity.hasBoiled |= potEntity.pot.isBoiling();
        potEntity.setChanged();
        boolean isBusy = potEntity.pot.isBoiling() || potEntity.pot.holdsSoup();
        if (isBusy && level.getGameTime() % STATE_UPDATE_TICKS == 0) {
            FoodNetwork.sendPotState(serverLevel, position, host.surfaceCenter(), potEntity.pot.isBoiling(), SoupState.of(potEntity.pot));
        }
    }

    // Once their heat is spent, the pebbles come back out, cooled.
    private void coolPebbles(StonePotHost host) {
        if (heatTicksLeft <= 0) {
            return;
        }
        heatTicksLeft--;
        if (heatTicksLeft > 0) {
            return;
        }
        LOG.debug(FoodLogCategories.STONE_BOILING, "pebbles spent", "pos", worldPosition, "pebbles", pebbles.size());
        pebbles.stream().filter(pebble -> !pebble.isEmpty()).forEach(host::release);
        pebbles.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(BUCKET_KEY, bucket.save(new CompoundTag()));
        tag.putInt(BOILS_LEFT_KEY, boilsLeft);
        tag.putBoolean(HAS_BOILED_KEY, hasBoiled);
        tag.putInt(HEAT_TICKS_LEFT_KEY, heatTicksLeft);
        ListTag pebbleList = new ListTag();
        pebbles.forEach(pebble -> pebbleList.add(pebble.save(new CompoundTag())));
        tag.put(PEBBLES_KEY, pebbleList);
        tag.put(POT_KEY, pot.save());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        bucket = ItemStack.of(tag.getCompound(BUCKET_KEY));
        boilsLeft = tag.getInt(BOILS_LEFT_KEY);
        hasBoiled = tag.getBoolean(HAS_BOILED_KEY);
        heatTicksLeft = tag.getInt(HEAT_TICKS_LEFT_KEY);
        pebbles.clear();
        tag.getList(PEBBLES_KEY, Tag.TAG_COMPOUND).forEach(pebbleTag -> pebbles.add(ItemStack.of((CompoundTag) pebbleTag)));
        pot = BoilingPot.load(tag.getCompound(POT_KEY));
    }
}
