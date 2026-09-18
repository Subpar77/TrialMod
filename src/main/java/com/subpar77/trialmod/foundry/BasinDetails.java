package com.subpar77.trialmod.foundry;


import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Optional;

// Snapshot of the current basin state.
public record BasinDetails(
        int capacity,
        int storedBuckets,
        int availableCapacity,
        Optional<FluidType> fluidType,
        Optional<BlockPos> sourcePos
) {
}
