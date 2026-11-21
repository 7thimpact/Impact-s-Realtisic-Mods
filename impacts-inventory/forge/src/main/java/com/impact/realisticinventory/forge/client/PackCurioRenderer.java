package com.impact.realisticinventory.forge.client;

import com.impact.realisticinventory.client.WornPackRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** Curios draws worn packs through this: it follows the wearer's torso, then Inventory's renderer draws the pack. */
final class PackCurioRenderer implements ICurioRenderer {
    private final WornPackRenderer packRenderer = new WornPackRenderer();

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack,
                                                                       RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffers,
                                                                       int light, float limbSwing, float limbSwingAmount, float partialTicks,
                                                                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> wearerModel)) {
            return;
        }
        poseStack.pushPose();
        wearerModel.body.translateAndRotate(poseStack);
        packRenderer.render(poseStack, buffers, light, slotContext.entity(), stack);
        poseStack.popPose();
    }
}
