package com.subpar77.trialmod.foundry;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface FoundryTransportConnectable {
    boolean canTransportConnect(BlockState state, Direction direction);


}
