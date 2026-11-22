package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.grinding.BowlContents;
import com.impact.lib.grinding.Grinding;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.advancement.CraftingAchievements;
import com.impact.realisticcrafting.advancement.CraftingAdvancements;
import com.impact.realisticcrafting.advancement.CraftingTriggers;
import com.impact.realisticcrafting.block.DyeVatInteractions;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.client.CraftingTooltips;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.item.LooseItem;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.lang.CraftingHints;
import com.impact.realisticcrafting.lang.CraftingTranslations;
import com.impact.realisticcrafting.registry.CraftingItems;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.atTable;
import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.craft;

/** Crafting milestone 13: the 26 achievements load, and the crafts and world events behind them grant them. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingAchievementGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final int SPEC_ACHIEVEMENTS = 26;
    private static final String RECIPE_UNLOCKS_FOLDER = "recipes/";
    private static final int PEBBLES_PER_COBBLESTONE = 9;
    private static final int LONGEST_HOLD_TICKS = 400;

    private CraftingAchievementGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theTabHasEveryAchievement(GameTestHelper helper) {
        long loaded = helper.getLevel().getServer().getAdvancements().getAllAdvancements().stream()
                .filter(advancement -> advancement.getId().getNamespace().equals(CraftingMod.MOD_ID))
                .filter(advancement -> !advancement.getId().getPath().startsWith(RECIPE_UNLOCKS_FOLDER))
                .count();
        helper.assertTrue(CraftingAdvancements.all().size() == SPEC_ACHIEVEMENTS, "Crafting defines 26 achievements");
        helper.assertTrue(loaded == SPEC_ACHIEVEMENTS, "All of them load: found " + loaded);
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void earlyCraftsGrantTheirAchievements(GameTestHelper helper) {
        CraftingServerConfig.AchievementSettings settings = CraftingServerConfig.get().achievements;
        ConfigOverrides.withValue(settings.scrapCollectorScraps, 1, () -> ConfigOverrides.withValue(settings.masterCraftsmanCrafts, 2,
                () -> ConfigOverrides.withValue(settings.longCraftTicks, 10, () -> atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
                    craft(table, player, new ItemStack(Items.OAK_PLANKS), new ItemStack(CraftingItems.stonePebble()));
                    helper.assertTrue(isDone(helper, player, CraftingAdvancements.CUT_TO_SIZE), "Trimming planks grants Cut to Size");
                    helper.assertTrue(isDone(helper, player, CraftingAdvancements.SCRAP_COLLECTOR), "Its Wood Scrap counts toward Scrap Collector");
                    helper.assertTrue(isDone(helper, player, CraftingAdvancements.PATIENCE_IS_A_VIRTUE), "A craft past longCraftTicks grants Patience");
                    helper.assertFalse(isDone(helper, player, CraftingAdvancements.MASTER_CRAFTSMAN), "One Trimmed Planks isn't 2 crafted items yet");
                    table.grid().clearContent();
                    ItemStack[] pebbles = new ItemStack[PEBBLES_PER_COBBLESTONE];
                    Arrays.setAll(pebbles, slot -> new ItemStack(CraftingItems.stonePebble()));
                    craft(table, player, pebbles);
                    helper.assertTrue(isDone(helper, player, CraftingAdvancements.ROCK_SOLID), "9 pebbles into cobblestone grants Rock Solid");
                    helper.assertTrue(isDone(helper, player, CraftingAdvancements.MASTER_CRAFTSMAN), "Crafting's recipes count toward Master Craftsman");
                }))));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void hammeringGrantsTheToolAchievements(GameTestHelper helper) {
        withPlayer(helper, player -> {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CraftingItems.looseTool(ToolMaterial.STONE, ToolKind.PICKAXE)));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(CraftingItems.stonePebble()));
            holdUse(player);
            helper.assertTrue(player.getMainHandItem().is(Items.STONE_PICKAXE), "The loose pickaxe is hammered");
            helper.assertTrue(isDone(helper, player, CraftingAdvancements.HAMMER_TIME), "Hammering a loose tool grants Hammer Time");
            helper.assertTrue(isDone(helper, player, CraftingAdvancements.STONE_AGE), "A stone tool grants Stone Age");
            helper.assertFalse(isDone(helper, player, CraftingAdvancements.PUSHING_IT), "Pushing It needs a loose piston");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void grindingDyesGrantsTheColorAchievements(GameTestHelper helper) {
        withPlayer(helper, player -> {
            ItemStack bowl = new ItemStack(Grinding.woodenBowl());
            BowlContents.load(bowl, new ItemStack(Items.RED_DYE));
            BowlContents.load(bowl, new ItemStack(Items.YELLOW_DYE));
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Grinding.woodenStick()));
            player.setItemInHand(InteractionHand.OFF_HAND, bowl);
            holdUse(player);
            helper.assertTrue(player.getInventory().countItem(Items.ORANGE_DYE) == 2, "Red and yellow grind into 2 orange");
            helper.assertTrue(isDone(helper, player, CraftingAdvancements.GRIND_SET), "Grinding a dye grants Grind Set");
            helper.assertTrue(isDone(helper, player, CraftingAdvancements.COLOR_THEORY), "Mixing two dyes grants Color Theory");
        });
        CraftingDyeVatGameTests.atVat(helper, (player, vat) -> {
            vat.fillWithWater();
            CraftingDyeVatGameTests.use(player, vat, new ItemStack(Items.SUGAR_CANE));
            helper.assertFalse(isDone(helper, player, CraftingAdvancements.DIP_IT), "Soaking in plain water isn't dyeing");
            vat.addDye(DyeColor.RED, Optional.empty());
            DyeVatInteractions.forgetLastDip(player);
            CraftingDyeVatGameTests.use(player, vat, new ItemStack(Items.WHITE_WOOL));
            helper.assertTrue(isDone(helper, player, CraftingAdvancements.DIP_IT), "Dipping wool in a red vat grants Dip It");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bedsGrantTheBedAchievements(GameTestHelper helper) {
        CraftingBedGameTests.atFrame(helper, (player, foot) -> {
            CraftingBedGameTests.use(player, foot, new ItemStack(CraftingItems.comforter()));
            CraftingBedGameTests.use(player, foot, new ItemStack(CraftingItems.sheet(DyeColor.RED)));
            helper.assertTrue(isDone(helper, player, CraftingAdvancements.MAKING_THE_BED), "Finishing a bed grants Making the Bed");
            helper.assertFalse(isDone(helper, player, CraftingAdvancements.FULL_SPECTRUM), "One color isn't all 16");
            Arrays.stream(DyeColor.values()).forEach(color -> CraftingTriggers.BED_ASSEMBLED.trigger(player, color));
            helper.assertTrue(isDone(helper, player, CraftingAdvancements.FULL_SPECTRUM), "A bed in every color grants Full Spectrum");
        });
        withPlayer(helper, player -> {
            CraftingAchievements.onTableBroken(player);
            helper.assertTrue(isDone(helper, player, CraftingAdvancements.FALLING_APART), "A table falling apart grants Falling Apart");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void everyItemNamesItsNextStep(GameTestHelper helper) {
        List<String> missing = ForgeRegistries.ITEMS.getEntries().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(CraftingMod.MOD_ID))
                .map(Map.Entry::getValue)
                .filter(item -> !(item instanceof LooseItem))
                .filter(item -> CraftingTooltips.hintNameOf(item).filter(CraftingAchievementGameTests::hasHintText).isEmpty())
                .map(item -> ResourceIds.itemId(item).getPath())
                .toList();
        helper.assertTrue(missing.isEmpty(), "Every Crafting item but the loose ones has a next-step hint; missing: " + missing);
        helper.succeed();
    }

    private static boolean hasHintText(String hintName) {
        return CraftingTranslations.ENGLISH.containsKey(CraftingHints.nextKey(hintName))
                && CraftingTranslations.ENGLISH.containsKey(CraftingHints.madeFromKey(hintName));
    }

    static void holdUse(ServerPlayer player) {
        player.getMainHandItem().getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        for (int tick = 0; tick < LONGEST_HOLD_TICKS && player.isUsingItem(); tick++) {
            ItemStack held = player.getUseItem();
            held.getItem().onUseTick(player.level(), player, held, player.getUseItemRemainingTicks());
        }
        player.stopUsingItem();
    }

    private static void withPlayer(GameTestHelper helper, Consumer<ServerPlayer> check) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            check.accept(player);
        } finally {
            MockPlayers.leaveWorld(player);
        }
    }

    private static boolean isDone(GameTestHelper helper, ServerPlayer player, ResourceLocation advancementId) {
        Advancement advancement = helper.getLevel().getServer().getAdvancements().getAdvancement(advancementId);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }
}
