package com.impact.realisticcrafting.recipe;

import com.impact.lib.hold.HoldAction;
import com.impact.lib.hold.HoldMotion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Hammering as Lib's hold-to-use action: one cycle, as long as the recipe takes, with the loose item in the main hand. */
public final class HammeringAction implements HoldAction {
    @Override
    public ResourceLocation id() {
        return CraftingRecipeTypes.HAMMERING_ID;
    }

    @Override
    public int cycleTicks(Player player, ItemStack held) {
        return Hammering.recipeFor(player.level(), held, player.getOffhandItem())
                .map(recipe -> Hammering.ticksFor(recipe, player.getOffhandItem()))
                .orElse(Integer.MAX_VALUE);
    }

    @Override
    public boolean canContinue(Player player, ItemStack held) {
        boolean isInMainHand = player.getMainHandItem() == held;
        return isInMainHand && Hammering.recipeFor(player.level(), held, player.getOffhandItem()).isPresent();
    }

    @Override
    public boolean completeCycle(ServerPlayer player, ItemStack held) {
        Hammering.finish(player, held);
        return false;
    }

    @Override
    public HoldMotion motion() {
        return HoldMotion.HAMMER;
    }
}
