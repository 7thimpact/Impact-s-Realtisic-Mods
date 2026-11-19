package com.impact.realisticinventory.client;

import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.config.InventoryClientConfig;
import com.impact.realisticinventory.pack.BackSlotStorage;
import com.impact.realisticinventory.pack.PackItem;
import com.impact.realisticinventory.pack.PackKind;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * Draws the worn pack on the player's back: the leather in its dye color, then the buckles and frame as drawn. It moves
 * out by a pixel over a chestplate, and can be turned off with {@code packs.renderOnPlayer} (client config).
 */
public final class WornPackLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float FULL_ALPHA = 1.0F;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int CHANNEL_MASK = 0xFF;
    private static final float CHANNEL_MAXIMUM = 255.0F;

    private final Map<PackKind, ModelPart> modelsByKind = new EnumMap<>(PackKind.class);

    public WornPackLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer, EntityModelSet models) {
        super(renderer);
        for (PackKind kind : PackKind.values()) {
            modelsByKind.put(kind, models.bakeLayer(WornPackModels.layerFor(kind)));
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float headYaw, float headPitch) {
        ItemStack worn = BackSlotStorage.get(player);
        boolean isShown = worn.getItem() instanceof PackItem && !player.isInvisible() && InventoryClientConfig.get().packs.isRenderedOnPlayer.get();
        if (!isShown) {
            return;
        }
        PackItem pack = (PackItem) worn.getItem();
        ModelPart model = modelsByKind.get(pack.kind());
        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            poseStack.translate(0.0F, 0.0F, PIXEL);
        }
        int color = pack.getColor(worn);
        float red = (color >> RED_SHIFT & CHANNEL_MASK) / CHANNEL_MAXIMUM;
        float green = (color >> GREEN_SHIFT & CHANNEL_MASK) / CHANNEL_MAXIMUM;
        float blue = (color & CHANNEL_MASK) / CHANNEL_MAXIMUM;
        model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(texture(pack.kind(), false))), packedLight,
                OverlayTexture.NO_OVERLAY, red, green, blue, FULL_ALPHA);
        model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(texture(pack.kind(), true))), packedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, FULL_ALPHA);
        poseStack.popPose();
    }

    private static ResourceLocation texture(PackKind kind, boolean isOverlay) {
        String suffix = isOverlay ? "_overlay" : "";
        return ResourceIds.of(InventoryMod.MOD_ID, "textures/entity/pack/" + kind.itemPath() + suffix + ".png");
    }
}
