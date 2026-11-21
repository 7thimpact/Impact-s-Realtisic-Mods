package com.impact.realisticvitality.air.block;

import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.AirTank;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.item.AirTankItem;
import com.impact.realisticvitality.registry.VitalityBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The Air Compressor's tank and fuel: while it burns, the tank fills from empty in {@code breathing.compressorRefillTicks}. */
public final class AirCompressorBlockEntity extends BlockEntity {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final String TANK_KEY = "Tank";
    private static final String BURN_TICKS_KEY = "BurnTicks";

    private ItemStack tank = ItemStack.EMPTY;
    private int burnTicks;

    public AirCompressorBlockEntity(BlockPos position, BlockState state) {
        super(VitalityBlocks.airCompressorEntity(), position, state);
    }

    static void serverTick(Level level, BlockPos position, BlockState state, AirCompressorBlockEntity compressor) {
        compressor.tickCompressor();
    }

    /** One tick: fills the tank while fuel burns. Fuel only burns while there's a tank to fill. */
    public void tickCompressor() {
        boolean isWorking = burnTicks > 0 && tank.getItem() instanceof AirTankItem tankItem && tankItem.tank.airIn(tank) < tankItem.tank.capacity();
        showLit(isWorking);
        if (!isWorking) {
            return;
        }
        burnTicks--;
        AirTank kind = ((AirTankItem) tank.getItem()).tank;
        int perTick = Math.max(1, kind.capacity() / VitalityServerConfig.get().breathing.compressorRefillTicks.get());
        kind.setAir(tank, kind.airIn(tank) + perTick);
        setChanged();
        if (kind.airIn(tank) >= kind.capacity()) {
            LOG.debug(VitalityLogCategories.GEAR, "tank refilled", "by", "air_compressor", "tank", kind.itemPath, "position", worldPosition.toShortString());
        }
    }

    public ItemStack tank() {
        return tank;
    }

    public int burnTicks() {
        return burnTicks;
    }

    /** A click: put a tank in, feed it fuel, or take the tank out with an empty hand. The server decides; the client guesses. */
    public boolean use(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean isTank = held.getItem() instanceof AirTankItem;
        int fuelTicks = LoaderPlatform.current().burnTicks(held);
        boolean isTakingTank = held.isEmpty() && !tank.isEmpty();
        if (level == null || level.isClientSide) {
            return isTank && tank.isEmpty() || fuelTicks > 0 || isTakingTank;
        }
        if (isTank && tank.isEmpty()) {
            tank = held.split(1);
            playSound();
            setChanged();
            return true;
        }
        if (fuelTicks > 0) {
            burnTicks += fuelTicks;
            held.shrink(1);
            setChanged();
            return true;
        }
        if (isTakingTank) {
            player.setItemInHand(hand, takeTank());
            playSound();
            return true;
        }
        return false;
    }

    ItemStack takeTank() {
        ItemStack taken = tank;
        tank = ItemStack.EMPTY;
        setChanged();
        return taken;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TANK_KEY, tank.save(new CompoundTag()));
        tag.putInt(BURN_TICKS_KEY, burnTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank = ItemStack.of(tag.getCompound(TANK_KEY));
        burnTicks = tag.getInt(BURN_TICKS_KEY);
    }

    private void showLit(boolean isLit) {
        BlockState state = getBlockState();
        if (level != null && state.getValue(AirCompressorBlock.LIT) != isLit) {
            level.setBlockAndUpdate(worldPosition, state.setValue(AirCompressorBlock.LIT, isLit));
        }
    }

    private void playSound() {
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.ARMOR_EQUIP_IRON, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}
