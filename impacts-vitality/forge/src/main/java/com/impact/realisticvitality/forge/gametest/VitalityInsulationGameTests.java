package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.temperature.ArmorLining;
import com.impact.realisticvitality.temperature.ComfortRange;
import com.impact.realisticvitality.temperature.Insulation;
import com.impact.realisticvitality.temperature.Insulations;
import com.impact.realisticvitality.temperature.Linings;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Map;
import java.util.Optional;

/** Vitality milestone 7: armor insulates by its tags, linings sew in at an anvil keeping the armor's NBT, and the clothing items exist. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityInsulationGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;
    private static final int CHESTPLATE_DAMAGE = 37;
    private static final double TOLERANCE = 0.001;

    private VitalityInsulationGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void ironArmorNarrowsTheComfortableRange(GameTestHelper helper) {
        Insulation iron = wear(helper, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        ComfortRange range = ComfortRange.of(iron);
        helper.assertTrue(Math.abs(range.coldEdge() - 62.0) < TOLERANCE && Math.abs(range.hotEdge() - 68.0) < TOLERANCE,
                "A full set of iron armor narrows the range to 62–68°F, got " + range);
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void leatherWarmsAndChainmailCools(GameTestHelper helper) {
        Insulation leather = wear(helper, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
        helper.assertTrue(Math.abs(leather.warmth() - 9.0) < TOLERANCE && leather.cooling() == 0.0, "Leather gives 2 warmth a piece, 3 for the chestplate");
        Insulation chainmail = wear(helper, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
        helper.assertTrue(chainmail.warmth() == 0.0 && Math.abs(chainmail.cooling() - 4.0) < TOLERANCE, "Chainmail gives 1 cooling a piece");
        Insulation diamond = wear(helper, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        helper.assertTrue(diamond.equals(Insulation.NONE), "Diamond armor doesn't insulate");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void liningsAddTheirInsulation(GameTestHelper helper) {
        ItemStack fur = ArmorLining.FUR.sewnInto(new ItemStack(Items.DIAMOND_BOOTS));
        helper.assertTrue(Insulations.ofPiece(fur, EquipmentSlot.FEET).equals(new Insulation(3.0, 0.0)), "A Fur Lining adds 3 warmth");
        ItemStack leather = ArmorLining.LEATHER.sewnInto(new ItemStack(Items.IRON_HELMET));
        helper.assertTrue(Insulations.ofPiece(leather, EquipmentSlot.HEAD).equals(new Insulation(-1.0, 1.0)), "A Leather Lining's 2 cooling on iron's −1");
        ItemStack relined = ArmorLining.WOOL.sewnInto(fur);
        helper.assertTrue(ArmorLining.of(relined).orElseThrow() == ArmorLining.WOOL, "A new lining replaces the old one");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void anvilLiningKeepsEnchantmentsAndDamage(GameTestHelper helper) {
        ItemStack chestplate = new ItemStack(Items.IRON_CHESTPLATE);
        EnchantmentHelper.setEnchantments(Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 3), chestplate);
        chestplate.setDamageValue(CHESTPLATE_DAMAGE);
        Optional<Linings.AnvilLining> result = Linings.anvilResult(chestplate, new ItemStack(Items.WHITE_WOOL, 3));
        if (ImpactsLib.isLoaded(VitalityMod.CRAFTING_MOD_ID)) {
            helper.assertTrue(result.isEmpty(), "With Crafting, linings are sewn with the Bone Needle instead of at an anvil");
            helper.succeed();
            return;
        }
        helper.assertTrue(result.isPresent(), "Armor and 3 wool make a Wool Lining at the anvil");
        ItemStack lined = result.get().lined();
        helper.assertTrue(ArmorLining.of(lined).orElseThrow() == ArmorLining.WOOL, "The lining is sewn in");
        helper.assertTrue(lined.getEnchantmentLevel(Enchantments.ALL_DAMAGE_PROTECTION) == 3, "Enchantments are kept");
        helper.assertTrue(lined.getDamageValue() == CHESTPLATE_DAMAGE, "Damage is kept");
        helper.assertTrue(result.get().levels() == 1 && result.get().materialCount() == 3, "It costs 1 level and 3 wool");
        helper.assertTrue(Linings.anvilResult(chestplate, new ItemStack(Items.WHITE_WOOL, 2)).isEmpty(), "2 wool isn't enough");
        helper.assertTrue(Linings.anvilResult(new ItemStack(Items.STICK), new ItemStack(Items.WHITE_WOOL, 3)).isEmpty(), "Only armor takes linings");
        helper.assertTrue(Linings.anvilResult(chestplate, new ItemStack(Items.RABBIT_HIDE, 4)).orElseThrow().lined()
                .is(Items.IRON_CHESTPLATE), "4 rabbit hide make a Fur Lining");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void strawHatIsAHelmetAndTheThermometerExists(GameTestHelper helper) {
        helper.assertTrue(VitalityItems.strawHat().getType() == ArmorItem.Type.HELMET, "The Straw Hat is worn on the head");
        helper.assertTrue(VitalityItems.strawHat().getDefense() == 0, "It gives shade, not armor");
        helper.assertTrue(new ItemStack(VitalityItems.thermometer()).getMaxStackSize() == 1, "The Thermometer is a single tool");
        helper.succeed();
    }

    private static Insulation wear(GameTestHelper helper, Item head, Item chest, Item legs, Item feet) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(head));
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chest));
            player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(legs));
            player.setItemSlot(EquipmentSlot.FEET, new ItemStack(feet));
            return Insulations.of(player);
        } finally {
            MockPlayers.leaveWorld(player);
        }
    }
}
