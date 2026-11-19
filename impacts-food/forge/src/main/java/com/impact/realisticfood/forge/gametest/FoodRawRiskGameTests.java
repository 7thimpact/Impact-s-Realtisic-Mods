package com.impact.realisticfood.forge.gametest;

import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodCommonConfig;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.raw.RawFoodRisk;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.registry.FoodTags;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Food milestone 8: raw meat and fish can give Hunger, through Forge's finished-eating event, by tag and config. */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodRawRiskGameTests {
    private static final int SPEC_HUNGER_TICKS = 300;

    private FoodRawRiskGameTests() {
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void rawFoodCanGiveHunger(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            ConfigOverrides.withValue(FoodServerConfig.get().rawFood.chance, 1.0, () -> {
                helper.assertTrue(RawFoodRisk.onEaten(player, new ItemStack(Items.BEEF)), "With chance 1, raw beef always gives Hunger");
                MobEffectInstance hunger = player.getEffect(MobEffects.HUNGER);
                helper.assertTrue(hunger != null && hunger.getDuration() == SPEC_HUNGER_TICKS, "Hunger lasts 15 seconds");
                player.removeAllEffects();
                helper.assertFalse(RawFoodRisk.onEaten(player, new ItemStack(Items.COOKED_BEEF)), "Cooked beef is safe");
                helper.assertFalse(RawFoodRisk.onEaten(player, new ItemStack(Items.CHICKEN)), "Raw chicken keeps vanilla's own risk instead");
                ConfigOverrides.withValue(FoodServerConfig.get().rawFood.overrides, List.of("minecraft:salmon=0"), () ->
                        helper.assertFalse(RawFoodRisk.onEaten(player, new ItemStack(Items.SALMON)), "A per-food override of 0 makes salmon safe"));
                ConfigOverrides.withValue(FoodCommonConfig.get().rawFood.isOn, false, () ->
                        helper.assertFalse(RawFoodRisk.onEaten(player, new ItemStack(Items.BEEF)), "rawFood.enabled false turns the risk off"));
                player.removeAllEffects();
                ItemStack rawCut = new ItemStack(FoodItems.piece(FoodPiece.RAW_BEEF_CUT));
                ForgeEventFactory.onItemUseFinish(player, rawCut, 0, ItemStack.EMPTY);
                helper.assertTrue(player.hasEffect(MobEffects.HUNGER), "Forge's finished-eating event applies the risk (here to a raw cut)");
            });
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.assertTrue(FoodServerConfig.get().rawFood.chance.defaultValue() == 0.3, "The chance is 30% by default");
        helper.assertTrue(new ItemStack(FoodItems.piece(FoodPiece.RAW_COD_FILLET)).is(FoodTags.RAW_RISKY), "Raw fillets are risky");
        helper.assertFalse(new ItemStack(FoodItems.piece(FoodPiece.RAW_CHICKEN_PIECE)).is(FoodTags.RAW_RISKY), "Raw chicken pieces aren't in the tag");
        helper.succeed();
    }
}
