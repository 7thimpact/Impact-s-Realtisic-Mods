package com.impact.lib.heat;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Heat and cold sources, shared by Food (boiling, Hot Pebbles) and Vitality (temperature, the Copper Still). A block in
 * {@code #impactslib:heat_sources} heats only while lit, when it has a lit state (campfires, furnaces).
 */
public final class HeatSources {
    public static final TagKey<Block> HEAT_SOURCES = TagKey.create(Registries.BLOCK, ResourceIds.of(ImpactsLib.MOD_ID, "heat_sources"));
    public static final TagKey<Block> COLD_SOURCES = TagKey.create(Registries.BLOCK, ResourceIds.of(ImpactsLib.MOD_ID, "cold_sources"));

    private HeatSources() {
    }

    /** Whether the block at the position gives heat right now: a heat source, and lit if it can be unlit. */
    public static boolean isHeated(Level level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.is(HEAT_SOURCES) && isLitIfLightable(state);
    }

    public static boolean isCold(Level level, BlockPos position) {
        return level.getBlockState(position).is(COLD_SOURCES);
    }

    private static boolean isLitIfLightable(BlockState state) {
        return !state.hasProperty(BlockStateProperties.LIT) || state.getValue(BlockStateProperties.LIT);
    }
}
