package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.block.custom.FoundryMoltenDisplayBlock;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public final class FoundryBasinVisuals {

    private FoundryBasinVisuals() {}

    public static void update(
            ServerLevel level,
            BlockPos basinKey,
            Set<BlockPos> interior) {

        FoundryBasinSavedData savedData =
                FoundryBasinSavedData.get(level);

        FoundryMaterial material =
                savedData.getMaterial(basinKey);

        int moltenAmountMb =
                savedData.getMoltenAmountMb(basinKey);

        if (material == null || moltenAmountMb <= 0) {
            clear(level, interior);
            return;
        }

        int capacityMb =
                FoundryBasin.getCapacityMb(interior);

        float fillRatio =
                (float) moltenAmountMb / capacityMb;

        int visualLevel =
                Math.max(
                        1,
                        Math.min(
                                16,
                                (int) Math.ceil(fillRatio * 16.0F)
                        )
                );

        BlockState displayState =
                material.getMoltenDisplayBlock()
                        .defaultBlockState()
                        .setValue(
                                FoundryMoltenDisplayBlock.LEVEL,
                                visualLevel
                        );

        for (BlockPos pos : interior) {

            BlockState currentState =
                    level.getBlockState(pos);

            if (!currentState.isAir()
                    && !(currentState.getBlock() instanceof FoundryMoltenDisplayBlock)) {

                continue;
            }

            if (!currentState.equals(displayState)) {
                level.setBlock(
                        pos,
                        displayState,
                        Block.UPDATE_ALL
                );
            }
        }
    }

    private static void clear(
            ServerLevel level,
            Set<BlockPos> interior) {

        for (BlockPos pos : interior) {

            BlockState currentState =
                    level.getBlockState(pos);

            if (currentState.getBlock() instanceof FoundryMoltenDisplayBlock) {

                level.setBlock(
                        pos,
                        net.minecraft.world.level.block.Blocks.AIR
                                .defaultBlockState(),
                        Block.UPDATE_ALL
                );
            }
        }
    }
}