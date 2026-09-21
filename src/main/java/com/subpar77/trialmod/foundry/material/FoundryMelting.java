package com.subpar77.trialmod.foundry.material;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FoundryMelting {

    private FoundryMelting() {}

    public static void  melt(ServerLevel level, Set<BlockPos> interior, float temperature) {
        List<BlockPos> candidates = new ArrayList<>();

        for(BlockPos pos : interior) {
            BlockState state = level.getBlockState(pos);
            Optional<FoundryMaterial> material = FoundryMaterial.fromSolidifiedBlock(state.getBlock());

            if(material.isEmpty()) {
                continue;
            }

            FoundryMaterial foundryMaterial = material.get();
            if(temperature >= foundryMaterial.getMeltingTemperature()) {
                candidates.add(pos);
            }
        }

        if(candidates.isEmpty()) {
            return;
        }

        BlockPos selectedPos = candidates.get(level.getRandom().nextInt(candidates.size()));
        BlockState selectedState = level.getBlockState(selectedPos);
        FoundryMaterial material = FoundryMaterial.fromSolidifiedBlock(selectedState.getBlock()).orElseThrow();

        level.setBlockAndUpdate(selectedPos, material.getMoltenFluid().defaultFluidState().createLegacyBlock());
    }
}
