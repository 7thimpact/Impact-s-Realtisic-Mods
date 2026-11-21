package com.impact.realisticvitality.forge.compat.jei;

import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.AirTank;
import com.impact.realisticvitality.air.BreathingMask;
import com.impact.realisticvitality.lang.VitalityTranslations;
import com.impact.realisticvitality.registry.VitalityBlocks;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.WaterContainer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;
import java.util.List;

/**
 * Vitality's JEI plugin: info pages on water and its purity, sipping, the containers, the water blocks, breathing gear,
 * the Thermometer, the Straw Hat and linings. Recipes show in vanilla's categories. JEI finds it on its own.
 */
@JeiPlugin
public final class VitalityJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceIds.of(VitalityMod.MOD_ID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ItemStack> waters = Arrays.stream(BottledWater.values()).map(VitalityItems::bottle).toList();
        info(registration, waters, "water");
        info(registration, List.of(VitalityItems.bottle(BottledWater.UNCLEAN)), "sipping");
        info(registration, Arrays.stream(WaterContainer.values()).map(container -> new ItemStack(VitalityItems.container(container))).toList(), "containers");
        info(registration, stacks(VitalityItems.container(WaterContainer.WOODEN_CUP)), "wooden_cup");
        info(registration, stacks(VitalityItems.container(WaterContainer.INSULATED_FLASK)), "insulated_flask");
        info(registration, stacks(VitalityItems.container(WaterContainer.TIDEBOUND_CANTEEN)), "tidebound_canteen");
        info(registration, stacks(VitalityItems.container(WaterContainer.HYDRATION_PACK)), "hydration_pack");
        info(registration, stacks(VitalityBlocks.rainBarrel()), "rain_barrel");
        info(registration, stacks(VitalityBlocks.waterFilter()), "water_filter");
        info(registration, stacks(VitalityBlocks.copperStill()), "copper_still");
        info(registration, stacks(VitalityItems.charcoalFilter()), "charcoal_filter");
        info(registration, stacks(VitalityItems.mask(BreathingMask.CLOTH_MASK)), "cloth_mask");
        info(registration, stacks(VitalityItems.mask(BreathingMask.RESPIRATOR), VitalityItems.mask(BreathingMask.REBREATHER)), "respirator");
        info(registration, stacks(VitalityItems.mask(BreathingMask.NETHERITE_REBREATHER)), "netherite_rebreather");
        info(registration, stacks(VitalityItems.airBladder()), "air_bladder");
        info(registration, stacks(VitalityItems.bottledAir()), "bottled_air");
        info(registration, stacks(VitalityItems.tank(AirTank.AIR_TANK), VitalityItems.tank(AirTank.SHULKER_AIR_TANK)), "air_tank");
        info(registration, stacks(VitalityBlocks.airCompressor()), "air_compressor");
        info(registration, stacks(VitalityItems.thermometer()), "thermometer");
        info(registration, stacks(VitalityItems.strawHat()), "straw_hat");
        info(registration, stacks(Items.WHITE_WOOL, Items.RABBIT_HIDE, Items.LEATHER), "linings");
    }

    private static void info(IRecipeRegistration registration, List<ItemStack> stacks, String page) {
        registration.addIngredientInfo(stacks, VanillaTypes.ITEM_STACK, Component.translatable(VitalityTranslations.jeiInfo(page)));
    }

    private static List<ItemStack> stacks(ItemLike... items) {
        return Arrays.stream(items).map(ItemStack::new).toList();
    }
}
