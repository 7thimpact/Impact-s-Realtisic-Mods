package com.impact.realisticcrafting.registry;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.LoosePebbleBlock;
import com.impact.realisticcrafting.block.MakeshiftStoneTableBlock;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.block.MakeshiftTableBlock;
import com.impact.realisticcrafting.block.MakeshiftTableBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/** Crafting's blocks. */
public final class CraftingBlocks {
    private static final float WOODEN_TABLE_STRENGTH = 2.5F;
    private static final float STONE_TABLE_STRENGTH = 3.0F;
    private static final Map<MakeshiftTable, Supplier<? extends MakeshiftTableBlock>> TABLES = new EnumMap<>(MakeshiftTable.class);
    private static volatile Supplier<LoosePebbleBlock> loosePebble;
    private static volatile Supplier<BlockEntityType<MakeshiftTableBlockEntity>> tableEntity;

    private CraftingBlocks() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(CraftingMod.MOD_ID);
        loosePebble = registrar.register(Registries.BLOCK, "loose_pebble", () -> new LoosePebbleBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE).noCollission().instabreak().sound(SoundType.STONE).offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY)));
        TABLES.put(MakeshiftTable.WOODEN, registrar.register(Registries.BLOCK, MakeshiftTable.WOODEN.path, () -> new MakeshiftTableBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(WOODEN_TABLE_STRENGTH).sound(SoundType.WOOD).noOcclusion()
                        .ignitedByLava())));
        TABLES.put(MakeshiftTable.STONE, registrar.register(Registries.BLOCK, MakeshiftTable.STONE.path, () -> new MakeshiftStoneTableBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(STONE_TABLE_STRENGTH).sound(SoundType.STONE).noOcclusion())));
        tableEntity = registrar.register(Registries.BLOCK_ENTITY_TYPE, "makeshift_table", () -> LoaderPlatform.current()
                .blockEntityType(MakeshiftTableBlockEntity::new, table(MakeshiftTable.WOODEN), table(MakeshiftTable.STONE)));
    }

    /** Natural pebble clusters. There's no block item: players can't place them. */
    public static LoosePebbleBlock loosePebble() {
        return loosePebble.get();
    }

    public static MakeshiftTableBlock table(MakeshiftTable table) {
        return TABLES.get(table).get();
    }

    public static BlockEntityType<MakeshiftTableBlockEntity> tableEntity() {
        return tableEntity.get();
    }
}
