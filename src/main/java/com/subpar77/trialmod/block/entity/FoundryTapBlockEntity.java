package com.subpar77.trialmod.block.entity;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.custom.FoundryChannelBlock;
import com.subpar77.trialmod.block.custom.FoundryTapBlock;
import com.subpar77.trialmod.foundry.*;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

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
        BlockState firstChannelState = level.getBlockState(outputPos);

        if(!(firstChannelState.getBlock() instanceof FoundryChannelBlock)) {
            return false;
        }

        FoundryChannelNetworkResult results = FoundryChannelNetwork.findConnectedChannels(level, outputPos);

        if(results.destinations().isEmpty()) {
            return false;
        }

        TrialMod.LOGGER.info("[Foundry] Tap found {} fluid destinations.", results.destinations().size());


        Direction basinDirection = outputDirection.getOpposite();
        BlockPos wallPos = pos.relative(basinDirection);
        Optional<BasinDetails> result = FoundryBasin.inspectBasin(level, wallPos);

        if(result.isEmpty()) {
            return false;
        }

        BasinDetails details = result.get();

        if(details.material().isEmpty() || details.moltenAmountMb() < FoundryBasin.MB_PER_BUCKET) {
            return false;
        }

        FoundryMaterial material = details.material().get();
        FluidStack transferStack = new FluidStack(material.getMoltenFluid(),FoundryBasin.MB_PER_BUCKET);
        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);
        FoundryFluidDestination selectedDestination = null;
        IFluidHandler selectedHandler = null;

        for(FoundryFluidDestination destination : results.destinations()) {
            IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, destination.receiverPos(),
                    destination.receiverSide());

            if (handler == null) {
                continue;
            }

            int accepted = handler.fill(transferStack, IFluidHandler.FluidAction.SIMULATE);

            if (accepted >= FoundryBasin.MB_PER_BUCKET) {
                selectedDestination = destination;
                selectedHandler = handler;
                break;
            }
        }

            if(selectedDestination == null || selectedHandler == null) {
                return false;
            }

            TrialMod.LOGGER.info(
                    "[Foundry] Tap selected receiver {} via route {}.",
                    selectedDestination.receiverPos(), selectedDestination.route()
            );

        boolean removed = savedData.tryRemoveMoltenMaterial(details.basinKey(), FoundryBasin.MB_PER_BUCKET);

        if(!removed) {
            return false;
        }

        int actuallyAccepted = selectedHandler.fill(transferStack, IFluidHandler.FluidAction.EXECUTE);
        int amountToRestore = FoundryBasin.MB_PER_BUCKET - actuallyAccepted;

        if(amountToRestore > 0) {
            boolean restored = savedData.tryAddMoltenMaterial(details.basinKey(), material, amountToRestore,
                    details.capacityMb());

            if(!restored) {
                TrialMod.LOGGER.warn(
                "[Foundry] Failed to restore {} mB {} to basin {} after partial transfer.",
                        amountToRestore, material.getSerializedName(), details.basinKey());
            }
        }

        if(actuallyAccepted <= 0) {
            return false;
        }

        TrialMod.LOGGER.info(
                "[Foundry] Tap {} transferred {} mB {} to receiver {} via route {}. Basin remaining={} mB.",
                pos, actuallyAccepted, material.getSerializedName(), selectedDestination.receiverPos(),
                selectedDestination.route(), savedData.getMoltenAmountMb(details.basinKey())
        );

        return true;

    }

    public float getGateProgress(float partialTick) {
        return Mth.lerp(partialTick, previousGateProgress, gateProgress);
    }
}
