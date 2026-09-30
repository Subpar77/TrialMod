package com.subpar77.trialmod.foundry;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Set;

public record FoundryChannelNetworkResult(Set<BlockPos> channels, List<FoundryFluidDestination> destinations) {
}
