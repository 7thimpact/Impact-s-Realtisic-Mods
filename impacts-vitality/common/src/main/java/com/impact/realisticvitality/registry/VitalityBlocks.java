package com.impact.realisticvitality.registry;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.block.AirCompressorBlock;
import com.impact.realisticvitality.air.block.AirCompressorBlockEntity;
import com.impact.realisticvitality.thirst.block.CopperStillBlock;
import com.impact.realisticvitality.thirst.block.CopperStillBlockEntity;
import com.impact.realisticvitality.thirst.block.RainBarrelBlock;
import com.impact.realisticvitality.thirst.block.RainBarrelBlockEntity;
import com.impact.realisticvitality.thirst.block.WaterFilterBlock;
import com.impact.realisticvitality.thirst.block.WaterFilterBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.List;
import java.util.function.Supplier;

/** Vitality's blocks, their items and block entities: the Rain Barrel, Water Filter, Copper Still and Air Compressor. */
public final class VitalityBlocks {
    private static final float WOODEN_STRENGTH = 2.5F;
    private static final float COPPER_STRENGTH = 3.0F;
    private static final float COPPER_BLAST_RESISTANCE = 6.0F;
    private static final int LIT_LIGHT = 7;
    private static volatile Supplier<RainBarrelBlock> rainBarrel;
    private static volatile Supplier<WaterFilterBlock> waterFilter;
    private static volatile Supplier<CopperStillBlock> copperStill;
    private static volatile Supplier<BlockEntityType<RainBarrelBlockEntity>> rainBarrelEntity;
    private static volatile Supplier<BlockEntityType<WaterFilterBlockEntity>> waterFilterEntity;
    private static volatile Supplier<BlockEntityType<CopperStillBlockEntity>> copperStillEntity;
    private static volatile Supplier<AirCompressorBlock> airCompressor;
    private static volatile Supplier<BlockEntityType<AirCompressorBlockEntity>> airCompressorEntity;

    private VitalityBlocks() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(VitalityMod.MOD_ID);
        rainBarrel = registrar.register(Registries.BLOCK, "rain_barrel", () -> new RainBarrelBlock(woodenProperties()));
        waterFilter = registrar.register(Registries.BLOCK, "water_filter", () -> new WaterFilterBlock(woodenProperties()));
        copperStill = registrar.register(Registries.BLOCK, "copper_still", () -> new CopperStillBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE).strength(COPPER_STRENGTH, COPPER_BLAST_RESISTANCE).sound(SoundType.COPPER)
                .requiresCorrectToolForDrops()));
        airCompressor = registrar.register(Registries.BLOCK, "air_compressor", () -> new AirCompressorBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE).strength(COPPER_STRENGTH, COPPER_BLAST_RESISTANCE).sound(SoundType.COPPER)
                .requiresCorrectToolForDrops().lightLevel(state -> state.getValue(AirCompressorBlock.LIT) ? LIT_LIGHT : 0)));
        registrar.register(Registries.ITEM, "air_compressor", () -> new BlockItem(airCompressor.get(), new Item.Properties()));
        airCompressorEntity = registrar.register(Registries.BLOCK_ENTITY_TYPE, "air_compressor",
                () -> LoaderPlatform.current().blockEntityType(AirCompressorBlockEntity::new, airCompressor.get()));
        registrar.register(Registries.ITEM, "rain_barrel", () -> new BlockItem(rainBarrel.get(), new Item.Properties()));
        registrar.register(Registries.ITEM, "water_filter", () -> new BlockItem(waterFilter.get(), new Item.Properties()));
        registrar.register(Registries.ITEM, "copper_still", () -> new BlockItem(copperStill.get(), new Item.Properties()));
        rainBarrelEntity = registrar.register(Registries.BLOCK_ENTITY_TYPE, "rain_barrel",
                () -> LoaderPlatform.current().blockEntityType(RainBarrelBlockEntity::new, rainBarrel.get()));
        waterFilterEntity = registrar.register(Registries.BLOCK_ENTITY_TYPE, "water_filter",
                () -> LoaderPlatform.current().blockEntityType(WaterFilterBlockEntity::new, waterFilter.get()));
        copperStillEntity = registrar.register(Registries.BLOCK_ENTITY_TYPE, "copper_still",
                () -> LoaderPlatform.current().blockEntityType(CopperStillBlockEntity::new, copperStill.get()));
    }

    public static RainBarrelBlock rainBarrel() {
        return rainBarrel.get();
    }

    public static WaterFilterBlock waterFilter() {
        return waterFilter.get();
    }

    public static CopperStillBlock copperStill() {
        return copperStill.get();
    }

    public static AirCompressorBlock airCompressor() {
        return airCompressor.get();
    }

    public static BlockEntityType<AirCompressorBlockEntity> airCompressorEntity() {
        return airCompressorEntity.get();
    }

    public static List<Block> all() {
        return List.of(rainBarrel(), waterFilter(), copperStill(), airCompressor());
    }

    public static BlockEntityType<RainBarrelBlockEntity> rainBarrelEntity() {
        return rainBarrelEntity.get();
    }

    public static BlockEntityType<WaterFilterBlockEntity> waterFilterEntity() {
        return waterFilterEntity.get();
    }

    public static BlockEntityType<CopperStillBlockEntity> copperStillEntity() {
        return copperStillEntity.get();
    }

    private static BlockBehaviour.Properties woodenProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(WOODEN_STRENGTH).sound(SoundType.WOOD).ignitedByLava();
    }
}
