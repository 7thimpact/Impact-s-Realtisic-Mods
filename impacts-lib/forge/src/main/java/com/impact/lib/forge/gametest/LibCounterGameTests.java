package com.impact.lib.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.CountTrigger;
import com.impact.lib.advancement.ImpactsAdvancementBuilder;
import com.impact.lib.attachment.DeathCopyRule;
import com.impact.lib.attachment.PlayerAttachment;
import com.impact.lib.attachment.PlayerAttachments;
import com.impact.lib.counter.ImpactsCounters;
import com.impact.lib.counter.PlayerCounters;
import com.impact.lib.gametest.MockPlayers;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Lib milestone 4: player attachments, counters, the count trigger and the advancement conventions. */
@GameTestHolder(ImpactsLib.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibCounterGameTests {
    private static final String EMPTY_TEMPLATE = "empty";
    private static final String TEST_COUNTER = "impactslibtest:meals";
    private static final int THREE_MEALS = 3;
    private static final int TWO_MEALS = 2;
    private static final PlayerAttachment<PlayerCounters> RESET_ON_DEATH = PlayerAttachments.register(new PlayerAttachment<>(
            ResourceIds.of("impactslibtest", "reset_on_death"), PlayerCounters::new, DeathCopyRule.NEVER));

    private LibCounterGameTests() {
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void countsTotalsAndItems(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            ImpactsCounters.add(player, TEST_COUNTER, Items.BREAD, THREE_MEALS);
            ImpactsCounters.add(player, TEST_COUNTER, Items.APPLE, TWO_MEALS);
            helper.assertTrue(ImpactsCounters.get(player, TEST_COUNTER, null) == THREE_MEALS + TWO_MEALS, "The total should add every item");
            helper.assertTrue(ImpactsCounters.get(player, TEST_COUNTER, Items.BREAD) == THREE_MEALS, "Bread should have its own count");
            PlayerCounters counters = ImpactsCounters.all(player);
            helper.assertTrue(counters.highestItemCount(TEST_COUNTER) == THREE_MEALS, "The highest single item is bread");
            helper.assertTrue(counters.distinctItemCount(TEST_COUNTER) == 2, "Two different items were counted");
            helper.assertTrue(CountTrigger.TriggerInstance.anyItem(TEST_COUNTER, THREE_MEALS).matches(counters, TEST_COUNTER), "Any item at 3 should match");
            helper.assertFalse(CountTrigger.TriggerInstance.total(TEST_COUNTER, 10).matches(counters, TEST_COUNTER), "A total of 10 is not reached");
            helper.assertTrue(CountTrigger.TriggerInstance.distinctItems(TEST_COUNTER, 2).matches(counters, TEST_COUNTER), "Two distinct items should match");
            ImpactsCounters.reset(player, TEST_COUNTER);
            helper.assertTrue(ImpactsCounters.get(player, TEST_COUNTER, null) == 0, "A reset counter starts over");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void savesAttachmentsWithThePlayer(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        CompoundTag savedPlayer;
        try {
            ImpactsCounters.add(player, TEST_COUNTER, Items.BREAD, THREE_MEALS);
            savedPlayer = player.saveWithoutId(new CompoundTag());
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.assertTrue(savedPlayer.contains(PlayerAttachments.NBT_KEY), "Attachments should be saved in the player's NBT");
        ServerPlayer reloadedPlayer = MockPlayers.joinWorld(helper.getLevel());
        try {
            reloadedPlayer.load(savedPlayer);
            helper.assertTrue(ImpactsCounters.get(reloadedPlayer, TEST_COUNTER, Items.BREAD) == THREE_MEALS, "Counters should load back");
        } finally {
            MockPlayers.leaveWorld(reloadedPlayer);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void followsDeathCopyRules(GameTestHelper helper) {
        ServerPlayer oldPlayer = MockPlayers.joinWorld(helper.getLevel());
        ServerPlayer newPlayer = MockPlayers.joinWorld(helper.getLevel());
        try {
            ImpactsCounters.add(oldPlayer, TEST_COUNTER, null, THREE_MEALS);
            PlayerAttachments.get(oldPlayer, RESET_ON_DEATH).add(TEST_COUNTER, null, TWO_MEALS);
            PlayerAttachments.copyOnRespawn(oldPlayer, newPlayer, false);
            helper.assertTrue(ImpactsCounters.get(newPlayer, TEST_COUNTER, null) == THREE_MEALS, "Counters are always kept through death");
            helper.assertTrue(PlayerAttachments.get(newPlayer, RESET_ON_DEATH).total(TEST_COUNTER) == 0, "A NEVER attachment resets on death");
            PlayerAttachments.copyOnRespawn(oldPlayer, newPlayer, true);
            helper.assertTrue(PlayerAttachments.get(newPlayer, RESET_ON_DEATH).total(TEST_COUNTER) == TWO_MEALS,
                    "Returning from the End keeps every attachment");
        } finally {
            MockPlayers.leaveWorld(oldPlayer);
            MockPlayers.leaveWorld(newPlayer);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void namesAdvancementTranslationKeys(GameTestHelper helper) {
        String titleKey = ImpactsAdvancementBuilder.titleKey(ResourceIds.of("impactsfood", "boiling/hard_boiled"));
        helper.assertTrue(titleKey.equals("advancements.impactsfood.boiling.hard_boiled.title"), "Unexpected title key: " + titleKey);
        helper.succeed();
    }
}
