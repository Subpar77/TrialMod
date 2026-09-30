package com.subpar77.trialmod;

import com.subpar77.trialmod.block.entity.ModBlockEntities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilities {

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.TEST_TANK_ENTITY.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler());
    }
}
