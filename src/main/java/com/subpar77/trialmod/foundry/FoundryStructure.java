package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.block.custom.FoundryMoltenDisplayBlock;
import com.subpar77.trialmod.foundry.material.FoundryMaterialForms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

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
            if (!isValidBasinFloor(level.getBlockState(floorPos))) {
                return Optional.empty();
            }

            for (Direction direction : new Direction[]{
                    Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {

                BlockPos neighborPos = currentPos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);

                if (neighborState.is(ModBlockTags.VALID_BASIN_WALLS)) {
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

        if (state.getBlock() instanceof FoundryMoltenDisplayBlock) {
            return true;
        }

        if (state.getFluidState().is(ModFluidTags.VALID_BASIN_FLUIDS)) {
            return true;
        }

        return FoundryMaterialForms.fromSolidifiedState(state).isPresent();
    }

    static boolean isValidBasinFloor(BlockState state) {

        if (!state.is(ModBlockTags.VALID_BASIN_FLOORS)) {
            return false;
        }

        if (state.getBlock() instanceof SlabBlock) {SlabType slabType = state.getValue(SlabBlock.TYPE);

            return slabType == SlabType.TOP
                    || slabType == SlabType.DOUBLE;
        }

        return true;
    }

    static boolean isValidBasinWall(BlockState state) {
        return state.is(ModBlockTags.VALID_BASIN_WALLS);
    }
}