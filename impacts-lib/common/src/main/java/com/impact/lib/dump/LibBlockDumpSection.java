package com.impact.lib.dump;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/** Lib's part of the block dump: the block's ID, state, tags, block entity data, and whether it's heated or boiling. */
public final class LibBlockDumpSection implements BlockDumpSection {
    public static final String SECTION_NAME = "lib";
    private static final String NONE = "none";

    @Override
    public Map<String, Object> describe(ServerLevel level, BlockPos position, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(position);
        return ImpactsDump.entries(
                "block", ResourceIds.blockId(state.getBlock()),
                "position", position.toShortString(),
                "state", state.getValues().isEmpty() ? NONE : state.toString().substring(state.toString().indexOf('[')),
                "tags", state.getTags().map(tag -> "#" + tag.location()).toList(),
                "blockEntity", blockEntity == null ? NONE : blockEntity.saveWithoutMetadata(),
                "heated", ImpactsLib.isHeated(level, position),
                "boiling", ImpactsLib.isBoiling(level, position));
    }
}
