package com.impact.realisticinventory.client;

import com.impact.realisticinventory.pack.PackItem;
import com.impact.realisticinventory.pack.PackKind;
import com.impact.realisticinventory.pack.PlacedPackBlock;
import com.impact.realisticinventory.pack.PlacedPackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * Draws a placed pack with the same model as a worn one, standing upright in the middle of its block with its front
 * (the side away from the wearer's back) facing the way it was set down.
 */
public final class PlacedPackRenderer implements BlockEntityRenderer<PlacedPackBlockEntity> {
    private static final float PIXELS_PER_BLOCK = 16.0F;
    private static final float BLOCK_CENTER = 0.5F;

    private final WornPackRenderer packRenderer = new WornPackRenderer();

    public PlacedPackRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PlacedPackBlockEntity placedPack, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                       int packedOverlay) {
        ItemStack pack = placedPack.pack();
        if (!(pack.getItem() instanceof PackItem packItem)) {
            return;
        }
        PackKind kind = packItem.kind();
        Direction facing = placedPack.getBlockState().getValue(PlacedPackBlock.FACING);
        poseStack.pushPose();
        poseStack.translate(BLOCK_CENTER, 0.0F, BLOCK_CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(0.0F, WornPackModels.placedBottomPixels(kind) / PIXELS_PER_BLOCK,
                -WornPackModels.placedDepthCenterPixels(kind) / PIXELS_PER_BLOCK);
        // Entity models grow downward; this turns the model upright without mirroring it.
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        packRenderer.drawPack(poseStack, buffers, packedLight, pack);
        poseStack.popPose();
    }
}
