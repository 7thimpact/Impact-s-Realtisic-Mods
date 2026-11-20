package com.impact.realisticcrafting.forge;

import com.impact.realisticcrafting.loot.PebbleDrops;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;

/** Crafting's game events on Forge's event bus. */
final class CraftingForgeEvents {
    private CraftingForgeEvents() {
    }

    static void register() {
        // Last, so a break another mod cancels never drops a pebble.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, CraftingForgeEvents::onBlockBroken);
    }

    // Stone broken by hand rolls no loot at all, so its pebble comes from here instead of the loot modifier.
    private static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof Level level)) {
            return;
        }
        int pebbles = PebbleDrops.fromHandBreak(event.getState(), event.getPlayer());
        if (pebbles > 0) {
            Block.popResource(level, event.getPos(), new ItemStack(CraftingItems.stonePebble(), pebbles));
        }
    }
}
