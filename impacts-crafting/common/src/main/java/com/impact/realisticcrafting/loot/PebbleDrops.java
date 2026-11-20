package com.impact.realisticcrafting.loot;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Pebbles from broken blocks: gravel drops one by chance (25%), stone with a pickaxe by chance (15%, on top of its own
 * drop), and stone broken by hand, which drops nothing else, always drops one. Silk Touch gives none; Fortune doesn't
 * matter. Dropped pebbles are always unused.
 */
public final class PebbleDrops {
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);

    private PebbleDrops() {
    }

    /** Extra pebbles when the block drops its loot normally (gravel, or stone with the right tool). */
    public static int fromLoot(BlockState state, ItemStack tool, RandomSource random) {
        if (EnchantmentHelper.hasSilkTouch(tool)) {
            return 0;
        }
        CraftingServerConfig.PebbleSettings settings = CraftingServerConfig.get().pebbles;
        double chance = 0.0;
        if (state.is(CraftingTags.PEBBLE_GRAVEL)) {
            chance = settings.gravelChance.get();
        } else if (dropsPebbles(state) && !tool.isEmpty()) {
            // Explosions roll stone's loot with no tool; only a pickaxe earns the stone chance.
            chance = settings.stonePickaxeChance.get();
        }
        boolean drops = chance > 0.0 && random.nextDouble() < chance;
        LOG.verbose(CraftingLogCategories.PEBBLES, "pebble roll", "block", ResourceIds.blockId(state.getBlock()), "chance", chance, "drops", drops);
        if (drops) {
            log(state, tool, "loot");
        }
        return drops ? 1 : 0;
    }

    /** Pebbles when a player breaks stone without the tool that makes it drop: always one. */
    public static int fromHandBreak(BlockState state, Player player) {
        boolean isHandBreak = dropsPebbles(state) && !player.hasCorrectToolForDrops(state) && !player.getAbilities().instabuild;
        if (!isHandBreak || !CraftingServerConfig.get().pebbles.doesHandBreakingDrop.get()) {
            return 0;
        }
        log(state, player.getMainHandItem(), "hand");
        return 1;
    }

    public static boolean dropsPebbles(BlockState state) {
        return state.is(CraftingTags.PEBBLE_BASE) && !state.is(CraftingTags.PEBBLE_WORLDGEN_ONLY);
    }

    private static void log(BlockState state, ItemStack tool, String source) {
        LOG.debug(CraftingLogCategories.PEBBLES, "pebble dropped", "block", ResourceIds.blockId(state.getBlock()),
                "tool", tool.isEmpty() ? "hand" : ResourceIds.itemId(tool.getItem()), "source", source);
    }
}
