package com.impact.realisticvitality.client;

import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.VitalityMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Vitality's back gear as boxes on the player's back, in the body's coordinates (the torso spans x -4 to 4 and y 0 to
 * 12, its back is at z 2, and the player's right is -x): an air tank standing on the back with its valve on top, and
 * the Hydration Pack, a slim pouch whose drinking tube runs over the right shoulder and down the chest.
 */
public final class BackGearModels {
    public static final ModelLayerLocation AIR_TANK = new ModelLayerLocation(ResourceIds.of(VitalityMod.MOD_ID, "air_tank"), "main");
    public static final ModelLayerLocation HYDRATION_PACK = new ModelLayerLocation(ResourceIds.of(VitalityMod.MOD_ID, "hydration_pack"), "main");
    private static final int TEXTURE_WIDTH = 32;
    private static final int TEXTURE_HEIGHT = 16;

    private BackGearModels() {
    }

    public static LayerDefinition airTank() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("tank", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 1.0F, 2.0F, 4, 10, 4), PartPose.ZERO);
        root.addOrReplaceChild("valve", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -1.0F, 3.0F, 2, 2, 2), PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition hydrationPack() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("pouch", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 0.0F, 2.0F, 6, 8, 2), PartPose.ZERO);
        root.addOrReplaceChild("tube", CubeListBuilder.create().texOffs(16, 0).addBox(-3.5F, -1.0F, -3.0F, 1, 1, 6), PartPose.ZERO);
        root.addOrReplaceChild("mouthpiece", CubeListBuilder.create().texOffs(16, 8).addBox(-3.5F, -1.0F, -3.0F, 1, 4, 1), PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
