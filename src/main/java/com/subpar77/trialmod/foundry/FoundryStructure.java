package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.common.Mod;

import java.util.*;

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

            if (visited.size() > 4) {
                return Optional.empty();
            }

            for (Direction direction : new Direction[] {
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {

                BlockPos neighborPos = currentPos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);

                if (neighborState.is(ModBlocks.FOUNDRY_BRICK)) {
                    continue;
                }

                if (neighborState.isAir()) {
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

                if (level.getBlockState(checkPos).isAir()) {
                    Optional<Set<BlockPos>> result = findBasin(level, checkPos);
                    if (result.isPresent()) {
                        return result;
                    }
                }
            }
        }
        return Optional.empty();
    }
}