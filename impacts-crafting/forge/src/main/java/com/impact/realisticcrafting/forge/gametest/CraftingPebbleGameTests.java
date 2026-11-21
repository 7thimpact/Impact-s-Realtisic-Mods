package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.LoosePebbleBlock;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.item.PebbleKind;
import com.impact.realisticcrafting.loot.PebbleDrops;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import com.impact.realisticcrafting.worldgen.CraftingWorldgen;
import com.impact.realisticcrafting.worldgen.LoosePebblePlacer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/** Crafting milestone 1: the three pebble sources (loose pebbles in worldgen, gravel, stone) and the loose pebble block. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingPebbleGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final BlockPos BASE = BlockPos.ZERO;
    private static final BlockPos ABOVE_BASE = BlockPos.ZERO.above();
    private static final double ALWAYS = 1.0;
    private static final double NEVER = 0.0;

    private CraftingPebbleGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void gravelDropsPebblesByChance(GameTestHelper helper) {
        ItemStack shovel = new ItemStack(Items.WOODEN_SHOVEL);
        ConfigOverrides.withValue(CraftingServerConfig.get().pebbles.gravelChance, ALWAYS, () ->
                helper.assertTrue(pebblesIn(drops(helper, Blocks.GRAVEL.defaultBlockState(), shovel)) == 1, "Gravel drops a pebble at 100%"));
        ConfigOverrides.withValue(CraftingServerConfig.get().pebbles.gravelChance, NEVER, () ->
                helper.assertTrue(pebblesIn(drops(helper, Blocks.GRAVEL.defaultBlockState(), shovel)) == 0, "and none at 0%"));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void silkTouchGivesNoPebble(GameTestHelper helper) {
        ItemStack silkShovel = new ItemStack(Items.IRON_SHOVEL);
        silkShovel.enchant(Enchantments.SILK_TOUCH, 1);
        ConfigOverrides.withValue(CraftingServerConfig.get().pebbles.gravelChance, ALWAYS, () -> {
            List<ItemStack> loot = drops(helper, Blocks.GRAVEL.defaultBlockState(), silkShovel);
            helper.assertTrue(pebblesIn(loot) == 0, "Silk Touch gives no pebble");
            helper.assertTrue(loot.stream().anyMatch(stack -> stack.is(Items.GRAVEL)), "only the gravel itself");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void stoneWithAPickaxeDropsPebblesByChance(GameTestHelper helper) {
        ItemStack pickaxe = new ItemStack(Items.WOODEN_PICKAXE);
        ConfigOverrides.withValue(CraftingServerConfig.get().pebbles.stonePickaxeChance, ALWAYS, () -> {
            List<ItemStack> loot = drops(helper, Blocks.STONE.defaultBlockState(), pickaxe);
            helper.assertTrue(pebblesIn(loot) == 1, "Stone mined with a pickaxe drops a pebble at 100%");
            helper.assertTrue(loot.stream().anyMatch(stack -> stack.is(Items.COBBLESTONE)), "on top of its cobblestone");
            helper.assertTrue(pebblesIn(drops(helper, Blocks.CALCITE.defaultBlockState(), pickaxe)) == 0, "Calcite only hosts worldgen");
            helper.assertTrue(pebblesIn(drops(helper, Blocks.STONE.defaultBlockState(), ItemStack.EMPTY)) == 0,
                    "Stone's loot rolled without a tool (an explosion) gives no pebble");
        });
        ConfigOverrides.withValue(CraftingServerConfig.get().pebbles.stonePickaxeChance, NEVER, () ->
                helper.assertTrue(pebblesIn(drops(helper, Blocks.STONE.defaultBlockState(), pickaxe)) == 0, "and none at 0%"));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void stoneByHandAlwaysDropsAPebble(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setGameMode(GameType.SURVIVAL);
            helper.assertTrue(PebbleDrops.fromHandBreak(Blocks.STONE.defaultBlockState(), player) == 1, "Stone broken by hand drops a pebble");
            helper.assertTrue(PebbleDrops.fromHandBreak(Blocks.DEEPSLATE.defaultBlockState(), player) == 1, "so does deepslate");
            helper.assertTrue(PebbleDrops.fromHandBreak(Blocks.MOSSY_COBBLESTONE.defaultBlockState(), player) == 0, "but not mossy cobblestone");
            helper.assertTrue(PebbleDrops.fromHandBreak(Blocks.DIRT.defaultBlockState(), player) == 0, "nor dirt");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE_PICKAXE));
            helper.assertTrue(PebbleDrops.fromHandBreak(Blocks.STONE.defaultBlockState(), player) == 0,
                    "With a pickaxe, stone drops its loot and the loot modifier rolls instead");
            player.setGameMode(GameType.CREATIVE);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            helper.assertTrue(PebbleDrops.fromHandBreak(Blocks.STONE.defaultBlockState(), player) == 0, "Creative mode drops nothing");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void breakingStoneByHandPopsAPebble(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos stone = helper.absolutePos(BASE);
        ServerPlayer player = MockPlayers.joinWorld(level);
        try {
            player.setGameMode(GameType.SURVIVAL);
            level.setBlockAndUpdate(stone, Blocks.STONE.defaultBlockState());
            player.gameMode.destroyBlock(stone);
            helper.assertTrue(level.getBlockState(stone).isAir(), "The stone breaks");
            helper.assertTrue(pebbleEntitiesAt(level, stone) == 1, "and pops one pebble");
        } finally {
            removeItemsAt(level, stone);
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void loosePebblesDropOnePebble(GameTestHelper helper) {
        BlockState loosePebbles = CraftingBlocks.loosePebble().defaultBlockState();
        helper.assertTrue(pebblesIn(drops(helper, loosePebbles, ItemStack.EMPTY)) == 1, "Loose pebbles break into one Stone Pebble");
        helper.assertTrue(loosePebbles.getDestroySpeed(helper.getLevel(), helper.absolutePos(BASE)) == 0.0F, "They break instantly");
        helper.assertTrue(CraftingBlocks.loosePebble().asItem() == Items.AIR, "Players can't place them: there's no block item");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void loosePebblesPopOffWithoutSupport(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BASE);
        BlockPos pebbles = helper.absolutePos(ABOVE_BASE);
        try {
            level.setBlockAndUpdate(base, Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pebbles, CraftingBlocks.loosePebble().defaultBlockState());
            level.setBlockAndUpdate(base, Blocks.AIR.defaultBlockState());
            helper.assertTrue(level.getBlockState(pebbles).isAir(), "Loose pebbles pop off when the block under them goes");
            helper.assertTrue(pebbleEntitiesAt(level, pebbles) == 1, "and drop their pebble");

            level.setBlockAndUpdate(base, Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pebbles, CraftingBlocks.loosePebble().defaultBlockState().setValue(LoosePebbleBlock.WATERLOGGED, true));
            level.setBlockAndUpdate(base, Blocks.AIR.defaultBlockState());
            helper.assertTrue(level.getBlockState(pebbles).is(Blocks.WATER), "Waterlogged pebbles leave their water behind");
        } finally {
            level.setBlockAndUpdate(pebbles, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(base, Blocks.AIR.defaultBlockState());
            removeItemsAt(level, pebbles);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void worldgenPlacesOnNaturalGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BASE);
        BlockPos spot = helper.absolutePos(ABOVE_BASE);
        try {
            helper.assertTrue(placesOn(level, base, spot, Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState()), "Pebbles generate on stone");
            helper.assertTrue(placesOn(level, base, spot, Blocks.CALCITE.defaultBlockState(), Blocks.AIR.defaultBlockState()),
                    "and on calcite, a worldgen-only base");
            helper.assertTrue(placesOn(level, base, spot, Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.AIR.defaultBlockState()), "on grass");
            helper.assertTrue(placesOn(level, base, spot, Blocks.SAND.defaultBlockState(), Blocks.AIR.defaultBlockState()), "and on sand,");
            helper.assertTrue(level.getBlockState(spot).is(CraftingBlocks.loosePebble(PebbleKind.SANDSTONE)), "where they're sandstone pebbles");
            helper.assertTrue(placesOn(level, base, spot, Blocks.GRANITE.defaultBlockState(), Blocks.AIR.defaultBlockState())
                    && level.getBlockState(spot).is(CraftingBlocks.loosePebble(PebbleKind.GRANITE)), "Granite ground has granite pebbles");
            helper.assertFalse(placesOn(level, base, spot, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.AIR.defaultBlockState()), "but not on planks");
            helper.assertFalse(placesOn(level, base, spot, Blocks.STONE.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState()),
                    "nor where the spot is taken");
            helper.assertTrue(placesOn(level, base, spot, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState()), "River beds work");
            helper.assertTrue(level.getBlockState(spot).getValue(LoosePebbleBlock.WATERLOGGED), "and those pebbles sit in water");
        } finally {
            level.setBlockAndUpdate(spot, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(base, Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void worldgenHonorsTheBiomeBlacklist(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BASE);
        BlockPos spot = helper.absolutePos(ABOVE_BASE);
        String biomeId = level.getBiome(spot).unwrapKey().orElseThrow().location().toString();
        try {
            ConfigOverrides.withValue(CraftingServerConfig.get().pebbles.worldgenBiomeBlacklist, List.of(biomeId), () ->
                    helper.assertFalse(placesOn(level, base, spot, Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState()),
                            "No pebbles in a blacklisted biome"));
            ConfigOverrides.withValue(CraftingServerConfig.get().pebbles.isWorldgenOn, false, () ->
                    helper.assertTrue(LoosePebblePlacer.placeOnSurface(level, base, level.getRandom()) == 0, "None at all with worldgen off"));
            ConfigOverrides.withValue(CraftingServerConfig.get().pebbles.worldgenDimensions, List.of("minecraft:the_nether"), () ->
                    helper.assertTrue(LoosePebblePlacer.placeInCaves(level, base, level.getRandom()) == 0, "None in an unlisted dimension"));
        } finally {
            level.setBlockAndUpdate(spot, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(base, Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void overworldBiomesGetTheFeatures(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Biome plains = level.registryAccess().registryOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        Biome stonyPeaks = level.registryAccess().registryOrThrow(Registries.BIOME).getOrThrow(Biomes.STONY_PEAKS);
        Biome nether = level.registryAccess().registryOrThrow(Registries.BIOME).getOrThrow(Biomes.NETHER_WASTES);
        Holder<PlacedFeature> surface = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE)
                .getHolderOrThrow(CraftingWorldgen.SURFACE_PEBBLES_PLACED);
        Holder<PlacedFeature> cave = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getHolderOrThrow(CraftingWorldgen.CAVE_PEBBLES_PLACED);
        helper.assertTrue(hasFeature(plains, surface) && hasFeature(plains, cave), "Plains gets both pebble features");
        helper.assertTrue(hasFeature(stonyPeaks, surface), "Stony peaks gets surface pebbles");
        helper.assertFalse(hasFeature(nether, surface), "The Nether doesn't");
        Registry<BiomeModifier> biomeModifiers = level.registryAccess().registryOrThrow(ForgeRegistries.Keys.BIOME_MODIFIERS);
        helper.assertTrue(biomeModifiers.containsKey(ResourceIds.of(CraftingMod.MOD_ID, "surface_loose_pebbles"))
                && biomeModifiers.containsKey(ResourceIds.of(CraftingMod.MOD_ID, "cave_loose_pebbles")), "Both biome modifiers load");
        helper.succeed();
    }

    private static boolean hasFeature(Biome biome, Holder<PlacedFeature> feature) {
        return biome.getGenerationSettings().features().stream().anyMatch(step -> step.contains(feature));
    }

    private static boolean placesOn(ServerLevel level, BlockPos base, BlockPos spot, BlockState baseState, BlockState spotState) {
        level.setBlock(base, baseState, Block.UPDATE_CLIENTS);
        level.setBlock(spot, spotState, Block.UPDATE_CLIENTS);
        return LoosePebblePlacer.tryPlace(level, spot, level.getRandom()) && level.getBlockState(spot).getBlock() instanceof LoosePebbleBlock;
    }

    private static List<ItemStack> drops(GameTestHelper helper, BlockState state, ItemStack tool) {
        return Block.getDrops(state, helper.getLevel(), helper.absolutePos(BASE), null, null, tool);
    }

    private static int pebblesIn(List<ItemStack> loot) {
        return loot.stream().filter(stack -> stack.is(CraftingTags.PEBBLES)).mapToInt(ItemStack::getCount).sum();
    }

    private static int pebbleEntitiesAt(ServerLevel level, BlockPos position) {
        return level.getEntitiesOfClass(ItemEntity.class, new AABB(position).inflate(1.0)).stream()
                .map(ItemEntity::getItem)
                .filter(stack -> stack.is(CraftingItems.stonePebble()))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static void removeItemsAt(ServerLevel level, BlockPos position) {
        level.getEntitiesOfClass(ItemEntity.class, new AABB(position).inflate(1.0)).forEach(ItemEntity::discard);
    }
}
