package com.subpar77.trialmod.foundry.material;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;
import java.util.Set;

public class FoundrySolidification {

    private FoundrySolidification() {}

    public static void  solidify(ServerLevel level, Set<BlockPos> interior, float temperature) {

        for(BlockPos pos : interior) {
            FluidState fluidState = level.getFluidState(pos);

            if(!fluidState.isSource()) {
                continue;
            }

            Optional<FoundryMaterial> material = FoundryMaterial.fromFluid(fluidState.getType());
            if(material.isEmpty()) {
                continue;
            }

            FoundryMaterial foundryMaterial = material.get();
            if(temperature <= foundryMaterial.getSolidificationTemperature()) {
                level.setBlockAndUpdate(pos, foundryMaterial.getSoldifiedBlock().defaultBlockState());
            }
        }
    }
}
