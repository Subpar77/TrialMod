package com.subpar77.trialmod.block.entity;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.custom.FoundryTapBlock;
import com.subpar77.trialmod.foundry.BasinDetails;
import com.subpar77.trialmod.foundry.FoundryBasin;
import com.subpar77.trialmod.foundry.FoundryBasinSavedData;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!state.getValue(FoundryTapBlock.OPEN)) {
            blockEntity.transferCooldown = 0;
            return;
        }

        blockEntity.transferCooldown++;

        if (blockEntity.transferCooldown < TRANSFER_INTERVAL_TICKS) {
            return;
        }

        blockEntity.transferCooldown = 0;
        attemptTransfer(serverLevel, pos, state);

    }

    private static boolean attemptTransfer(ServerLevel level, BlockPos pos, BlockState state) {
        Direction outputDirection = state.getValue(FoundryTapBlock.FACING);
        BlockPos outputPos = pos.relative(outputDirection);

        if(!level.getBlockState(outputPos).isAir()) {
            return false;
        }

        Direction basinDirection = outputDirection.getOpposite();
        BlockPos wallPos = pos.relative(basinDirection);
        Optional<BasinDetails> result = FoundryBasin.inspectBasin(level, wallPos);

        if(result.isEmpty()) {
            return false;
        }

        BasinDetails details = result.get();

        if(details.moltenAmountMb() < FoundryBasin.MB_PER_BUCKET) {
            return false;
        }

        FoundryMaterial material = details.material().get();
        BlockState fluidBlock = material.getMoltenFluid().defaultFluidState().createLegacyBlock();
        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);

        boolean removed = savedData.tryRemoveMoltenMaterial(details.basinKey(), FoundryBasin.MB_PER_BUCKET);

        if(!removed) {
            return false;
        }

        boolean placed = level.setBlock(outputPos, fluidBlock, Block.UPDATE_ALL);

        if(!placed) {
            boolean restored = savedData.tryAddMoltenMaterial(details.basinKey(), material, FoundryBasin.MB_PER_BUCKET,
                    details.capacityMb());

            if(!restored) {
                TrialMod.LOGGER.warn(
                        "[Foundry] Failed to restore {} mB {} to basin {} after Tap output failed.",
                        FoundryBasin.MB_PER_BUCKET, material.getSerializedName(), details.basinKey()
                );
            }
            return false;
        }

        TrialMod.LOGGER.info(
                "[Foundry] Tap {} transferred {} mB {} from basin {}. Remaining molten={} mB.",
                pos, FoundryBasin.MB_PER_BUCKET, material.getSerializedName(), details.basinKey(), savedData.getMoltenAmountMb(
                        details.basinKey())
                );
        return true;

    }

    public float getGateProgress(float partialTick) {
        return Mth.lerp(partialTick, previousGateProgress, gateProgress);
    }
}
