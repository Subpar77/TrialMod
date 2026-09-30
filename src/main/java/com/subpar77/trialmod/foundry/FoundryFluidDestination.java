package com.subpar77.trialmod.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public record FoundryFluidDestination(BlockPos receiverPos, Direction receiverSide, BlockPos channelPos,
                                      List<BlockPos> route) {

}
