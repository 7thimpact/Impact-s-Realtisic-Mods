package com.impact.realisticcrafting.registry;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.bed.BedFrameBlock;
import com.impact.realisticcrafting.block.DyeVatBlock;
import com.impact.realisticcrafting.block.DyeVatBlockEntity;
import com.impact.realisticcrafting.block.LoosePebbleBlock;
import com.impact.realisticcrafting.block.MakeshiftStoneTableBlock;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.block.MakeshiftTableBlock;
import com.impact.realisticcrafting.block.MakeshiftTableBlockEntity;
import com.impact.realisticcrafting.item.PebbleKind;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Crafting's blocks. */
public final class CraftingBlocks {
    private static final float WOODEN_TABLE_STRENGTH = 2.5F;
    private static final float STONE_TABLE_STRENGTH = 3.0F;
    private static final Map<MakeshiftTable, Supplier<? extends MakeshiftTableBlock>> TABLES = new EnumMap<>(MakeshiftTable.class);
    private static final Map<PebbleKind, Supplier<LoosePebbleBlock>> LOOSE_PEBBLES = new EnumMap<>(PebbleKind.class);
    private static volatile Supplier<BlockEntityType<MakeshiftTableBlockEntity>> tableEntity;
    private static final float DYE_VAT_STRENGTH = 2.0F;
    private static volatile Supplier<DyeVatBlock> dyeVat;
    private static final float BED_FRAME_STRENGTH = 0.2F;
    private static volatile Supplier<BedFrameBlock> bedFrame;
    private static volatile Supplier<BlockEntityType<DyeVatBlockEntity>> dyeVatEntity;

    private CraftingBlocks() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(CraftingMod.MOD_ID);
        for (PebbleKind kind : PebbleKind.values()) {
            LOOSE_PEBBLES.put(kind, registrar.register(Registries.BLOCK, kind.blockPath, () -> new LoosePebbleBlock(kind, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).noCollission().instabreak().sound(SoundType.STONE).offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY))));
        }
        TABLES.put(MakeshiftTable.WOODEN, registrar.register(Registries.BLOCK, MakeshiftTable.WOODEN.path, () -> new MakeshiftTableBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(WOODEN_TABLE_STRENGTH).sound(SoundType.WOOD).noOcclusion()
                        .ignitedByLava())));
        TABLES.put(MakeshiftTable.STONE, registrar.register(Registries.BLOCK, MakeshiftTable.STONE.path, () -> new MakeshiftStoneTableBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(STONE_TABLE_STRENGTH).sound(SoundType.STONE).noOcclusion())));
        tableEntity = registrar.register(Registries.BLOCK_ENTITY_TYPE, "makeshift_table", () -> LoaderPlatform.current()
                .blockEntityType(MakeshiftTableBlockEntity::new, table(MakeshiftTable.WOODEN), table(MakeshiftTable.STONE)));
        dyeVat = registrar.register(Registries.BLOCK, "dye_vat", () -> new DyeVatBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                .strength(DYE_VAT_STRENGTH).sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
        bedFrame = registrar.register(Registries.BLOCK, "bed_frame", () -> new BedFrameBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                .strength(BED_FRAME_STRENGTH).sound(SoundType.WOOD).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY)));
        dyeVatEntity = registrar.register(Registries.BLOCK_ENTITY_TYPE, "dye_vat",
                () -> LoaderPlatform.current().blockEntityType(DyeVatBlockEntity::new, dyeVat.get()));
    }

    /** Natural stone pebble clusters. There's no block item: players can't place them. */
    public static LoosePebbleBlock loosePebble() {
        return loosePebble(PebbleKind.STONE);
    }

    public static LoosePebbleBlock loosePebble(PebbleKind kind) {
        return LOOSE_PEBBLES.get(kind).get();
    }

    public static List<LoosePebbleBlock> loosePebbles() {
        return Arrays.stream(PebbleKind.values()).map(CraftingBlocks::loosePebble).toList();
    }

    public static MakeshiftTableBlock table(MakeshiftTable table) {
        return TABLES.get(table).get();
    }

    public static BlockEntityType<MakeshiftTableBlockEntity> tableEntity() {
        return tableEntity.get();
    }

    public static BedFrameBlock bedFrame() {
        return bedFrame.get();
    }

    public static DyeVatBlock dyeVat() {
        return dyeVat.get();
    }

    public static BlockEntityType<DyeVatBlockEntity> dyeVatEntity() {
        return dyeVatEntity.get();
    }
}
