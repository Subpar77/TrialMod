package com.subpar77.trialmod.network;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.menu.FoundryMoldMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private ModNetworking() {}

    private static void handleMoldRecipes(FoundryMoldRecipesPayload payload, IPayloadContext context) {
        TrialMod.LOGGER.info(
                "[Foundry] Received {} recipe previews for menu {} | Selected index: {}",
                payload.recipePreviews().size(), payload.containerId(), payload.selectedRecipeIndex());

        AbstractContainerMenu openMenu = context.player().containerMenu;

        if(openMenu instanceof FoundryMoldMenu moldMenu && moldMenu.containerId == payload.containerId()) {

            moldMenu.receiveRecipePreviews(payload.recipePreviews(), payload.selectedRecipeIndex());

            TrialMod.LOGGER.info("[Foundry] Applied recipe preview update to menu {}",
                    moldMenu.containerId);
        }
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(FoundryMoldRecipesPayload.TYPE, FoundryMoldRecipesPayload.STREAM_CODEC,
                ModNetworking::handleMoldRecipes);
    }
}
