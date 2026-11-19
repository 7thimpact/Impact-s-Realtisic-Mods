package com.impact.lib.forge.crafting;

import com.google.gson.JsonObject;
import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import java.util.stream.Stream;

/**
 * {@code impactslib:water_bottle}: a vanilla water bottle, or anything in {@code #impactslib:water_bottles} (Vitality adds
 * its Purified and Unclean Water). Food's Dough uses it.
 */
public final class WaterBottleIngredient extends AbstractIngredient {
    public static final ResourceLocation ID = ResourceIds.of(ImpactsLib.MOD_ID, "water_bottle");
    public static final TagKey<Item> WATER_BOTTLES = TagKey.create(Registries.ITEM, ResourceIds.of(ImpactsLib.MOD_ID, "water_bottles"));
    public static final WaterBottleIngredient INSTANCE = new WaterBottleIngredient();

    private WaterBottleIngredient() {
        super(Stream.of(new Ingredient.ItemValue(vanillaWaterBottle()), new Ingredient.TagValue(WATER_BOTTLES)));
    }

    @Override
    public boolean test(ItemStack stack) {
        boolean isVanillaWaterBottle = stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
        return isVanillaWaterBottle || stack.is(WATER_BOTTLES);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<WaterBottleIngredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", ID.toString());
        return json;
    }

    private static ItemStack vanillaWaterBottle() {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
    }

    /** The ingredient has no fields; its JSON is only its type. */
    public static final class Serializer implements IIngredientSerializer<WaterBottleIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public WaterBottleIngredient parse(FriendlyByteBuf buffer) {
            return WaterBottleIngredient.INSTANCE;
        }

        @Override
        public WaterBottleIngredient parse(JsonObject json) {
            return WaterBottleIngredient.INSTANCE;
        }

        @Override
        public void write(FriendlyByteBuf buffer, WaterBottleIngredient ingredient) {
        }
    }
}
