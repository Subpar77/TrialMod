package com.subpar77.trialmod.foundry;


import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;

import java.util.Optional;

// Snapshot of the current basin state.
public record BasinDetails(
        BlockPos basinKey,
        int capacityMb,
        int amountMb,
        int moltenAmountMb,
        float temperature,
        Optional<FoundryMaterial> material
) {
    public int availableCapacityMb() {
        return Math.max(0, capacityMb - amountMb);
    }

    public int solidAmountMb() {
        return Math.max(0, amountMb - moltenAmountMb);
    }

    public boolean isEmpty() {
        return amountMb <= 0 || material.isEmpty();
    }
}
