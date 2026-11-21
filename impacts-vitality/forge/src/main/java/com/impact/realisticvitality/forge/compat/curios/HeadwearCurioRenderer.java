package com.impact.realisticvitality.forge.compat.curios;

import com.impact.lib.ResourceIds;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Draws a mask or hat worn in Curios's head slot the way the helmet slot would: the item's own armor model (or
 * vanilla's outer armor layer) with only the head showing, in the item's armor texture, following the wearer's head.
 */
final class HeadwearCurioRenderer implements ICurioRenderer {
    private static final String ARMOR_TEXTURE_FORMAT = "%s:textures/models/armor/%s_layer_1.png";
    private static final String DEFAULT_NAMESPACE = "minecraft";
    private HumanoidModel<LivingEntity> outerArmorModel;

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack,
                                                                       RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffers,
                                                                       int light, float limbSwing, float limbSwingAmount, float partialTicks,
                                                                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(stack.getItem() instanceof ArmorItem armorItem) || !(renderLayerParent.getModel() instanceof HumanoidModel<?> wearerModel)) {
            return;
        }
        LivingEntity wearer = slotContext.entity();
        Model model = ForgeHooksClient.getArmorModel(wearer, stack, EquipmentSlot.HEAD, outerArmorModel());
        if (model instanceof HumanoidModel<?> headModel) {
            copyPose(wearerModel, headModel);
            headModel.setAllVisible(false);
            headModel.head.visible = true;
            headModel.hat.visible = true;
        }
        ResourceLocation texture = armorTexture(wearer, stack, armorItem);
        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(buffers, RenderType.armorCutoutNoCull(texture), false, stack.hasFoil());
        model.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private HumanoidModel<LivingEntity> outerArmorModel() {
        if (outerArmorModel == null) {
            outerArmorModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
        }
        return outerArmorModel;
    }

    // Vanilla's rule for an armor material's texture, then the item's own say through Forge.
    private static ResourceLocation armorTexture(LivingEntity wearer, ItemStack stack, ArmorItem armorItem) {
        String materialName = armorItem.getMaterial().getName();
        int separator = materialName.indexOf(':');
        String namespace = separator == -1 ? DEFAULT_NAMESPACE : materialName.substring(0, separator);
        String path = separator == -1 ? materialName : materialName.substring(separator + 1);
        String defaultTexture = String.format(ARMOR_TEXTURE_FORMAT, namespace, path);
        String texture = ForgeHooksClient.getArmorTexture(wearer, stack, defaultTexture, EquipmentSlot.HEAD, null);
        return ResourceIds.tryParse(texture).orElseGet(() -> ResourceIds.tryParse(defaultTexture).orElseThrow());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyPose(HumanoidModel wearerModel, HumanoidModel headModel) {
        wearerModel.copyPropertiesTo(headModel);
    }
}
