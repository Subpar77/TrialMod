package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.block.custom.FoundryChannelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class FoundryChannelNetwork {

    public static Set<BlockPos> findConnectedChannels(ServerLevel level, BlockPos startPos) {

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toCheck = new ArrayDeque<>();

        toCheck.add(startPos);

        while(!toCheck.isEmpty()) {
            BlockPos currentPos = toCheck.poll();

            if(!visited.add(currentPos)) {
                continue;
            }

            BlockState currentState = level.getBlockState(currentPos);
            for(Direction direction : new Direction[] {
                    Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {


                boolean open = FoundryChannelBlock.isTransportOpen(currentState, direction);

                if(!open) {
                    continue;
                }

                BlockPos neighborPos = currentPos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);

                if(neighborState.getBlock() instanceof FoundryChannelBlock && !visited.contains(neighborPos)) {

                    toCheck.add(neighborPos);

                }

            }
        }

        return visited;
    }
}
