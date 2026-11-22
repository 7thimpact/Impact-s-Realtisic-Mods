package com.impact.realisticfood.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.CauldronInteractions;
import com.impact.realisticfood.boiling.CauldronPots;
import com.impact.realisticfood.food.FoodBase;
import com.impact.realisticfood.food.FoodStyle;
import com.impact.realisticfood.registry.FoodItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Vanilla behavior Food must leave alone: powder snow cauldrons, and vanilla's own item tags for styled foods. */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodVanillaGameTests {
    private static final BlockPos CAMPFIRE = BlockPos.ZERO;
    private static final BlockPos CAULDRON = CAMPFIRE.above();
    private static final TagKey<Item> VITALITY_DRY_FOODS = TagKey.create(Registries.ITEM, ResourceIds.of("impactsvitality", "dry_foods"));

    private FoodVanillaGameTests() {
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void aHeatedPowderSnowCauldronBoilsNothing(GameTestHelper helper) {
        helper.setBlock(CAMPFIRE, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        helper.setBlock(CAULDRON, Blocks.POWDER_SNOW_CAULDRON);
        Vec3 inside = Vec3.atBottomCenterOf(helper.absolutePos(CAULDRON)).add(0.0, 0.2, 0.0);
        ItemEntity egg = new ItemEntity(helper.getLevel(), inside.x, inside.y, inside.z, new ItemStack(Items.EGG));
        helper.getLevel().addFreshEntity(egg);
        try {
            CauldronInteractions.onItemInWater(helper.getLevel(), helper.absolutePos(CAULDRON), helper.getBlockState(CAULDRON), egg);
            helper.assertTrue(egg.isAlive() && egg.getItem().is(Items.EGG), "An egg dropped in powder snow stays an egg");
            helper.assertFalse(CauldronPots.of(helper.getLevel()).pot(helper.absolutePos(CAULDRON)).isPresent(), "Powder snow never becomes a pot");
        } finally {
            egg.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void styledFoodsJoinVanillaTagsButNotOtherImpactMods(GameTestHelper helper) {
        Item roastedCod = FoodItems.styled(FoodBase.COOKED_COD, FoodStyle.TOASTED).orElseThrow();
        helper.assertTrue(new ItemStack(roastedCod).is(ItemTags.FISHES), "Fire-Roasted Cod is in vanilla's fishes tag, like cooked cod");
        Item boiledBread = FoodItems.styled(FoodBase.BREAD, FoodStyle.BOILED).orElseThrow();
        helper.assertFalse(new ItemStack(boiledBread).is(VITALITY_DRY_FOODS),
                "Boiled Bread doesn't pick up Vitality's dry foods from bread (Vitality's tags list the styles they mean)");
        helper.succeed();
    }
}
