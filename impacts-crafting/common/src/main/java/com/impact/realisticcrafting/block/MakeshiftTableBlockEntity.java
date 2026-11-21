package com.impact.realisticcrafting.block;

import com.impact.lib.item.UsesDurability;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** A makeshift table's exact uses spent. Saved as {@code uses} like Lib's uses-tracked items, and left out while unused. */
public final class MakeshiftTableBlockEntity extends BlockEntity {
    private static final float CREAK_VOLUME = 0.6F;
    private static final float CREAK_PITCH = 0.7F;
    private int usesSpent;

    public MakeshiftTableBlockEntity(BlockPos position, BlockState state) {
        super(CraftingBlocks.tableEntity(), position, state);
    }

    public MakeshiftTable table() {
        return getBlockState().getBlock() instanceof MakeshiftTableBlock tableBlock ? tableBlock.table() : MakeshiftTable.WOODEN;
    }

    public int usesSpent() {
        return usesSpent;
    }

    public int usesLeft() {
        return Math.max(0, table().maxUses() - usesSpent);
    }

    public void setUsesSpent(int usesSpent) {
        this.usesSpent = Math.max(0, usesSpent);
        setChanged();
        showWearStage();
    }

    /** Spends one use. Returns true when the table is worn out and falls apart. */
    public boolean useOnce() {
        setUsesSpent(usesSpent + 1);
        return usesLeft() <= 0;
    }

    private void showWearStage() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        int stage = TableWear.stageFor(usesLeft(), table().maxUses());
        if (state.hasProperty(MakeshiftTableBlock.WEAR) && state.getValue(MakeshiftTableBlock.WEAR) != stage) {
            level.setBlock(worldPosition, state.setValue(MakeshiftTableBlock.WEAR, stage), Block.UPDATE_ALL);
            level.playSound(null, worldPosition, CraftingSounds.tableCreak(), SoundSource.BLOCKS, CREAK_VOLUME, CREAK_PITCH);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (usesSpent > 0) {
            tag.putInt(UsesDurability.USES_KEY, usesSpent);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        usesSpent = tag.getInt(UsesDurability.USES_KEY);
    }
}
