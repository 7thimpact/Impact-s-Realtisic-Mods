package com.impact.realisticfood.boiling;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.network.FoodNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Every water cauldron in a dimension that is heating, boiling or holding items. Vanilla cauldrons have no block
 * entity, so their pots live here, saved with the dimension, and are ticked by the dimension's tick.
 */
public final class CauldronPots extends SavedData {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final String DATA_NAME = FoodMod.MOD_ID + "_cauldron_pots";
    private static final String POTS_KEY = "Pots";
    private static final String POSITION_KEY = "Pos";
    private static final String POT_KEY = "Pot";
    private static final int STATE_UPDATE_TICKS = 20;

    private final Map<BlockPos, BoilingPot> potsByPosition = new HashMap<>();

    public static CauldronPots of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(CauldronPots::load, CauldronPots::new, DATA_NAME);
    }

    public static boolean isBoiling(ServerLevel level, BlockPos position) {
        return of(level).pot(position).map(BoilingPot::isBoiling).orElse(false);
    }

    public Optional<BoilingPot> pot(BlockPos position) {
        return Optional.ofNullable(potsByPosition.get(position));
    }

    /** The cauldron's pot, starting one if it has none. */
    public BoilingPot track(BlockPos position) {
        setDirty();
        return potsByPosition.computeIfAbsent(position.immutable(), newPosition -> new BoilingPot());
    }

    /** Called every server tick for each dimension. */
    public static void tickAll(ServerLevel level) {
        CauldronPots pots = of(level);
        if (pots.potsByPosition.isEmpty()) {
            return;
        }
        boolean isStateUpdateTick = level.getGameTime() % STATE_UPDATE_TICKS == 0;
        // A pot can boil dry or be emptied mid-tick, which removes it, so walk over a copy.
        for (BlockPos position : List.copyOf(pots.potsByPosition.keySet())) {
            pots.tickPot(level, position, isStateUpdateTick);
        }
        pots.setDirty();
    }

    /** Ticks one cauldron's pot. GameTests call this directly. */
    public void tickPot(ServerLevel level, BlockPos position, boolean isStateUpdateTick) {
        BoilingPot pot = potsByPosition.get(position);
        if (pot == null || !level.isLoaded(position)) {
            return;
        }
        if (!CauldronPotHost.isWaterCauldron(level.getBlockState(position))) {
            dropContents(level, position);
            return;
        }
        CauldronPotHost host = new CauldronPotHost(level, position);
        pot.tick(host);
        boolean hasBoiledDry = !potsByPosition.containsKey(position);
        if (hasBoiledDry) {
            return;
        }
        if (pot.isIdle(host)) {
            potsByPosition.remove(position);
            return;
        }
        if (isStateUpdateTick && (pot.isBoiling() || pot.holdsSoup())) {
            FoodNetwork.sendPotState(level, position, host.surfaceCenter(), pot.isBoiling(), SoupState.of(pot));
        }
    }

    /** A block changed at a water cauldron's spot: start a pot when it has heat under it. */
    public static void onWaterCauldronUpdated(ServerLevel level, BlockPos position) {
        if (new CauldronPotHost(level, position).isHeated()) {
            of(level).track(position);
        }
    }

    /** A water cauldron is gone (emptied, boiled dry or broken): its unboiled items drop out. */
    public static void onWaterCauldronRemoved(ServerLevel level, BlockPos position) {
        CauldronPots pots = of(level);
        if (pots.potsByPosition.containsKey(position)) {
            pots.dropContents(level, position);
        }
    }

    private void dropContents(ServerLevel level, BlockPos position) {
        BoilingPot pot = potsByPosition.remove(position);
        setDirty();
        if (pot == null) {
            return;
        }
        List<ItemStack> inputs = pot.takeAllInputs();
        inputs.forEach(input -> Block.popResource(level, position, input));
        if (!inputs.isEmpty()) {
            LOG.debug(FoodLogCategories.EVAPORATION, "items dropped out", "pos", position,
                    "items", inputs.stream().map(input -> ResourceIds.itemId(input.getItem()).toString()).toList());
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag potList = new ListTag();
        potsByPosition.forEach((position, pot) -> {
            CompoundTag entry = new CompoundTag();
            entry.put(POSITION_KEY, NbtUtils.writeBlockPos(position));
            entry.put(POT_KEY, pot.save());
            potList.add(entry);
        });
        tag.put(POTS_KEY, potList);
        return tag;
    }

    private static CauldronPots load(CompoundTag tag) {
        CauldronPots pots = new CauldronPots();
        for (Tag entryTag : tag.getList(POTS_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) entryTag;
            pots.potsByPosition.put(NbtUtils.readBlockPos(entry.getCompound(POSITION_KEY)), BoilingPot.load(entry.getCompound(POT_KEY)));
        }
        return pots;
    }

    /** Whether the state is a water cauldron, for the block hooks. */
    public static boolean isWaterCauldron(BlockState state) {
        return CauldronPotHost.isWaterCauldron(state);
    }
}
