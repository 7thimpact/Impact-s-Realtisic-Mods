package com.impact.realisticcrafting.registry;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.LoosePebbleBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

/** Crafting's blocks. */
public final class CraftingBlocks {
    private static volatile Supplier<LoosePebbleBlock> loosePebble;

    private CraftingBlocks() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(CraftingMod.MOD_ID);
        loosePebble = registrar.register(Registries.BLOCK, "loose_pebble", () -> new LoosePebbleBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE).noCollission().instabreak().sound(SoundType.STONE).offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY)));
    }

    /** Natural pebble clusters. There's no block item: players can't place them. */
    public static LoosePebbleBlock loosePebble() {
        return loosePebble.get();
    }
}
