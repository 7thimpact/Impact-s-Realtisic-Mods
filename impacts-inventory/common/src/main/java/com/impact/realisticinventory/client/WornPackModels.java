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
 * back is at z 2). A satchel is a small bag low on the back, a backpack a box with a front pocket, a frame pack a tall
 * box held by a wooden frame, an explorer's pack the same on light posts under a bedroll, an expedition (or netherite)
 * pack a deep pack with three pockets and a bedroll, and an ender pack a backpack under a shulker-shell lid.
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
            case BACKPACK, ENDER_PACK -> 10.5F;
            case FRAME_PACK -> 13.0F;
            case EXPLORERS_PACK -> 12.0F;
            case EXPEDITION_PACK, NETHERITE_PACK -> 12.5F;
        };
    }

    /** The middle of a placed pack's depth (z, away from the wearer's back), in pixels, so it sits centered in its block. */
    public static float placedDepthCenterPixels(PackKind kind) {
        return switch (kind) {
            case SATCHEL -> 3.0F;
            case BACKPACK, FRAME_PACK, EXPLORERS_PACK, ENDER_PACK -> 4.5F;
            case EXPEDITION_PACK, NETHERITE_PACK -> 5.0F;
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
            case EXPLORERS_PACK -> explorersPack();
            case EXPEDITION_PACK, NETHERITE_PACK -> expeditionPack();
            case ENDER_PACK -> enderPack();
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

    // The frame pack's shape on light posts, with a bedroll strapped across the top instead of the top bar.
    private static LayerDefinition explorersPack() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, 2.5F, 8, 12, 4), PartPose.ZERO);
        root.addOrReplaceChild("left_post", CubeListBuilder.create().texOffs(48, 0).addBox(-5.0F, -1.0F, 3.5F, 1, 13, 1), PartPose.ZERO);
        root.addOrReplaceChild("right_post", CubeListBuilder.create().texOffs(52, 0).addBox(4.0F, -1.0F, 3.5F, 1, 13, 1), PartPose.ZERO);
        root.addOrReplaceChild("bedroll", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -3.0F, 2.5F, 10, 3, 3), PartPose.ZERO);
        return LayerDefinition.create(mesh, PACK_TEXTURE_WIDTH, PACK_TEXTURE_HEIGHT);
    }

    // A deep pack with a pocket on each side and on the front, and a bedroll on top. The Netherite Pack shares it.
    private static LayerDefinition expeditionPack() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -0.5F, 2.0F, 8, 13, 5), PartPose.ZERO);
        root.addOrReplaceChild("left_pocket", CubeListBuilder.create().texOffs(28, 0).addBox(-6.0F, 5.0F, 2.5F, 2, 6, 3), PartPose.ZERO);
        root.addOrReplaceChild("right_pocket", CubeListBuilder.create().texOffs(40, 0).addBox(4.0F, 5.0F, 2.5F, 2, 6, 3), PartPose.ZERO);
        root.addOrReplaceChild("front_pocket", CubeListBuilder.create().texOffs(28, 12).addBox(-3.0F, 6.0F, 7.0F, 6, 4, 1), PartPose.ZERO);
        root.addOrReplaceChild("bedroll", CubeListBuilder.create().texOffs(0, 20).addBox(-4.5F, -3.5F, 3.0F, 9, 3, 3), PartPose.ZERO);
        return LayerDefinition.create(mesh, PACK_TEXTURE_WIDTH, PACK_TEXTURE_HEIGHT);
    }

    // The backpack's shape under a shulker-shell lid.
    private static LayerDefinition enderPack() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.5F, 2.0F, 8, 10, 4), PartPose.ZERO);
        root.addOrReplaceChild("pocket", CubeListBuilder.create().texOffs(0, 14).addBox(-3.0F, 5.5F, 6.0F, 6, 4, 1), PartPose.ZERO);
        root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(28, 0).addBox(-4.5F, -1.0F, 1.5F, 9, 2, 5), PartPose.ZERO);
        return LayerDefinition.create(mesh, PACK_TEXTURE_WIDTH, PACK_TEXTURE_HEIGHT);
    }
}
