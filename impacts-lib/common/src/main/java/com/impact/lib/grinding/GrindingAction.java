package com.impact.lib.grinding;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.crafting.Byproduct;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.hold.HoldAction;
import com.impact.lib.hold.HoldMotion;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Grinding as a hold-to-use action: the stick in the main hand, a loaded bowl in the off hand, one item per cycle. */
final class GrindingAction implements HoldAction {
    static final ResourceLocation ID = ResourceIds.of(ImpactsLib.MOD_ID, "grinding");
    private static final int WEAR_PER_ITEM = 1;
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public int cycleTicks(Player player, ItemStack stick) {
        int stickTicks = stick.getItem() instanceof GrindingStickItem stickItem ? stickItem.tier().grindTicks() : GrinderTier.WOOD.grindTicks();
        return recipeFor(player).map(GrindingRecipe::grindTicks).filter(ticks -> ticks > 0).orElse(stickTicks);
    }

    @Override
    public boolean canContinue(Player player, ItemStack stick) {
        return player.getMainHandItem() == stick && recipeFor(player).isPresent();
    }

    @Override
    public boolean completeCycle(ServerPlayer player, ItemStack stick) {
        Optional<GrindingRecipe> recipe = recipeFor(player);
        if (recipe.isEmpty()) {
            return false;
        }
        ItemStack bowl = player.getOffhandItem();
        ItemStack result = recipe.get().assemble(BowlContents.asContainer(bowl), player.level().registryAccess());
        BowlContents.consumeOneOfEach(bowl);
        ImpactsItems.giveOrDrop(player, result.copy());
        recipe.get().byproducts().stream().filter(byproduct -> byproduct.rolls(player.getRandom())).map(Byproduct::stack)
                .forEach(byproduct -> ImpactsItems.giveOrDrop(player, byproduct.copy()));
        bowl.hurtAndBreak(WEAR_PER_ITEM, player, wearer -> wearer.broadcastBreakEvent(InteractionHand.OFF_HAND));
        stick.hurtAndBreak(WEAR_PER_ITEM, player, wearer -> wearer.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        LOG.debug(LibLogCategories.GRINDER, "ground", "player", player.getGameProfile().getName(), "recipe", recipe.get().getId(),
                "result", ResourceIds.itemId(result.getItem()), "count", result.getCount());
        return canContinue(player, player.getMainHandItem());
    }

    @Override
    public HoldMotion motion() {
        return HoldMotion.STIR;
    }

    // The bowl's tier limits the grind, whatever the stick is made of.
    private static Optional<GrindingRecipe> recipeFor(Player player) {
        ItemStack bowl = player.getOffhandItem();
        if (!(bowl.getItem() instanceof GrinderBowlItem bowlItem) || BowlContents.read(bowl).isEmpty()) {
            return Optional.empty();
        }
        return player.level().getRecipeManager().getRecipesFor(Grinding.recipeType(), BowlContents.asContainer(bowl), player.level()).stream()
                .filter(recipe -> bowlItem.tier().reaches(recipe.minimumTier()))
                .findFirst();
    }
}
