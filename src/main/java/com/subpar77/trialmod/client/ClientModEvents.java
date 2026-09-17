package com.subpar77.trialmod.client;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.entity.ModBlockEntities;
import com.subpar77.trialmod.client.model.FoundryTapModel;
import com.subpar77.trialmod.client.renderer.FoundryTapRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TrialMod.MODID, value = Dist.CLIENT)

public class ClientModEvents {

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FoundryTapModel.LAYER_LOCATION, FoundryTapModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_TAP_BLOCK_ENTITY.get(), FoundryTapRenderer::new);
    }

}
