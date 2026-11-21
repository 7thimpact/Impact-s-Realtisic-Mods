package com.impact.realisticfood.client;

import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.spit.Spit;
import com.impact.realisticfood.spit.SpitHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

import java.util.List;

/**
 * Draws a campfire's spit across the fire, with its food along it. Each turn rolls the spit half a turn over
 * {@code spit.turnTicks}, worked out from the turn count and when the last turn ends, so no extra syncing is needed.
 */
public final class SpitRenderer {
    private static final double SPIT_HEIGHT = 0.82;
    private static final float SPIT_SCALE = 1.15F;
    // The Spit item's texture runs corner to corner (mirrored by the item's fixed display); this lays it along the spit's axis.
    private static final float TEXTURE_DIAGONAL_DEGREES = 45.0F;
    private static final float FOOD_SCALE = 0.32F;
    private static final double FOOD_SPAN = 0.62;
    private static final float DEGREES_PER_TURN = 180.0F;
    private static final int SEED = 0;

    private SpitRenderer() {
    }

    public static void render(CampfireBlockEntity campfire, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay,
                              ItemRenderer itemRenderer) {
        SpitHolder.spitOf(campfire).ifPresent(spit -> drawSpit(campfire, spit, partialTick, poseStack, buffers, light, overlay, itemRenderer));
    }

    private static void drawSpit(CampfireBlockEntity campfire, Spit spit, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light,
                                 int overlay, ItemRenderer itemRenderer) {
        Direction facing = campfire.getBlockState().getValue(CampfireBlock.FACING);
        float roll = rollDegrees(campfire, spit, partialTick);
        poseStack.pushPose();
        poseStack.translate(0.5, SPIT_HEIGHT, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(roll));
        poseStack.mulPose(Axis.ZP.rotationDegrees(TEXTURE_DIAGONAL_DEGREES));
        poseStack.scale(SPIT_SCALE, SPIT_SCALE, SPIT_SCALE);
        itemRenderer.renderStatic(spit.spitItem(), ItemDisplayContext.FIXED, light, overlay, poseStack, buffers, campfire.getLevel(), SEED);
        poseStack.popPose();
        drawFood(campfire, spit.items(), roll, poseStack, buffers, light, overlay, itemRenderer);
        poseStack.popPose();
    }

    private static void drawFood(CampfireBlockEntity campfire, List<ItemStack> items, float roll, PoseStack poseStack, MultiBufferSource buffers, int light,
                                 int overlay, ItemRenderer itemRenderer) {
        int slots = items.size();
        for (int slot = 0; slot < slots; slot++) {
            ItemStack food = items.get(slot);
            if (food.isEmpty()) {
                continue;
            }
            double along = slots == 1 ? 0.0 : -FOOD_SPAN / 2.0 + FOOD_SPAN * slot / (slots - 1);
            poseStack.pushPose();
            poseStack.translate(along, 0.0, 0.0);
            poseStack.mulPose(Axis.XP.rotationDegrees(roll));
            poseStack.scale(FOOD_SCALE, FOOD_SCALE, FOOD_SCALE);
            itemRenderer.renderStatic(food, ItemDisplayContext.FIXED, light, overlay, poseStack, buffers, campfire.getLevel(), SEED + slot);
            poseStack.popPose();
        }
    }

    // Half a turn per turn, eased over the turn's ticks.
    private static float rollDegrees(CampfireBlockEntity campfire, Spit spit, float partialTick) {
        if (campfire.getLevel() == null) {
            return 0.0F;
        }
        float now = campfire.getLevel().getGameTime() + partialTick;
        float ticksLeft = Math.max(0.0F, spit.turnEndsAt() - now);
        float turnTicks = FoodServerConfig.get().spit.turnTicks.get();
        float unfinished = Math.min(1.0F, ticksLeft / turnTicks);
        return (spit.turns() - unfinished) * DEGREES_PER_TURN;
    }
}
