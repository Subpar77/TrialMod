package com.subpar77.trialmod.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.Set;

public class FoundryHeat {
    private FoundryHeat() {}

    public static Optional<HeatSourceData> getHeatSource(Level level, BlockPos interiorPos) {
        BlockPos heatPos = interiorPos.below().below();
        BlockState state = level.getBlockState(heatPos);

        if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
            return Optional.of(new HeatSourceData(1200.0F, 5.0F));
            }

        return Optional.empty();
    }

    public static Optional<HeatSourceData> inspect(Level level, Set<BlockPos> interior) {
        float maxTemperature = 0.0F;
        float totalHeatingRate = 0.0F;
        boolean foundHeatSource = false;

        for (BlockPos interiorPos : interior) {
            Optional<HeatSourceData> heatSource = getHeatSource(level, interiorPos);

            if (heatSource.isPresent()) {
                HeatSourceData data = heatSource.get();

                maxTemperature = Math.max(maxTemperature, data.maxTemperature());

                totalHeatingRate += data.heatingRate();
                foundHeatSource = true;
            }
        }

        if (!foundHeatSource) {
            return Optional.empty();
        }

        return Optional.of(new HeatSourceData(maxTemperature, totalHeatingRate));
    }
}
