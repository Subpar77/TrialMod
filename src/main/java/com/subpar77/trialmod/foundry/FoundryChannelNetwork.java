package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.custom.FoundryChannelBlock;
import com.subpar77.trialmod.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.*;

public class FoundryChannelNetwork {

    public static FoundryChannelNetworkResult findConnectedChannels(ServerLevel level, BlockPos startPos) {

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toCheck = new ArrayDeque<>();
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        List<FoundryFluidDestination> destinations = new ArrayList<>();

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

                if (neighborState.getBlock() instanceof FoundryChannelBlock) {

                    boolean neighborOpen = FoundryChannelBlock.isTransportOpen(neighborState, direction.getOpposite());

                    if(!neighborOpen) {
                        continue;
                    }

                    if(!visited.contains(neighborPos) && !cameFrom.containsKey(neighborPos)) {
                        cameFrom.put(neighborPos, currentPos);
                        toCheck.add(neighborPos);
                    }
                        continue;
                    }

                    IFluidHandler fluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos,
                            direction.getOpposite());

                    if(fluidHandler != null) {
//                        TrialMod.LOGGER.info(
//                                "[Foundry] Found fluid receiver at {} on side {}",
//                                neighborPos, direction.getOpposite()
//                        );

                        List<BlockPos> route = reconstructPath(cameFrom, startPos, currentPos);

                        FoundryFluidDestination destination = new FoundryFluidDestination(neighborPos,
                                direction.getOpposite(), currentPos, route);

                        destinations.add(destination);

//                        TrialMod.LOGGER.info("[Foundry] Destinations: {}.", destinations);

                        FluidStack testFluid = new FluidStack(ModFluids.MOLTEN_COPPER_SOURCE.get(), 1000);
                        int accepted = fluidHandler.fill(testFluid, IFluidHandler.FluidAction.SIMULATE);

//                        TrialMod.LOGGER.info(
//                                "[Foundry] Receiver at {} would accept {} mB.", neighborPos, accepted
//                        );
                    }
                }
            }

        for(BlockPos endPos : visited) {
            List<BlockPos> path = reconstructPath(cameFrom, startPos, endPos);

            TrialMod.LOGGER.info("[Foundry] Path: {}.", path);
        }

//        TrialMod.LOGGER.info(
//                "[Foundry] cameFrom: {}.", cameFrom);

        return new FoundryChannelNetworkResult(visited, destinations);
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
