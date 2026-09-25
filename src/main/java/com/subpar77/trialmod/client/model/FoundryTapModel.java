package com.subpar77.trialmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.subpar77.trialmod.TrialMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class FoundryTapModel {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TrialMod.MODID, "foundry_tap"), "main");
    private final ModelPart base;
    private final ModelPart gate;

    public FoundryTapModel(ModelPart root) {
        this.base = root.getChild("base");
        this.gate = this.base.getChild("gate");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 41).addBox(7.0F, -8.0F, -8.0F, 1.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(34, 41).addBox(-8.0F, -8.0F, -8.0F, 1.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, 0.0F));

        PartDefinition gate = base.addOrReplaceChild("gate", CubeListBuilder.create().texOffs(0, 18).addBox(-7.0F, -9.0F, -7.0F, 14.0F, 9.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        base.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

    public void setGateProgress(float progress) {
        gate.y = -4.5F * progress;
    }
}
