package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

// Geometry Only - Is valid basin and where are its interior cells?
public class FoundryStructure {
    public static Optional<Set<BlockPos>> findBasin(Level level, BlockPos startInterior) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toCheck = new ArrayDeque<>();

        toCheck.add(startInterior);

        while (!toCheck.isEmpty()) {
            BlockPos currentPos = toCheck.poll();

            if (!visited.add(currentPos)) {
                continue;
            }

            if (!isValidInteriorContent(level, currentPos)) {
                return Optional.empty();
            }

            if (visited.size() > 4) {
                return Optional.empty();
            }

            BlockPos floorPos = currentPos.below();
            if(!isValidFoundryFloor(level.getBlockState(floorPos))) {
                return Optional.empty();
            }

            for (Direction direction : new Direction[] {
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {

                BlockPos neighborPos = currentPos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);

                if (neighborState.is(ModBlockTags.VALID_FOUNDRY_BLOCKS)) {
                    continue;
                }


                if (isValidInteriorContent(level, neighborPos)) {
                    toCheck.add(neighborPos);
                } else {
                    return Optional.empty();
                }
            }
        }
        return Optional.of(visited);
    }

    public static Optional<Set<BlockPos>> findBasinFromWall(Level level, BlockPos wallPos) {

        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                if (xOffset == 0 && zOffset == 0) {
                    continue;
                }

                BlockPos checkPos = wallPos.offset(xOffset, 0, zOffset);

                if (isValidInteriorContent(level, checkPos)) {
                    Optional<Set<BlockPos>> result = findBasin(level, checkPos);
                    if (result.isPresent()) {
                        return result;
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isValidInteriorContent(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.isAir()) {
            return true;
        }

        if (state.getFluidState().is(ModFluidTags.VALID_BASIN_FLUIDS)) {
            return true;
        }

        return FoundryMaterial.fromSolidifiedBlock(state.getBlock()).isPresent();
    }

    private static boolean isValidFoundryFloor(BlockState state) {
        return state.is(ModBlockTags.VALID_FOUNDRY_BLOCKS);
    }
}