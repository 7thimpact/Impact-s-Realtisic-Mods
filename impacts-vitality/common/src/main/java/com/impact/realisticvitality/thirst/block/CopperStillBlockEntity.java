package com.impact.realisticvitality.thirst.block;

import com.impact.lib.ImpactsLib;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.registry.VitalityBlocks;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.DrinkValues;
import com.impact.realisticvitality.thirst.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * The Copper Still's water and its four slots. Hoppers above or beside it fill the water slot (water buckets and bottles)
 * and the glass-bottle slot; hoppers below take the emptied buckets and bottles and the purified water.
 */
public final class CopperStillBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final int WATER_SLOT = 0;
    public static final int EMPTIED_SLOT = 1;
    public static final int GLASS_BOTTLE_SLOT = 2;
    public static final int PURIFIED_SLOT = 3;
    private static final int[] SLOTS_FED_FROM_ABOVE_AND_SIDES = {WATER_SLOT, GLASS_BOTTLE_SLOT};
    private static final int[] SLOTS_TAKEN_FROM_BELOW = {EMPTIED_SLOT, PURIFIED_SLOT};
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final String INPUT_KEY = "Input";
    private static final String OUTPUT_KEY = "Output";
    private static final String PROGRESS_KEY = "Progress";
    private static final int COMPARATOR_MAXIMUM = 15;

    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final WaterTank input = new WaterTank();
    private final WaterTank output = new WaterTank();
    private int progressTicks;

    public CopperStillBlockEntity(BlockPos position, BlockState state) {
        super(VitalityBlocks.copperStillEntity(), position, state);
    }

    static void serverTick(Level level, BlockPos position, BlockState state, CopperStillBlockEntity still) {
        still.tickStill(ImpactsLib.isHeated(level, position.below()));
    }

    /** One tick: takes in the water slot's water, distils when heated, and bottles purified water. */
    public void tickStill(boolean isHeated) {
        VitalityServerConfig.WaterBlockSettings settings = VitalityServerConfig.get().waterBlocks;
        drainWaterSlot(settings);
        boolean isWorking = isHeated && !input.isEmpty() && output.drinks() < settings.copperStillDrinks.get();
        showLit(isWorking);
        if (isWorking) {
            distil(settings);
        }
        fillGlassBottle();
    }

    public WaterTank input() {
        return input;
    }

    public WaterTank output() {
        return output;
    }

    /** A click by hand: take purified water out into a bottle or container, or pour water in. */
    public boolean use(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level == null || level.isClientSide) {
            return WaterBlockInteractions.isWaterHandling(held);
        }
        int moved = WaterBlockInteractions.takeInto(level, worldPosition, player, hand, output);
        if (moved == 0) {
            moved = WaterBlockInteractions.pourFrom(level, worldPosition, player, hand, input, VitalityServerConfig.get().waterBlocks.copperStillDrinks.get());
        }
        if (moved > 0) {
            setChanged();
        }
        return moved > 0;
    }

    int comparatorSignal() {
        return output.drinks() * COMPARATOR_MAXIMUM / VitalityServerConfig.get().waterBlocks.copperStillDrinks.get();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? SLOTS_TAKEN_FROM_BELOW : SLOTS_FED_FROM_ABOVE_AND_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == EMPTIED_SLOT || slot == PURIFIED_SLOT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case WATER_SLOT -> waterIn(stack).isPresent();
            case GLASS_BOTTLE_SLOT -> stack.is(Items.GLASS_BOTTLE);
            default -> false;
        };
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, count);
        setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.put(INPUT_KEY, input.save());
        tag.put(OUTPUT_KEY, output.save());
        tag.putInt(PROGRESS_KEY, progressTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        input.load(tag.getCompound(INPUT_KEY));
        output.load(tag.getCompound(OUTPUT_KEY));
        progressTicks = tag.getInt(PROGRESS_KEY);
    }

    // A water bucket is 4 drinks of unclean water; a bottle is 1 drink of its own purity.
    private static Optional<WaterIn> waterIn(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET)) {
            return Optional.of(new WaterIn(VitalityServerConfig.get().waterBlocks.bucketDrinks.get(), WaterPurity.UNCLEAN, new ItemStack(Items.BUCKET)));
        }
        Optional<WaterPurity> bottlePurity = VitalityItems.bottledWater(stack).map(water -> water.purity)
                .or(() -> DrinkValues.isVanillaWaterBottle(stack) ? Optional.of(WaterPurity.UNCLEAN) : Optional.empty());
        return bottlePurity.map(purity -> new WaterIn(1, purity, new ItemStack(Items.GLASS_BOTTLE)));
    }

    private void drainWaterSlot(VitalityServerConfig.WaterBlockSettings settings) {
        Optional<WaterIn> water = waterIn(items.get(WATER_SLOT));
        if (water.isEmpty() || input.drinks() + water.get().drinks() > settings.copperStillDrinks.get()) {
            return;
        }
        ItemStack emptied = items.get(EMPTIED_SLOT);
        boolean hasRoomForEmpty = emptied.isEmpty()
                || ItemStack.isSameItemSameTags(emptied, water.get().emptied()) && emptied.getCount() < emptied.getMaxStackSize();
        if (!hasRoomForEmpty) {
            return;
        }
        input.add(water.get().drinks(), water.get().purity());
        items.get(WATER_SLOT).shrink(1);
        if (emptied.isEmpty()) {
            items.set(EMPTIED_SLOT, water.get().emptied());
        } else {
            emptied.grow(1);
        }
        setChanged();
    }

    private void distil(VitalityServerConfig.WaterBlockSettings settings) {
        progressTicks++;
        int ticksPerDrink = Math.max(1, settings.copperStillTicksPerBucket.get() / settings.bucketDrinks.get());
        if (progressTicks < ticksPerDrink) {
            return;
        }
        progressTicks = 0;
        WaterPurity distilledPurity = input.purity();
        input.take(1);
        output.add(1, WaterPurity.PURIFIED);
        setChanged();
        LOG.debug(VitalityLogCategories.PURITY, "water distilled", "position", worldPosition.toShortString(), "from", distilledPurity.serializedName,
                "waiting", input.drinks(), "purified", output.drinks());
    }

    private void fillGlassBottle() {
        boolean canBottle = !output.isEmpty() && items.get(GLASS_BOTTLE_SLOT).is(Items.GLASS_BOTTLE) && items.get(PURIFIED_SLOT).isEmpty();
        if (!canBottle) {
            return;
        }
        items.get(GLASS_BOTTLE_SLOT).shrink(1);
        items.set(PURIFIED_SLOT, VitalityItems.bottle(BottledWater.filledWith(output.purity())));
        output.take(1);
        setChanged();
    }

    private void showLit(boolean isLit) {
        BlockState state = getBlockState();
        if (level != null && state.getValue(CopperStillBlock.LIT) != isLit) {
            level.setBlockAndUpdate(worldPosition, state.setValue(CopperStillBlock.LIT, isLit));
        }
    }

    private record WaterIn(int drinks, WaterPurity purity, ItemStack emptied) {
    }
}
