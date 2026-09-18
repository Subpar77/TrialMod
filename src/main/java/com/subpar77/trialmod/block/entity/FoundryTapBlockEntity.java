package com.subpar77.trialmod.block.entity;

import com.subpar77.trialmod.block.custom.FoundryTapBlock;
import com.subpar77.trialmod.foundry.BasinDetails;
import com.subpar77.trialmod.foundry.FoundryBasin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class FoundryTapBlockEntity extends BlockEntity {
    public FoundryTapBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FOUNDRY_TAP_BLOCK_ENTITY.get(), pos, blockState);
    }

    private float gateProgress = 0.0F;
    private float previousGateProgress = 0.0F;
    private static final int TRANSFER_INTERVAL_TICKS = 20;
    private int transferCooldown =0;

    public static void clientTick(Level level, BlockPos pos, BlockState state, FoundryTapBlockEntity blockEntity) {
        blockEntity.previousGateProgress = blockEntity.gateProgress;

        float speed = 0.2F;

        if (state.getValue(FoundryTapBlock.OPEN)) {
            blockEntity.gateProgress = Math.min(1.0F, blockEntity.gateProgress + speed);
        } else {
            blockEntity.gateProgress = Math.max(0.0F, blockEntity.gateProgress = speed);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryTapBlockEntity blockEntity) {
        if (!state.getValue(FoundryTapBlock.OPEN)) {
            blockEntity.transferCooldown = 0;
            return;
        }

        blockEntity.transferCooldown++;

        if (blockEntity.transferCooldown < TRANSFER_INTERVAL_TICKS) {
            return;
        }

        blockEntity.transferCooldown = 0;
        Direction outputDirection = state.getValue(FoundryTapBlock.FACING);
        Direction basinDirection = outputDirection.getOpposite();
        BlockPos wallPos = pos.relative(basinDirection);
        Optional<BasinDetails> basinDetails = FoundryBasin.inspectBasin(level, wallPos);

        if (basinDetails.isEmpty()) {
            return;
        }

        BasinDetails details = basinDetails.get();

        if (details.storedBuckets() <= 0) {
            return;
        }

        FoundryBasin.extractOneSource(level, details);
    }

    public float getGateProgress(float partialTick) {
        return Mth.lerp(partialTick, previousGateProgress, gateProgress);
    }
}
