package com.subpar77.trialmod.block.entity;

import com.subpar77.trialmod.block.custom.FoundryTapBlock;
import com.subpar77.trialmod.foundry.BasinDetails;
import com.subpar77.trialmod.foundry.FoundryBasin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

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
            blockEntity.gateProgress = Math.max(0.0F, blockEntity.gateProgress - speed);
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
        attemptTransfer(level, pos, state);

    }

    public static  boolean attemptTransfer (Level level, BlockPos pos, BlockState state) {
        Direction outputDirection = state.getValue(FoundryTapBlock.FACING);
        BlockPos outputPos = pos.relative(outputDirection);

        if (!level.getBlockState(outputPos).isAir()) {
            return false;
        }

        Direction basinDirection = outputDirection.getOpposite();
        BlockPos wallPos = pos.relative(basinDirection);

        Optional<BasinDetails> result = FoundryBasin.inspectBasin(level, wallPos);

        if(result.isEmpty()) {
            return false;
        }

        BasinDetails details = result.get();

        if (details.storedBuckets() <= 0) {
            return  false;
        }

        BlockPos sourcePos = details.sourcePos().orElseThrow();

        FluidState sourceFluid = level.getFluidState(sourcePos);
        BlockState fluidBlock = sourceFluid.createLegacyBlock();

        if(!FoundryBasin.extractOneSource(level, details)) {
            return false;
        }

        level.setBlock(outputPos, fluidBlock, Block.UPDATE_ALL);

        return true;
    }

    public float getGateProgress(float partialTick) {
        return Mth.lerp(partialTick, previousGateProgress, gateProgress);
    }
}
