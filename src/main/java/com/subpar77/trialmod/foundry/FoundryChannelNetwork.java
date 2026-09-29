package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.custom.FoundryChannelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class FoundryChannelNetwork {

    public static Set<BlockPos> findConnectedChannels(ServerLevel level, BlockPos startPos) {

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toCheck = new ArrayDeque<>();
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();

        toCheck.add(startPos);

        while (!toCheck.isEmpty()) {
            BlockPos currentPos = toCheck.poll();

            if (!visited.add(currentPos)) {
                continue;
            }

            BlockState currentState = level.getBlockState(currentPos);
            for (Direction direction : new Direction[]{
                    Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {


                boolean open = FoundryChannelBlock.isTransportOpen(currentState, direction);

                if (!open) {
                    continue;
                }

                BlockPos neighborPos = currentPos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);

                if (neighborState.getBlock() instanceof FoundryChannelBlock && !visited.contains(neighborPos)) {

                    if (!cameFrom.containsKey(neighborPos)) {
                        cameFrom.put(neighborPos, currentPos);
                        toCheck.add(neighborPos);
                    }

                }
            }

        }

        for(BlockPos endPos : visited) {
            List<BlockPos> path = reconstructPath(cameFrom, startPos, endPos);

            TrialMod.LOGGER.info("[Foundry] Path: {}.", path);
        }

//        TrialMod.LOGGER.info(
//                "[Foundry] cameFrom: {}.", cameFrom);

        return visited;
    }

    private static List<BlockPos> reconstructPath(Map<BlockPos, BlockPos> cameFrom, BlockPos startPos, BlockPos endPos) {
        List<BlockPos> path = new ArrayList<>();

        BlockPos currentPos = endPos;
        path.add(currentPos);

        while(!currentPos.equals(startPos)) {
            currentPos = cameFrom.get(currentPos);
            path.add(currentPos);
        }

        Collections.reverse(path);

        return path;
    }
}
