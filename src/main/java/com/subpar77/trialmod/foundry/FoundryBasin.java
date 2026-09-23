package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import com.subpar77.trialmod.foundry.material.FoundrySolidForm;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

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

        if(!savedData.isRegistered(basinKey)) {
            return Optional.empty();
        }

        int capacityMb = getCapacityMb(interior);

        return Optional.of(new BasinDetails(basinKey, capacityMb, savedData.getAmountMb(basinKey),
                savedData.getMoltenAmountMb(basinKey), savedData.getTemperature(basinKey),
                Optional.ofNullable(savedData.getMaterial(basinKey))));
    }

    public static Optional<BlockPos> registerBasinFromWall (ServerLevel level, BlockPos wallPos) {
        Optional<Set<BlockPos>> interiorResult = FoundryStructure.findBasinFromWall(level, wallPos);

        if(interiorResult.isEmpty()) {
            return Optional.empty();
        }

        Optional<BlockPos> basinKeyResult = findBasinKey(interiorResult.get());

        if(basinKeyResult.isEmpty()) {
            return Optional.empty();
        }

        BlockPos basinKey = basinKeyResult.get();
        FoundryBasinSavedData.get(level).registerBasin(basinKey);

        return Optional.of(basinKey);
    }


    public static int getPhysicalSolidAmountMb(ServerLevel level, Set<BlockPos> interior) {
        int totalMb = 0;

        for(BlockPos pos : interior){
            BlockState state = level.getBlockState(pos);
            Optional<FoundrySolidForm> solidForm = FoundryMaterial.fromSolidifiedState(state);

            if(solidForm.isPresent()) {
                totalMb += solidForm.get().amountMb();
            }
        }

        return totalMb;
    }

    public static int getStateCapacityMb(ServerLevel level, Set<BlockPos> interior) {
        int totalCapacityMb = getCapacityMb(interior);
        int physicalSolidMb = getPhysicalSolidAmountMb(level, interior);

        return Math.max(0, totalCapacityMb - physicalSolidMb);
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
