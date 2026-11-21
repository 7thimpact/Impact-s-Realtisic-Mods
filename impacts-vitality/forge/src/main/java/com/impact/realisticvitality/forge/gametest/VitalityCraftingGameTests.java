package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.CraftingGrids;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.item.UsesDurability;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.AirTank;
import com.impact.realisticvitality.config.VitalityGear;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.temperature.ArmorLining;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Vitality milestone 10, Crafting part: with Impact's Realistic Crafting installed, the gear is made from Crafting's
 * parts (the vanilla-material recipes switch off), linings are sewn with the Bone Needle, and the loose Air Tank is
 * hammered. Crafting's items are looked up by registry ID; the tests that need them pass at once without Crafting.
 */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityCraftingGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;
    private static final String WITH_CRAFTING_SUFFIX = "_with_" + VitalityMod.CRAFTING_MOD_ID;
    // The gear whose recipe needs no Crafting parts, so it's the same either way; the Air Tank switches to a loose tank.
    private static final List<VitalityGear> UNCHANGED_GEAR = List.of(VitalityGear.BOTTLED_AIR, VitalityGear.TIDEBOUND_CANTEEN,
            VitalityGear.COPPER_STILL, VitalityGear.NETHERITE_REBREATHER, VitalityGear.SHULKER_AIR_TANK, VitalityGear.AIR_TANK);
    private static final int CHESTPLATE_DAMAGE = 40;
    private static final int LONGEST_HOLD_TICKS = 400;

    private VitalityCraftingGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void gearRecipesSwitchToCraftingParts(GameTestHelper helper) {
        boolean hasCrafting = ImpactsLib.isLoaded(VitalityMod.CRAFTING_MOD_ID);
        RecipeManager recipes = helper.getLevel().getRecipeManager();
        Arrays.stream(VitalityGear.values()).filter(gear -> !UNCHANGED_GEAR.contains(gear)).forEach(gear -> {
            boolean hasAloneRecipe = recipes.byKey(ResourceIds.of(VitalityMod.MOD_ID, gear.itemPath)).isPresent();
            boolean hasCraftingRecipe = recipes.byKey(ResourceIds.of(VitalityMod.MOD_ID, gear.itemPath + WITH_CRAFTING_SUFFIX)).isPresent();
            helper.assertTrue(hasAloneRecipe != hasCrafting, "The " + gear.itemPath + " recipe from vanilla materials loads only without Crafting");
            helper.assertTrue(hasCraftingRecipe == hasCrafting, "The " + gear.itemPath + " recipe from Crafting's parts loads only with Crafting");
        });
        boolean hasLooseTank = recipes.byKey(ResourceIds.of(VitalityMod.MOD_ID, "loose_air_tank")).isPresent()
                && recipes.byKey(ResourceIds.of(VitalityMod.MOD_ID, "air_tank_from_hammering")).isPresent();
        helper.assertTrue(hasLooseTank == hasCrafting, "The loose Air Tank and its hammering load only with Crafting");
        helper.assertTrue(recipes.byKey(ResourceIds.of(VitalityMod.MOD_ID, "air_tank")).isPresent() != hasCrafting,
                "The Air Tank from iron ingots loads only without Crafting");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theBoneNeedleSewsALiningIn(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded(VitalityMod.CRAFTING_MOD_ID)) {
            helper.succeed();
            return;
        }
        ItemStack chestplate = new ItemStack(Items.IRON_CHESTPLATE);
        EnchantmentHelper.setEnchantments(Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 3), chestplate);
        chestplate.setDamageValue(CHESTPLATE_DAMAGE);
        CraftingContainer grid = CraftingGrids.of(chestplate, new ItemStack(Items.WHITE_WOOL), new ItemStack(Items.RED_WOOL),
                new ItemStack(Items.WHITE_WOOL), crafting("twine"), crafting("bone_needle"));
        Optional<CraftingRecipe> recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
        helper.assertTrue(recipe.isPresent(), "Armor, 3 wool, Twine and the Bone Needle sew a lining");
        ItemStack lined = recipe.get().assemble(grid, helper.getLevel().registryAccess());
        helper.assertTrue(ArmorLining.of(lined).orElseThrow() == ArmorLining.WOOL, "It's a Wool Lining");
        helper.assertTrue(lined.getEnchantmentLevel(Enchantments.ALL_DAMAGE_PROTECTION) == 3 && lined.getDamageValue() == CHESTPLATE_DAMAGE,
                "The chestplate keeps its enchantments and damage");
        NonNullList<ItemStack> remaining = recipe.get().getRemainingItems(grid);
        ItemStack needle = remaining.stream().filter(stack -> isCrafting(stack, "bone_needle")).findFirst().orElse(ItemStack.EMPTY);
        helper.assertTrue(needle.getDamageValue() == 1 || UsesDurability.usesSpent(needle) == 1, "The needle stays and takes 1 damage");
        CraftingContainer withoutNeedle = CraftingGrids.of(chestplate, new ItemStack(Items.WHITE_WOOL), new ItemStack(Items.WHITE_WOOL),
                new ItemStack(Items.WHITE_WOOL), crafting("twine"));
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, withoutNeedle, helper.getLevel()).isEmpty(),
                "Without the needle nothing is sewn");
        CraftingContainer tooLittleHide = CraftingGrids.of(chestplate, new ItemStack(Items.RABBIT_HIDE), new ItemStack(Items.RABBIT_HIDE),
                new ItemStack(Items.RABBIT_HIDE), crafting("twine"), crafting("bone_needle"));
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, tooLittleHide, helper.getLevel()).isEmpty(),
                "A Fur Lining takes 4 rabbit hide, not 3");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void aLooseAirTankIsHammeredIntoAnAirTank(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded(VitalityMod.CRAFTING_MOD_ID)) {
            helper.succeed();
            return;
        }
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(VitalityItems.looseAirTank()));
            player.setItemInHand(InteractionHand.OFF_HAND, crafting("stone_pebble"));
            holdUse(player);
            helper.assertTrue(player.getMainHandItem().is(VitalityItems.tank(AirTank.AIR_TANK)), "Hammering the loose tank seals it into an Air Tank");
            helper.assertTrue(UsesDurability.usesSpent(player.getOffhandItem()) == 1, "The pebble wears by 1 use");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    private static void holdUse(ServerPlayer player) {
        player.getMainHandItem().getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        for (int tick = 0; tick < LONGEST_HOLD_TICKS && player.isUsingItem(); tick++) {
            ItemStack held = player.getUseItem();
            held.getItem().onUseTick(player.level(), player, held, player.getUseItemRemainingTicks());
        }
        player.stopUsingItem();
    }

    private static ItemStack crafting(String itemPath) {
        ResourceLocation itemId = ResourceIds.of(VitalityMod.CRAFTING_MOD_ID, itemPath);
        Item item = ResourceIds.item(itemId).orElseThrow(() -> new IllegalStateException("Crafting has no item " + itemId));
        return new ItemStack(item);
    }

    private static boolean isCrafting(ItemStack stack, String itemPath) {
        return !stack.isEmpty() && ResourceIds.itemId(stack.getItem()).equals(ResourceIds.of(VitalityMod.CRAFTING_MOD_ID, itemPath));
    }
}
