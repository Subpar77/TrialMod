package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.*;

public final class FoundryBasinSpill {
    private static final int SPILL_AMOUNT_MB = FoundryBasin.MB_PER_BUCKET;

    private FoundryBasinSpill() {}

    public static boolean attemptSpill(ServerLevel level, BlockPos basinKey, Set<BlockPos> interior, FoundryBasinBreach.Result breach) {

        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);
        FoundryMaterial material = savedData.getMaterial(basinKey);

        if(material == null) {
            return false;
        }

        if(savedData.getMoltenAmountMb(basinKey) < SPILL_AMOUNT_MB) {
            return false;
        }

        Optional<BlockPos> spillPos = findSpillPosition(level, material, interior, breach);

        if(spillPos.isEmpty()) {
            return false;
        }

        BlockPos targetPos = spillPos.get();
        BlockState moltenState = material.getMoltenFluid().defaultFluidState().createLegacyBlock();

        boolean removed = savedData.tryRemoveMoltenMaterial(basinKey, SPILL_AMOUNT_MB);

        if(!removed) {
            return false;
        }

        boolean placed = level.setBlock(targetPos, moltenState, Block.UPDATE_ALL);

        if(!placed) {
            boolean restored = savedData.tryAddMoltenMaterial(basinKey, material, SPILL_AMOUNT_MB, FoundryBasin.getCapacityMb(interior));

            if(!restored) {
                TrialMod.LOGGER.error(
                        "[Foundry] Failed to restore {} mB {} to basin {} after spill placement failed.",
                        SPILL_AMOUNT_MB, material.getSerializedName(), basinKey
                );
            }

            return false;
        }

        savedData.setLastSpillPos(basinKey, targetPos);

        return true;
    }

    private static Optional<BlockPos> findSpillPosition(ServerLevel level, FoundryMaterial material,
                                                        Set<BlockPos> interior, FoundryBasinBreach.Result breach) {

        Queue<BlockPos> toCheck = new PriorityQueue<>(Comparator.comparingInt((BlockPos pos)-> pos.getY())
                .thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));
        Set<BlockPos> visited = new HashSet<>();

        toCheck.addAll(sortedPositions(breach.floorBreaches()));
        toCheck.addAll(sortedPositions(breach.wallBreaches()));

        while(!toCheck.isEmpty()) {
            BlockPos current = toCheck.poll();

            if (!visited.add(current)) {
                continue;
            }

            if (interior.contains(current)) {
                continue;
            }

            BlockState state = level.getBlockState(current);

            if (state.isAir()) {
                return Optional.of(current.immutable());
            }

            FluidState fluidState = state.getFluidState();

            if (!isMoltenMaterial(state, material)) {
                continue;
            }

            if (!fluidState.isSource()) {
                return Optional.of(current.immutable());
            }

            for (Direction direction : new Direction[]{
                    Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {

                BlockPos next = current.relative(direction);

                if (!interior.contains(next)) {
                    toCheck.add(next);
                }
            }
        }

        return Optional.empty();
    }

    private static boolean isMoltenMaterial(BlockState state, FoundryMaterial material) {

        FluidState fluidState = state.getFluidState();

        if (fluidState.isEmpty()) {
            return false;
        }

        return material.getMoltenFluid().isSame(fluidState.getType());
    }

    private static List<BlockPos> sortedPositions(Set<BlockPos> positions) {

        return positions.stream().sorted(Comparator.comparingInt((BlockPos pos) -> pos.getY())
                .thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ)).toList();
    }
}
