package com.impact.realisticinventory.client;

import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.PackKind;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * The packs as boxes on the player's back, in the body's coordinates (the torso spans x -4 to 4, y 0 to 12, and its
 * back is at z 2). A satchel is a small bag low on the back, a backpack a box with a front pocket, and a frame pack
 * a tall box held by a wooden frame.
 */
public final class WornPackModels {
    private static final String LAYER_NAME = "main";
    private static final int SATCHEL_TEXTURE_WIDTH = 32;
    private static final int SATCHEL_TEXTURE_HEIGHT = 16;
    private static final int PACK_TEXTURE_WIDTH = 64;
    private static final int PACK_TEXTURE_HEIGHT = 32;

    private WornPackModels() {
    }

    /** Where a placed pack's model rests: its lowest point (y grows downward in model space), in pixels. */
    public static float placedBottomPixels(PackKind kind) {
        return switch (kind) {
            case SATCHEL -> 11.5F;
            case BACKPACK -> 10.5F;
            case FRAME_PACK -> 13.0F;
        };
    }

    /** The middle of a placed pack's depth (z, away from the wearer's back), in pixels, so it sits centered in its block. */
    public static float placedDepthCenterPixels(PackKind kind) {
        return switch (kind) {
            case SATCHEL -> 3.0F;
            case BACKPACK, FRAME_PACK -> 4.5F;
        };
    }

    public static ModelLayerLocation layerFor(PackKind kind) {
        return new ModelLayerLocation(ResourceIds.of(InventoryMod.MOD_ID, kind.itemPath()), LAYER_NAME);
    }

    public static LayerDefinition definitionFor(PackKind kind) {
        return switch (kind) {
            case SATCHEL -> satchel();
            case BACKPACK -> backpack();
            case FRAME_PACK -> framePack();
        };
    }

    private static LayerDefinition satchel() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("bag", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 6.5F, 2.0F, 6, 5, 2), PartPose.ZERO);
        return LayerDefinition.create(mesh, SATCHEL_TEXTURE_WIDTH, SATCHEL_TEXTURE_HEIGHT);
    }

    private static LayerDefinition backpack() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.5F, 2.0F, 8, 10, 4), PartPose.ZERO);
        root.addOrReplaceChild("pocket", CubeListBuilder.create().texOffs(0, 14).addBox(-3.0F, 5.5F, 6.0F, 6, 4, 1), PartPose.ZERO);
        return LayerDefinition.create(mesh, PACK_TEXTURE_WIDTH, PACK_TEXTURE_HEIGHT);
    }

    private static LayerDefinition framePack() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, 2.5F, 8, 12, 4), PartPose.ZERO);
        root.addOrReplaceChild("left_post", CubeListBuilder.create().texOffs(48, 0).addBox(-5.0F, -2.0F, 3.5F, 1, 15, 1), PartPose.ZERO);
        root.addOrReplaceChild("right_post", CubeListBuilder.create().texOffs(52, 0).addBox(4.0F, -2.0F, 3.5F, 1, 15, 1), PartPose.ZERO);
        root.addOrReplaceChild("top_bar", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -3.0F, 3.5F, 10, 1, 1), PartPose.ZERO);
        return LayerDefinition.create(mesh, PACK_TEXTURE_WIDTH, PACK_TEXTURE_HEIGHT);
    }
}
