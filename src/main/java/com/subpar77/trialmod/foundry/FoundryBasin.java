package com.subpar77.trialmod.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Optional;
import java.util.Set;

// Operational basin inspection/manipulation
// What's in the basin right now? Can I extract it? Extraction
public class FoundryBasin {

    public static Optional<BasinDetails> inspectBasin(Level level, BlockPos wallPos) {

        Optional<Set<BlockPos>> result = FoundryStructure.findBasinFromWall(level, wallPos);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Set<BlockPos> interiorPositions = result.get();
        int storedBuckets = 0;
        Optional<BlockPos> sourcePos = Optional.empty();

        FluidType detectedFluidType = null;

        for (BlockPos interiorPos : interiorPositions) {
            FluidState fluidState = level.getFluidState(interiorPos);

            if (fluidState.is(Tags.Fluids.LAVA)) {
                detectedFluidType = fluidState.getFluidType();

                if (fluidState.isSource()) {
                    storedBuckets++;
                    sourcePos = Optional.of(interiorPos);
                }
            }
        }

        int capacity = interiorPositions.size();
        int availableCapacity = capacity - storedBuckets;

        return Optional.of(new BasinDetails(capacity, storedBuckets, availableCapacity, Optional.ofNullable(detectedFluidType), sourcePos));

    }

    public static boolean extractOneSource(Level level, BasinDetails details) {
        if (details.storedBuckets() <= 0) {
            return false;
        }

        BlockPos sourcePos = details.sourcePos().orElseThrow();
        FluidState fluidState = level.getFluidState(sourcePos);

        if (!fluidState.is(Tags.Fluids.LAVA) || !fluidState.isSource()) {
            return false;
        }

        level.setBlock(sourcePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        return true;
    }

}
