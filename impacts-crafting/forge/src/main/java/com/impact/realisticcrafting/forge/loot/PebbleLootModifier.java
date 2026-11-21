package com.impact.realisticcrafting.forge.loot;

import com.google.common.base.Suppliers;
import com.impact.realisticcrafting.loot.PebbleDrops;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import java.util.function.Supplier;

/** {@code impactscrafting:pebble_drops}: adds a pebble by chance to gravel's loot, and to stone's when broken with the right tool. */
public final class PebbleLootModifier extends LootModifier {
    public static final String PATH = "pebble_drops";
    public static final Supplier<Codec<? extends IGlobalLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.<PebbleLootModifier>create(instance -> codecStart(instance).apply(instance, PebbleLootModifier::new)));

    public PebbleLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (state == null) {
            return generatedLoot;
        }
        int pebbles = PebbleDrops.fromLoot(state, tool == null ? ItemStack.EMPTY : tool, context.getRandom());
        if (pebbles > 0) {
            generatedLoot.add(PebbleDrops.pebbleFor(state, pebbles));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
