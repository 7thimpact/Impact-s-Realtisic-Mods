package com.impact.realisticinventory.client;

import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.config.InventoryClientConfig;
import com.impact.realisticinventory.pack.PackItem;
import com.impact.realisticinventory.pack.PackKind;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * Draws a worn pack on its wearer's back: the leather in its dye color, then the buckles and frame as drawn. It moves
 * out by a pixel over a chestplate, and {@code packs.renderOnPlayer} (client config) turns it off. Curios calls it for
 * the back slot, so Curios's own render toggle applies too.
 */
public final class WornPackRenderer {
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float FULL_ALPHA = 1.0F;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int CHANNEL_MASK = 0xFF;
    private static final float CHANNEL_MAXIMUM = 255.0F;

    // Baked on first use, after the game has loaded its entity models.
    private final Map<PackKind, ModelPart> modelsByKind = new EnumMap<>(PackKind.class);

    /** Draws the pack; the pose must already follow the wearer's body (its torso's position and rotation). */
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, LivingEntity wearer, ItemStack worn) {
        boolean isShown = worn.getItem() instanceof PackItem && !wearer.isInvisible() && InventoryClientConfig.get().packs.isRenderedOnPlayer.get();
        if (!isShown) {
            return;
        }
        poseStack.pushPose();
        if (!wearer.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            poseStack.translate(0.0F, 0.0F, PIXEL);
        }
        drawPack(poseStack, buffers, packedLight, worn);
        poseStack.popPose();
    }

    /** Draws the pack model in model space (as on a torso): dyed leather, then the undyed buckles and frame. */
    public void drawPack(PoseStack poseStack, MultiBufferSource buffers, int packedLight, ItemStack packStack) {
        if (!(packStack.getItem() instanceof PackItem pack)) {
            return;
        }
        ModelPart model = modelsByKind.computeIfAbsent(pack.kind(),
                kind -> Minecraft.getInstance().getEntityModels().bakeLayer(WornPackModels.layerFor(kind)));
        int color = pack.getColor(packStack);
        float red = (color >> RED_SHIFT & CHANNEL_MASK) / CHANNEL_MAXIMUM;
        float green = (color >> GREEN_SHIFT & CHANNEL_MASK) / CHANNEL_MAXIMUM;
        float blue = (color & CHANNEL_MASK) / CHANNEL_MAXIMUM;
        model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(texture(pack.kind(), false))), packedLight,
                OverlayTexture.NO_OVERLAY, red, green, blue, FULL_ALPHA);
        model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(texture(pack.kind(), true))), packedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, FULL_ALPHA);
    }

    private static ResourceLocation texture(PackKind kind, boolean isOverlay) {
        String suffix = isOverlay ? "_overlay" : "";
        return ResourceIds.of(InventoryMod.MOD_ID, "textures/entity/pack/" + kind.itemPath() + suffix + ".png");
    }
}
