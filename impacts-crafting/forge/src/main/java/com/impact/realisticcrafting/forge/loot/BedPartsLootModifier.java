package com.impact.realisticcrafting.forge.loot;

import com.google.common.base.Suppliers;
import com.impact.realisticcrafting.bed.BedParts;
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

/** {@code impactscrafting:bed_parts}: a broken bed drops its frame, comforter and sheet instead of itself. */
public final class BedPartsLootModifier extends LootModifier {
    public static final String PATH = "bed_parts";
    public static final Supplier<Codec<? extends IGlobalLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.<BedPartsLootModifier>create(instance -> codecStart(instance).apply(instance, BedPartsLootModifier::new)));

    public BedPartsLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state != null) {
            BedParts.replaceBedDrop(state, generatedLoot);
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
