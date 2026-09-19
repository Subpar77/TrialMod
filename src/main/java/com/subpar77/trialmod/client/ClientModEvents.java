package com.subpar77.trialmod.client;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.entity.ModBlockEntities;
import com.subpar77.trialmod.client.model.FoundryTapModel;
import com.subpar77.trialmod.client.renderer.FoundryTapRenderer;
import com.subpar77.trialmod.fluid.ModFluids;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TrialMod.MODID, value = Dist.CLIENT)

public class ClientModEvents {

    private static final ResourceLocation MOLTEN_COPPER_STILL =
            ResourceLocation.fromNamespaceAndPath(TrialMod.MODID, "block/molten_copper_still");

    private static final ResourceLocation MOLTEN_COPPER_FLOWING =
            ResourceLocation.fromNamespaceAndPath(TrialMod.MODID, "block/molten_copper_flow");

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FoundryTapModel.LAYER_LOCATION, FoundryTapModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_TAP_BLOCK_ENTITY.get(), FoundryTapRenderer::new);
    }

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {

            @Override
            public ResourceLocation getStillTexture() {
                return MOLTEN_COPPER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return MOLTEN_COPPER_FLOWING;
            }
        },
                ModFluids.MOLTEN_COPPER_TYPE.get());
    }

}
