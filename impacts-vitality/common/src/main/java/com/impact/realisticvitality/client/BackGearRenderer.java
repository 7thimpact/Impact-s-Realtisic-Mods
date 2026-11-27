package com.impact.realisticvitality.client;

import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.item.AirTankItem;
import com.impact.realisticvitality.item.HydrationPackItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Draws Vitality's back gear on its wearer's back, looking like the item: a silver or shulker-purple tank, or the
 * leather Hydration Pack. It moves out by a pixel over a chestplate. Curios calls it for its back slot, so Curios's own
 * render toggle applies.
 */
public final class BackGearRenderer {
    private static final float PIXEL = 1.0F / 16.0F;
    private static final String TEXTURE_FOLDER = "textures/entity/back_gear/";
    private static final String TEXTURE_EXTENSION = ".png";

    // Baked on first use, after the game has loaded its entity models.
    private final Map<ModelLayerLocation, ModelPart> modelsByLayer = new HashMap<>();

    /** Draws the gear; the pose must already follow the wearer's body (its torso's position and rotation). */
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, LivingEntity wearer, ItemStack worn) {
        Optional<ModelLayerLocation> layer = layerFor(worn);
        if (layer.isEmpty() || wearer.isInvisible()) {
            return;
        }
        ModelPart model = modelsByLayer.computeIfAbsent(layer.get(), location -> Minecraft.getInstance().getEntityModels().bakeLayer(location));
        poseStack.pushPose();
        if (!wearer.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            poseStack.translate(0.0F, 0.0F, PIXEL);
        }
        model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(textureOf(worn))), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static Optional<ModelLayerLocation> layerFor(ItemStack worn) {
        if (worn.getItem() instanceof AirTankItem) {
            return Optional.of(BackGearModels.AIR_TANK);
        }
        if (worn.getItem() instanceof HydrationPackItem) {
            return Optional.of(BackGearModels.HYDRATION_PACK);
        }
        return Optional.empty();
    }

    // Each item has its own worn texture, named like the item: air_tank, shulker_air_tank, hydration_pack.
    private static ResourceLocation textureOf(ItemStack worn) {
        return ResourceIds.of(VitalityMod.MOD_ID, TEXTURE_FOLDER + ResourceIds.itemId(worn.getItem()).getPath() + TEXTURE_EXTENSION);
    }
}
