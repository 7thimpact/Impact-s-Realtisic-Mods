package com.impact.lib.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.advancement.LibAdvancements;
import com.impact.lib.advancement.LoneLibAchievement;
import com.impact.lib.command.ModArgument;
import com.impact.lib.config.ConfigTextFormat;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.config.DebugConfigSection;
import com.impact.lib.config.LibServerConfig;
import com.impact.lib.dump.DumpSection;
import com.impact.lib.dump.ImpactsDump;
import com.impact.lib.dump.LibPlayerDumpSection;
import com.impact.lib.gametest.MockPlayers;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Lib milestone 2: the /impacts commands, the player dump and "Why am I here?". */
@GameTestHolder(ImpactsLib.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibCommandGameTests {
    private static final String EMPTY_TEMPLATE = "empty";
    private static final Set<String> SAMPLE_MOD_IDS = Set.of("impactslib", "impactsinventory");
    private static final List<String> SAMPLE_CATEGORIES = List.of("hotbar", "locks", "pickup");
    private static final int COMMAND_FAILED = 0;
    private static final int ONE_TARGET = 1;
    private static final int REGULAR_PLAYER_PERMISSION_LEVEL = 0;
    private static final int CHANGED_WOODEN_GRINDER_DURABILITY = 100;

    private enum OverflowTarget { CHEST, DROP }

    private LibCommandGameTests() {
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void resolvesModIdsAndShortNames(GameTestHelper helper) {
        helper.assertTrue(ModArgument.resolve("impactsinventory", SAMPLE_MOD_IDS).equals(Optional.of("impactsinventory")), "A full mod ID should resolve");
        helper.assertTrue(ModArgument.resolve("inventory", SAMPLE_MOD_IDS).equals(Optional.of("impactsinventory")), "A short name should resolve");
        helper.assertTrue(ModArgument.resolve("food", SAMPLE_MOD_IDS).isEmpty(), "An unknown mod should not resolve");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void parsesTypedConfigValues(GameTestHelper helper) {
        helper.assertTrue(ConfigTextFormat.parseBoolean("TRUE").equals(Optional.of(true)), "Booleans ignore case");
        helper.assertTrue(ConfigTextFormat.parseBoolean("yes").isEmpty(), "Only true and false are booleans");
        helper.assertTrue(ConfigTextFormat.parseInteger(" 42 ").equals(Optional.of(42)), "Integers may have spaces around them");
        helper.assertTrue(ConfigTextFormat.parseInteger("4.2").isEmpty(), "A decimal is not an integer");
        helper.assertTrue(ConfigTextFormat.parseDouble("0.1").equals(Optional.of(0.1)), "Decimals parse");
        helper.assertTrue(ConfigTextFormat.parseDouble("NaN").isEmpty(), "Values must be finite numbers");
        helper.assertTrue(ConfigTextFormat.parseStringList("[a=1, b=2]").equals(Optional.of(List.of("a=1", "b=2"))), "Lists parse with brackets");
        helper.assertTrue(ConfigTextFormat.parseStringList("").equals(Optional.of(List.of())), "Empty text is an empty list");
        helper.assertTrue(ConfigTextFormat.parseEnum(OverflowTarget.class, "drop").equals(Optional.of(OverflowTarget.DROP)), "Enum names ignore case");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void changesDebugCategoryLists(GameTestHelper helper) {
        List<String> everyCategory = List.of(DebugConfigSection.ALL_CATEGORIES);
        helper.assertTrue(DebugConfigSection.categoriesWith(everyCategory, "hotbar").equals(everyCategory), "Turning a category on keeps \"all\"");
        helper.assertTrue(DebugConfigSection.categoriesWith(List.of("pickup"), "hotbar").equals(List.of("pickup", "hotbar")), "Turning a category on adds it");
        helper.assertTrue(DebugConfigSection.categoriesWithout(everyCategory, "hotbar", SAMPLE_CATEGORIES).equals(List.of("locks", "pickup")),
                "Turning a category off expands \"all\" into the other categories");
        helper.assertTrue(DebugConfigSection.categoriesWithout(List.of("pickup", "hotbar"), "hotbar", SAMPLE_CATEGORIES).equals(List.of("pickup")),
                "Turning a category off removes it");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void runsDebugCommands(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Commands commands = server.getCommands();
        CommandSourceStack operator = server.createCommandSourceStack().withSuppressedOutput();
        helper.assertTrue(commands.performPrefixedCommand(operator, "impacts debug") >= ONE_TARGET, "/impacts debug should list every mod");
        helper.assertTrue(commands.performPrefixedCommand(operator, "impacts debug lib") == ONE_TARGET, "The short name lib should work");
        helper.assertTrue(commands.performPrefixedCommand(operator, "impacts debug impactslib on startup") == ONE_TARGET, "Turning a category on should work");
        helper.assertTrue(DebugConfigSection.forMod(ImpactsLib.MOD_ID).orElseThrow().isEnabled.get(), "Turning a category on keeps debug on");
        helper.assertTrue(commands.performPrefixedCommand(operator, "impacts debug impactslib on no_such_category") == COMMAND_FAILED,
                "An unknown category should be refused");
        helper.assertTrue(commands.performPrefixedCommand(operator, "impacts debug no_such_mod") == COMMAND_FAILED, "An unknown mod should be refused");
        CommandSourceStack regularPlayer = operator.withPermission(REGULAR_PLAYER_PERMISSION_LEVEL);
        helper.assertTrue(commands.performPrefixedCommand(regularPlayer, "impacts debug") == COMMAND_FAILED,
                "Players below commands.permissionLevel should not be able to use /impacts");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void showsAndChangesServerConfigValues(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Commands commands = server.getCommands();
        CommandSourceStack operator = server.createCommandSourceStack().withSuppressedOutput();
        ConfigValue<Integer> woodenDurability = LibServerConfig.get().grinder.woodenDurability;
        int originalDurability = woodenDurability.get();
        try {
            helper.assertTrue(commands.performPrefixedCommand(operator, "impacts config lib") > COMMAND_FAILED, "Listing Lib's server values should work");
            helper.assertTrue(commands.performPrefixedCommand(operator, "impacts config lib grinder.woodenDurability") == ONE_TARGET, "Showing one value should work");
            helper.assertTrue(commands.performPrefixedCommand(operator, "impacts config lib grinder.woodenDurability 0") == COMMAND_FAILED, "An out-of-range value should be refused");
            helper.assertTrue(commands.performPrefixedCommand(operator, "impacts config lib grinder.woodenDurability lots") == COMMAND_FAILED, "Text that isn't a number should be refused");
            helper.assertTrue(commands.performPrefixedCommand(operator, "impacts config lib grinder.noSuchKey 5") == COMMAND_FAILED, "An unknown key should be refused");
            helper.assertTrue(commands.performPrefixedCommand(operator, "impacts config lib grinder.woodenDurability " + CHANGED_WOODEN_GRINDER_DURABILITY) == ONE_TARGET,
                    "A valid value should be accepted");
            helper.assertTrue(woodenDurability.get() == CHANGED_WOODEN_GRINDER_DURABILITY, "The new value should apply right away");
        } finally {
            woodenDurability.set(originalDurability);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void dumpsPlayerWithLibSection(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            List<DumpSection> sections = ImpactsDump.describePlayer(player);
            helper.assertTrue(sections.get(0).name().equals(LibPlayerDumpSection.SECTION_NAME), "Lib's section should come first");
            helper.assertTrue(sections.get(0).entries().containsKey("dimension"), "Lib's section should show the dimension");
            CommandSourceStack operator = server.createCommandSourceStack().withSuppressedOutput();
            int result = server.getCommands().performPrefixedCommand(operator, "impacts dump player " + player.getGameProfile().getName());
            helper.assertTrue(result == ONE_TARGET, "/impacts dump player should succeed");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void grantsWhyAmIHereOnlyWhenLibIsAlone(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Advancement whyAmIHere = server.getAdvancements().getAdvancement(LibAdvancements.WHY_AM_I_HERE_ID);
        boolean isLibAlone = LoneLibAchievement.isLibAlone();
        helper.assertTrue((whyAmIHere != null) == isLibAlone, "\"Why am I here?\" should exist only when Lib is the only Impact mod");
        // Joining the world fires the login hook that grants the advancement.
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            boolean isGranted = whyAmIHere != null && player.getAdvancements().getOrStartProgress(whyAmIHere).isDone();
            helper.assertTrue(isGranted == isLibAlone, "\"Why am I here?\" should be granted on login only when Lib is alone");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }
}
