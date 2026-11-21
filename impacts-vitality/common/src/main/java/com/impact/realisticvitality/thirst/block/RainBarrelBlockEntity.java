package com.impact.realisticvitality.thirst.block;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.registry.VitalityBlocks;
import com.impact.realisticvitality.thirst.WaterContainers;
import com.impact.realisticvitality.thirst.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The Rain Barrel's water: 1 drink per minute of rain falling on it, up to 16, purified unless the config says rain is unclean. */
public final class RainBarrelBlockEntity extends BlockEntity {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final String TANK_KEY = "Tank";
    private static final String RAIN_TICKS_KEY = "RainTicks";
    private static final int COMPARATOR_MAXIMUM = 15;
    private static final int FILL_STEPS = 4;

    private final WaterTank tank = new WaterTank();
    private int rainTicks;

    public RainBarrelBlockEntity(BlockPos position, BlockState state) {
        super(VitalityBlocks.rainBarrelEntity(), position, state);
    }

    static void serverTick(Level level, BlockPos position, BlockState state, RainBarrelBlockEntity barrel) {
        barrel.collectRain(level.isRainingAt(position.above()));
    }

    /** One tick of weather. Only rain that reaches the barrel (open sky, a biome where it rains) counts. */
    public void collectRain(boolean isRainingOnIt) {
        VitalityServerConfig settings = VitalityServerConfig.get();
        if (!isRainingOnIt || tank.drinks() >= settings.waterBlocks.rainBarrelDrinks.get()) {
            return;
        }
        rainTicks++;
        if (rainTicks < settings.waterBlocks.rainBarrelTicksPerDrink.get()) {
            return;
        }
        rainTicks = 0;
        tank.add(1, settings.purity.isRainClean.get() ? WaterPurity.PURIFIED : WaterPurity.UNCLEAN);
        onTankChanged();
        LOG.debug(VitalityLogCategories.DRINK, "rain collected", "position", worldPosition.toShortString(), "drinks", tank.drinks(),
                "purity", tank.purity().serializedName);
    }

    public WaterTank tank() {
        return tank;
    }

    boolean canFill(ItemStack held) {
        return held.is(Items.GLASS_BOTTLE) || WaterContainers.containerOf(held).isPresent();
    }

    boolean fillHeld(Player player, InteractionHand hand) {
        int taken = WaterBlockInteractions.takeInto(level, worldPosition, player, hand, tank);
        if (taken <= 0) {
            return false;
        }
        onTankChanged();
        LOG.debug(VitalityLogCategories.DRINK, "taken from rain barrel", "player", player.getScoreboardName(), "drinks", taken, "left", tank.drinks());
        return true;
    }

    int comparatorSignal() {
        return tank.drinks() * COMPARATOR_MAXIMUM / VitalityServerConfig.get().waterBlocks.rainBarrelDrinks.get();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TANK_KEY, tank.save());
        tag.putInt(RAIN_TICKS_KEY, rainTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.load(tag.getCompound(TANK_KEY));
        rainTicks = tag.getInt(RAIN_TICKS_KEY);
    }

    private void onTankChanged() {
        setChanged();
        if (level == null) {
            return;
        }
        int maximum = VitalityServerConfig.get().waterBlocks.rainBarrelDrinks.get();
        int fill = (tank.drinks() * FILL_STEPS + maximum - 1) / maximum;
        BlockState state = getBlockState();
        if (state.getValue(RainBarrelBlock.FILL) != fill) {
            level.setBlockAndUpdate(worldPosition, state.setValue(RainBarrelBlock.FILL, fill));
        }
        level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
    }
}
