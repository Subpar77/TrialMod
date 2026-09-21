package com.subpar77.trialmod.foundry.material;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FoundrySolidification {

    private FoundrySolidification() {}

    public static void  solidify(ServerLevel level, Set<BlockPos> interior, float temperature) {
        List<BlockPos> candidates = new ArrayList<>();

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
                candidates.add(pos);
            }
        }

        if(candidates.isEmpty()) {
            return;
        }

        BlockPos selectedPos = candidates.get(level.getRandom().nextInt(candidates.size()));
        FluidState selectedFluid = level.getFluidState(selectedPos);
        FoundryMaterial material = FoundryMaterial.fromFluid(selectedFluid.getType()).orElseThrow();

        level.setBlockAndUpdate(selectedPos, material.getSolidifiedBlock().defaultBlockState());
    }
}
