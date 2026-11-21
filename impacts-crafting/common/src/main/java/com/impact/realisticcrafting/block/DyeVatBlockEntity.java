package com.impact.realisticcrafting.block;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * The Dye Vat's contents: water or not, the color dissolved in it (none for plain water) and how many charges of that
 * color are left. Synced to clients, which tint the water. An empty vat under the rain slowly fills.
 */
public final class DyeVatBlockEntity extends BlockEntity {
    private static final String WATER_KEY = "Water";
    private static final String COLOR_KEY = "Color";
    private static final String CHARGES_KEY = "Charges";
    private static final String RAIN_TICKS_KEY = "RainTicks";
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);

    private boolean hasWater;
    private DyeColor color;
    private int charges;
    private int rainTicks;

    public DyeVatBlockEntity(BlockPos position, BlockState state) {
        super(CraftingBlocks.dyeVatEntity(), position, state);
    }

    static void serverTick(Level level, BlockPos position, BlockState state, DyeVatBlockEntity vat) {
        vat.collectRain(level.isRainingAt(position.above()));
    }

    public boolean hasWater() {
        return hasWater;
    }

    public Optional<DyeColor> color() {
        return Optional.ofNullable(color);
    }

    public int charges() {
        return charges;
    }

    /** One tick of weather: rain on an empty vat fills it after a while. */
    public void collectRain(boolean isRainingOnIt) {
        if (hasWater || !isRainingOnIt) {
            return;
        }
        rainTicks++;
        if (rainTicks >= CraftingServerConfig.get().dyeVat.rainFillTicks.get()) {
            fillWithWater();
            LOG.debug(CraftingLogCategories.VAT, "filled by rain", "pos", worldPosition.toShortString());
        }
    }

    public void fillWithWater() {
        hasWater = true;
        rainTicks = 0;
        onContentsChanged();
    }

    public void empty() {
        hasWater = false;
        color = null;
        charges = 0;
        onContentsChanged();
    }

    /** Adds a dye's charges: the same color tops up, a mixing color changes the vat's color. Returns false when refused. */
    public boolean addDye(DyeColor added, Optional<DyeColor> mixedColor) {
        CraftingServerConfig.DyeVatSettings settings = CraftingServerConfig.get().dyeVat;
        if (!hasWater || charges >= settings.capacity.get()) {
            return false;
        }
        DyeColor newColor = color == null || color == added ? added : mixedColor.orElse(null);
        if (newColor == null) {
            return false;
        }
        color = newColor;
        charges = Math.min(settings.capacity.get(), charges + settings.chargesPerDye.get());
        onContentsChanged();
        return true;
    }

    /** Spends charges on a dipped item; plain water costs nothing. A vat that runs out of dye is plain water again. */
    public void spendCharges(int spent) {
        if (color == null) {
            return;
        }
        charges = Math.max(0, charges - spent);
        if (charges == 0) {
            color = null;
        }
        onContentsChanged();
    }

    private void onContentsChanged() {
        setChanged();
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        if (state.getValue(DyeVatBlock.FILLED) != hasWater) {
            level.setBlock(worldPosition, state.setValue(DyeVatBlock.FILLED, hasWater), Block.UPDATE_ALL);
        }
        level.sendBlockUpdated(worldPosition, state, getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(WATER_KEY, hasWater);
        if (color != null) {
            tag.putString(COLOR_KEY, color.getName());
        }
        tag.putInt(CHARGES_KEY, charges);
        tag.putInt(RAIN_TICKS_KEY, rainTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        hasWater = tag.getBoolean(WATER_KEY);
        color = tag.contains(COLOR_KEY) ? DyeColor.byName(tag.getString(COLOR_KEY), null) : null;
        charges = tag.getInt(CHARGES_KEY);
        rainTicks = tag.getInt(RAIN_TICKS_KEY);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
