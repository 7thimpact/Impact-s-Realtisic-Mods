package com.impact.realisticfood.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.audit.RecipeAudit;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.CraftingGrids;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.cooking.CookingMethod;
import com.impact.realisticfood.cooking.PieceCooking;
import com.impact.realisticfood.cutting.Cut;
import com.impact.realisticfood.cutting.Cuts;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.item.KnifeMaterial;
import com.impact.realisticfood.registry.FoodItems;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Set;

/** Food milestone 2: knives, every cut, byproducts and their config, piece food values and cooking. */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodCuttingGameTests {
    private static final int KNIFE_SLOT = 1;
    private static final int FOOD_SLOT = 0;
    private static final int SPEC_PIECES_PER_CUT = 2;
    private static final int FULL_STACK = 64;

    private FoodCuttingGameTests() {
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void everyCutHasAKnifeRecipe(GameTestHelper helper) {
        for (Cut cut : Cuts.ALL) {
            Optional<CraftingRecipe> recipe = findCraftingRecipe(helper, gridWithKnife(new ItemStack(cut.input()), KnifeMaterial.FLINT));
            String inputName = ResourceIds.itemId(cut.input()).toString();
            helper.assertTrue(recipe.isPresent(), inputName + " and a knife should make a cutting recipe");
            ItemStack result = recipe.get().assemble(gridWithKnife(new ItemStack(cut.input()), KnifeMaterial.FLINT), helper.getLevel().registryAccess());
            helper.assertTrue(result.is(cut.result().get()) && result.getCount() == cut.resultCount(), inputName + " should cut into its pieces");
        }
        helper.assertTrue(Cuts.forInput(Items.BEEF).orElseThrow().resultCount() == SPEC_PIECES_PER_CUT, "Raw beef cuts into 2");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void cuttingWearsTheKnife(GameTestHelper helper) {
        CraftingContainer grid = gridWithKnife(new ItemStack(Items.BEEF), KnifeMaterial.IRON);
        CraftingRecipe recipe = findCraftingRecipe(helper, grid).orElseThrow();
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(grid);
        helper.assertTrue(remaining.get(KNIFE_SLOT).is(FoodItems.knife(KnifeMaterial.IRON)) && remaining.get(KNIFE_SLOT).getDamageValue() == 1,
                "The knife stays in the grid with 1 damage");
        helper.assertTrue(remaining.get(FOOD_SLOT).isEmpty(), "The beef is used up");
        helper.assertTrue(findCraftingRecipe(helper, CraftingGrids.of(new ItemStack(Items.BEEF))).isEmpty(), "Beef alone cuts into nothing");
        helper.assertTrue(findCraftingRecipe(helper, CraftingGrids.of(new ItemStack(Items.COOKED_BEEF), new ItemStack(FoodItems.knife(KnifeMaterial.FLINT)))).isEmpty(),
                "Cooked food isn't cut");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void byproductsFollowTheConfig(GameTestHelper helper) {
        FoodServerConfig.CuttingSettings cutting = FoodServerConfig.get().cutting;
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            ConfigOverrides.withValue(cutting.byproductChanceOverrides, List.of("minecraft:beef=1.0"), () -> {
                cutBeefAs(helper, player);
                helper.assertTrue(player.getInventory().countItem(Items.BONE) == 1, "With chance 1, cutting beef always gives a bone");
                ConfigOverrides.withValue(cutting.areByproductsOn, false, () -> cutBeefAs(helper, player));
                helper.assertTrue(player.getInventory().countItem(Items.BONE) == 1, "With byproducts off, no more bones");
            });
            ConfigOverrides.withValue(cutting.byproductChanceOverrides, List.of("minecraft:beef=0"), () -> cutBeefAs(helper, player));
            helper.assertTrue(player.getInventory().countItem(Items.BONE) == 1, "With chance 0, never a bone");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    // With Inventory installed, its locked slots count as full too; either way the byproduct is never deleted.
    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void byproductsWithNoRoomDropAtTheFeet(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            Inventory inventory = player.getInventory();
            for (int slot = 0; slot < inventory.items.size(); slot++) {
                inventory.items.set(slot, new ItemStack(Items.DIRT, FULL_STACK));
            }
            inventory.offhand.set(0, new ItemStack(Items.DIRT, FULL_STACK));
            // Mock players all join at the world spawn, where earlier runs may have left bones lying.
            Set<UUID> bonesAlreadyThere = bonesNear(helper, player).stream().map(ItemEntity::getUUID).collect(Collectors.toSet());
            ConfigOverrides.withValue(FoodServerConfig.get().cutting.byproductChanceOverrides, List.of("minecraft:beef=1.0"), () -> cutBeefAs(helper, player));
            long newBones = bonesNear(helper, player).stream().filter(bone -> !bonesAlreadyThere.contains(bone.getUUID())).count();
            helper.assertTrue(newBones == 1, "With every slot full, the bone drops at the player's feet (found " + newBones + ")");
            helper.assertTrue(inventory.countItem(Items.BONE) == 0, "Nothing is squeezed into the inventory");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void yieldsFollowTheConfig(GameTestHelper helper) {
        ConfigOverrides.withValue(FoodServerConfig.get().cutting.yieldOverrides, List.of("minecraft:beef=3"), () -> {
            CraftingContainer grid = gridWithKnife(new ItemStack(Items.BEEF), KnifeMaterial.FLINT);
            ItemStack result = findCraftingRecipe(helper, grid).orElseThrow().assemble(grid, helper.getLevel().registryAccess());
            helper.assertTrue(result.getCount() == 3, "cutting.yieldOverrides makes beef cut into 3");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void piecesFillHalfAsMuch(GameTestHelper helper) {
        FoodProperties cookedBeefCut = foodOf(FoodItems.piece(FoodPiece.COOKED_BEEF_CUT));
        helper.assertTrue(cookedBeefCut.getNutrition() == 4, "A Cooked Beef Cut restores 4 hunger (cooked beef restores 8)");
        helper.assertTrue(cookedBeefCut.getSaturationModifier() == foodOf(Items.COOKED_BEEF).getSaturationModifier(),
                "Its saturation shrinks with its hunger");
        helper.assertTrue(cookedBeefCut.isMeat(), "Cuts are meat, so wolves eat them");
        FoodProperties rawChickenPiece = foodOf(FoodItems.piece(FoodPiece.RAW_CHICKEN_PIECE));
        helper.assertTrue(rawChickenPiece.getEffects().stream().anyMatch(effect -> effect.getFirst().getEffect() == MobEffects.HUNGER),
                "Raw chicken pieces keep raw chicken's Hunger chance");
        helper.assertTrue(FoodItems.piece(FoodPiece.CARROT_SLICES).isEdible(), "Carrot slices can be eaten raw");
        helper.assertFalse(FoodItems.piece(FoodPiece.POTATO_SLICES).isEdible(), "Potato slices are cooked first");
        for (FoodPiece piece : FoodPiece.values()) {
            boolean neverGainsFood = !piece.isEdible
                    || foodOf(FoodItems.piece(piece)).getNutrition() * SPEC_PIECES_PER_CUT <= Math.max(SPEC_PIECES_PER_CUT, foodOf(piece.wholeItem).getNutrition());
            helper.assertTrue(neverGainsFood, piece.path + " shouldn't give more food than its whole item");
        }
        ConfigOverrides.withValue(FoodServerConfig.get().pieces.foodShare, 0.25, () ->
                helper.assertTrue(foodOf(FoodItems.piece(FoodPiece.COOKED_BEEF_CUT)).getNutrition() == 2, "pieces.foodShare 0.25 gives 2"));
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void rawPiecesCookInHalfTheTime(GameTestHelper helper) {
        Container rawCut = new SimpleContainer(new ItemStack(FoodItems.piece(FoodPiece.RAW_BEEF_CUT)));
        AbstractCookingRecipe smelting = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, rawCut, helper.getLevel()).orElseThrow();
        helper.assertTrue(smelting.getResultItem(helper.getLevel().registryAccess()).is(FoodItems.piece(FoodPiece.COOKED_BEEF_CUT)),
                "A Raw Beef Cut smelts into a Cooked Beef Cut");
        helper.assertTrue(smelting.getCookingTime() == 100, "It takes 100 ticks in a furnace");
        AbstractCookingRecipe campfire = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, rawCut, helper.getLevel()).orElseThrow();
        helper.assertTrue(campfire.getCookingTime() == 300, "It takes 300 ticks on a campfire");
        Container potatoSlices = new SimpleContainer(new ItemStack(FoodItems.piece(FoodPiece.POTATO_SLICES)));
        AbstractCookingRecipe frying = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMOKING, potatoSlices, helper.getLevel()).orElseThrow();
        helper.assertTrue(frying.getResultItem(helper.getLevel().registryAccess()).is(FoodItems.piece(FoodPiece.FRIED_POTATO_SLICES)),
                "Potato slices cook into Fried Potato Slices");
        ConfigOverrides.withValue(PieceCooking.ticksSetting(CookingMethod.SMELTING), 120, () ->
                helper.assertTrue(smelting.getCookingTime() == 120, "pieces.furnaceTicks changes the furnace time"));
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void knivesAreWeakConfiguredWeapons(GameTestHelper helper) {
        helper.assertTrue(new ItemStack(FoodItems.knife(KnifeMaterial.FLINT)).getMaxDamage() == 64, "A Flint Knife lasts 64 uses");
        helper.assertTrue(new ItemStack(FoodItems.knife(KnifeMaterial.IRON)).getMaxDamage() == 250, "An Iron Knife lasts 250 uses");
        ConfigOverrides.withValue(FoodServerConfig.get().knives.flintDurability, 10, () ->
                helper.assertTrue(new ItemStack(FoodItems.knife(KnifeMaterial.FLINT)).getMaxDamage() == 10, "knives.flintDurability changes it"));
        helper.assertTrue(attackDamageBonus(FoodItems.knife(KnifeMaterial.FLINT)) == 2.0, "A Flint Knife deals 3 damage (1 + 2)");
        helper.assertTrue(attackDamageBonus(FoodItems.knife(KnifeMaterial.IRON)) == 3.0, "An Iron Knife deals 4 damage (1 + 3)");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void knifeCuttingReplacesGridSeeds(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.itemId(Items.MELON_SEEDS)).isEmpty(),
                "Vanilla's grid melon seeds recipe is off while cutting is on");
        CraftingContainer grid = gridWithKnife(new ItemStack(Items.PUMPKIN), KnifeMaterial.FLINT);
        ItemStack seeds = findCraftingRecipe(helper, grid).orElseThrow().assemble(grid, helper.getLevel().registryAccess());
        helper.assertTrue(seeds.is(Items.PUMPKIN_SEEDS) && seeds.getCount() == 4, "A pumpkin and a knife give 4 seeds");
        List<RecipeAudit.Entry> audit = RecipeAudit.audit(helper.getLevel().getServer().getResourceManager(), helper.getLevel().getRecipeManager());
        boolean isAuditedAsTurnedOff = audit.stream().anyMatch(entry -> entry.recipeId().equals(ResourceIds.itemId(Items.MELON_SEEDS))
                && entry.overridingMods().equals(Set.of(FoodMod.MOD_ID)) && entry.isTurnedOff());
        helper.assertTrue(isAuditedAsTurnedOff, "Lib's recipe audit lists melon seeds as turned off by Food");
        helper.succeed();
    }

    private static void cutBeefAs(GameTestHelper helper, ServerPlayer player) {
        CraftingContainer grid = gridWithKnife(new ItemStack(Items.BEEF), KnifeMaterial.FLINT);
        CraftingRecipe recipe = findCraftingRecipe(helper, grid).orElseThrow();
        ForgeHooks.setCraftingPlayer(player);
        try {
            recipe.getRemainingItems(grid);
        } finally {
            ForgeHooks.setCraftingPlayer(null);
        }
    }

    private static List<ItemEntity> bonesNear(GameTestHelper helper, ServerPlayer player) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(3.0), entity -> entity.getItem().is(Items.BONE));
    }

    private static double attackDamageBonus(Item knife) {
        return new ItemStack(knife).getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream()
                .mapToDouble(AttributeModifier::getAmount).sum();
    }

    static FoodProperties foodOf(Item item) {
        return new ItemStack(item).getFoodProperties(null);
    }

    private static CraftingContainer gridWithKnife(ItemStack food, KnifeMaterial knife) {
        return CraftingGrids.of(food, new ItemStack(FoodItems.knife(knife)));
    }

    private static Optional<CraftingRecipe> findCraftingRecipe(GameTestHelper helper, CraftingContainer grid) {
        return helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
    }
}
