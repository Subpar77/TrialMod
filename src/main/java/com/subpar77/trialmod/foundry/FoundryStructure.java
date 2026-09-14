package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.common.Mod;

import java.util.Optional;

public class FoundryStructure {
    public static boolean isValidFoundry(Level level, BlockPos center) {

        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                if (xOffset == 0 && zOffset ==0) {
                    continue;
                }

                BlockPos checkPos = center.offset(xOffset, 0, zOffset);

                if (!level.getBlockState(checkPos).is(ModBlocks.FOUNDRY_BRICK)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static Optional<BlockPos> findFoundryCenter(Level level, BlockPos brickPos) {
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                if (xOffset == 0 && zOffset == 0) {
                    continue;
                }

                BlockPos candidateCenter = brickPos.offset(xOffset, 0, zOffset);

                if (isValidFoundry(level, candidateCenter)) {
                    return Optional.of(candidateCenter);
                }
            }

        }
        return Optional.empty();
    }
}
