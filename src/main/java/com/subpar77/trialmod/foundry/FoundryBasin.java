package com.subpar77.trialmod.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

// Operational basin inspection/manipulation
// What's in the basin right now? Can I extract it? Extraction
public class FoundryBasin {
    public static final int MB_PER_BUCKET = 1000;

    private FoundryBasin() {}

    public static Optional<BasinDetails> inspectBasin(ServerLevel level, BlockPos wallPos) {

        Optional<Set<BlockPos>> interiorResult = FoundryStructure.findBasinFromWall(level, wallPos);

        if (interiorResult.isEmpty()) {
            return Optional.empty();
        }

        Set<BlockPos> interior = interiorResult.get();
        Optional<BlockPos> basinKeyResult = findBasinKey(interior);

        if (basinKeyResult.isEmpty()) {
            return Optional.empty();
        }

        BlockPos basinKey = basinKeyResult.get();
        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);
        int capacityMb = getCapacityMb(interior);

        return Optional.of(new BasinDetails(basinKey, capacityMb, savedData.getAmountMb(basinKey),
                savedData.getMoltenAmountMb(basinKey), savedData.getTemperature(basinKey),
                Optional.ofNullable(savedData.getMaterial(basinKey))));
    }

    public static int getCapacityMb(Set<BlockPos> interior) {
        return interior.size() * MB_PER_BUCKET;
    }

    public static Optional<BlockPos> findBasinKey(Set<BlockPos> interior) {
        return interior.stream().min(Comparator.comparingInt(
                (BlockPos pos) -> pos.getX()).thenComparingInt(BlockPos::getZ).thenComparingInt(BlockPos::getY))
                .map(BlockPos::immutable);
    }
}
