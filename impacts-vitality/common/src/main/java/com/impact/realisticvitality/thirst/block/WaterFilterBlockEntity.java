package com.impact.realisticvitality.thirst.block;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.item.CharcoalFilterItem;
import com.impact.realisticvitality.registry.VitalityBlocks;
import com.impact.realisticvitality.thirst.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Water Filter's water: what was poured in, and the purified water dripped out of it, 1 drink every 30 seconds while
 * a Charcoal Filter is inside. Each drink wears the filter; a worn-out filter crumbles.
 */
public final class WaterFilterBlockEntity extends BlockEntity {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final String INPUT_KEY = "Input";
    private static final String OUTPUT_KEY = "Output";
    private static final String FILTER_KEY = "Filter";
    private static final String DRIP_TICKS_KEY = "DripTicks";

    private final WaterTank input = new WaterTank();
    private final WaterTank output = new WaterTank();
    private ItemStack charcoalFilter = ItemStack.EMPTY;
    private int dripTicks;

    public WaterFilterBlockEntity(BlockPos position, BlockState state) {
        super(VitalityBlocks.waterFilterEntity(), position, state);
    }

    static void serverTick(Level level, BlockPos position, BlockState state, WaterFilterBlockEntity filter) {
        filter.tickFiltering();
    }

    /** One tick: drips a drink through the filter when enough time has passed, and shows whether it's working. */
    public void tickFiltering() {
        VitalityServerConfig.WaterBlockSettings settings = VitalityServerConfig.get().waterBlocks;
        boolean canFilter = !charcoalFilter.isEmpty() && !input.isEmpty() && output.drinks() < settings.waterFilterDrinks.get();
        showFiltering(canFilter);
        if (!canFilter) {
            return;
        }
        dripTicks++;
        if (dripTicks < settings.waterFilterTicksPerDrink.get()) {
            return;
        }
        dripTicks = 0;
        WaterPurity pouredPurity = input.purity();
        input.take(1);
        output.add(1, WaterPurity.PURIFIED);
        wearFilter(settings.waterFilterWearPerDrink.get());
        setChanged();
        LOG.debug(VitalityLogCategories.PURITY, "water filtered", "position", worldPosition.toShortString(), "from", pouredPurity.serializedName,
                "waiting", input.drinks(), "purified", output.drinks());
    }

    public WaterTank input() {
        return input;
    }

    public WaterTank output() {
        return output;
    }

    public ItemStack charcoalFilter() {
        return charcoalFilter;
    }

    /**
     * A click with the held item: insert or remove the filter, take purified water out, or pour water in. The client
     * only guesses from the held item; the server decides.
     */
    public boolean use(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level == null || level.isClientSide) {
            return WaterBlockInteractions.isWaterHandling(held) || held.getItem() instanceof CharcoalFilterItem
                    || held.isEmpty() && player.isSecondaryUseActive();
        }
        if (held.getItem() instanceof CharcoalFilterItem && charcoalFilter.isEmpty()) {
            charcoalFilter = held.split(1);
            playFilterSound(SoundEvents.ARMOR_EQUIP_LEATHER);
            setChanged();
            return true;
        }
        if (held.isEmpty() && player.isSecondaryUseActive() && !charcoalFilter.isEmpty()) {
            player.setItemInHand(hand, removeCharcoalFilter());
            playFilterSound(SoundEvents.ARMOR_EQUIP_LEATHER);
            return true;
        }
        int moved = WaterBlockInteractions.takeInto(level, worldPosition, player, hand, output);
        if (moved == 0) {
            moved = WaterBlockInteractions.pourFrom(level, worldPosition, player, hand, input, VitalityServerConfig.get().waterBlocks.waterFilterDrinks.get());
        }
        if (moved > 0) {
            setChanged();
        }
        return moved > 0;
    }

    ItemStack removeCharcoalFilter() {
        ItemStack removed = charcoalFilter;
        charcoalFilter = ItemStack.EMPTY;
        setChanged();
        return removed;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(INPUT_KEY, input.save());
        tag.put(OUTPUT_KEY, output.save());
        tag.put(FILTER_KEY, charcoalFilter.save(new CompoundTag()));
        tag.putInt(DRIP_TICKS_KEY, dripTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        input.load(tag.getCompound(INPUT_KEY));
        output.load(tag.getCompound(OUTPUT_KEY));
        charcoalFilter = ItemStack.of(tag.getCompound(FILTER_KEY));
        dripTicks = tag.getInt(DRIP_TICKS_KEY);
    }

    private void wearFilter(int wear) {
        charcoalFilter.setDamageValue(charcoalFilter.getDamageValue() + wear);
        if (charcoalFilter.getDamageValue() < charcoalFilter.getMaxDamage()) {
            return;
        }
        charcoalFilter = ItemStack.EMPTY;
        playFilterSound(SoundEvents.ITEM_BREAK);
        LOG.debug(VitalityLogCategories.GEAR, "filter worn out", "block", "water_filter", "position", worldPosition.toShortString());
    }

    private void showFiltering(boolean isFiltering) {
        BlockState state = getBlockState();
        if (level != null && state.getValue(WaterFilterBlock.FILTERING) != isFiltering) {
            level.setBlockAndUpdate(worldPosition, state.setValue(WaterFilterBlock.FILTERING, isFiltering));
        }
    }

    private void playFilterSound(SoundEvent sound) {
        if (level != null) {
            level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}
