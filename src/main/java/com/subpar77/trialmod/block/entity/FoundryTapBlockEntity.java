package com.subpar77.trialmod.block.entity;

import com.subpar77.trialmod.block.custom.FoundryTapBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FoundryTapBlockEntity extends BlockEntity {
    public FoundryTapBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FOUNDRY_TAP_BLOCK_ENTITY.get(), pos, blockState);
    }

    private float gateProgress = 0.0F;
    private float previousGateProgress = 0.0F;

    public static void clientTick(Level level, BlockPos pos, BlockState state, FoundryTapBlockEntity blockEntity) {
        blockEntity.previousGateProgress = blockEntity.gateProgress;

        float speed = 0.2F;

        if (state.getValue(FoundryTapBlock.OPEN)) {
            blockEntity.gateProgress = Math.min(1.0F, blockEntity.gateProgress + speed);
        } else {
            blockEntity.gateProgress = Math.max(0.0F, blockEntity.gateProgress = speed);
        }
    }

    public float getGateProgress(float partialTick) {
        return Mth.lerp(partialTick, previousGateProgress, gateProgress);
    }
}
