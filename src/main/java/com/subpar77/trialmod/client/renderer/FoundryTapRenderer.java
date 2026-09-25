package com.subpar77.trialmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.custom.FoundryTapBlock;
import com.subpar77.trialmod.block.entity.FoundryTapBlockEntity;
import com.subpar77.trialmod.client.model.FoundryTapModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class FoundryTapRenderer implements BlockEntityRenderer<FoundryTapBlockEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TrialMod.MODID, "textures/block/foundry_tap.png");

    private final FoundryTapModel model;

    public FoundryTapRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new FoundryTapModel(context.bakeLayer(FoundryTapModel.LAYER_LOCATION));
    }


    @Override
    public void render(FoundryTapBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        poseStack.translate(0.5D, 1.50, 0.5D);
        Direction facing = blockEntity.getBlockState().getValue(FoundryTapBlock.FACING);

        float rotation = switch (facing) {
            case NORTH -> 0.0F;
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };

        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        poseStack.scale(1.0F, -1.0F, -1.0F);

        float gateProgress = blockEntity.getGateProgress(partialTick);
        model.setGateProgress(gateProgress);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();

    }
}
