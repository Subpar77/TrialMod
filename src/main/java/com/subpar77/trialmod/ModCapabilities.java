package com.subpar77.trialmod;

import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.block.entity.ModBlockEntities;
import com.subpar77.trialmod.foundry.FoundryBasinItemHandler;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilities {

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.TEST_TANK_ENTITY.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.FOUNDRY_MOLD_ENTITY.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler());

        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state,
                                                             blockEntity, side) -> {
            if (!(level instanceof ServerLevel serverLevel)) {
                return null;
            }
            return new FoundryBasinItemHandler(serverLevel, pos);
        }, ModBlocks.STONE_BASIN_WALL.get());
    }
}
