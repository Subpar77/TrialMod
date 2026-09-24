package com.subpar77.trialmod.foundry.material;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.custom.FoundryMoltenDisplayBlock;
import com.subpar77.trialmod.foundry.FoundryBasinSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class FoundrySolidification {

    private FoundrySolidification() {}

    private static final int BLOCK_MB = FoundryMaterialForms.BLOCK_MB;
    private static final int SLAB_MB = FoundryMaterialForms.SLAB_MB;
    private static final int CLUMP_MB = FoundryMaterialForms.CLUMP_MB;
    private static final int NUGGET_MB = FoundryMaterialForms.NUGGET_MB;


    public static void process(ServerLevel level, BlockPos basinKey, Set<BlockPos> interior) {

        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);
        FoundryMaterial material = savedData.getMaterial(basinKey);

        if (material == null) {
            return;
        }

        if (savedData.getMoltenAmountMb(basinKey) > 0) {
            return;
        }

        int solidAmountMb = savedData.getSolidAmountMb(basinKey);

        if (solidAmountMb < SLAB_MB) {
            return;
        }

        List<BlockPos> positions = new ArrayList<>(interior);

        positions.sort(
                Comparator.comparingInt((BlockPos pos) -> pos.getX())
                        .thenComparingInt(BlockPos::getZ)
                        .thenComparingInt(BlockPos::getY)
        );

        for (BlockPos pos : positions) {

            if (solidAmountMb < SLAB_MB) {
                break;
            }

            BlockState state = level.getBlockState(pos);

            Optional<FoundrySolidForm> formResult = FoundryMaterialForms.getSolidForm(material, state);

            if (formResult.isEmpty()) {
                continue;
            }

            FoundrySolidForm form = formResult.get();

            if (form.amountMb() != SLAB_MB) {
                continue;
            }

            BlockState replacement = material.getSolidifiedBlock().defaultBlockState();

            if (transferSolidToWorld(
                    level,
                    basinKey,
                    pos,
                    state,
                    replacement,
                    SLAB_MB)) {

                solidAmountMb -= SLAB_MB;
            }
        }

        for (BlockPos pos : positions) {

            if (solidAmountMb < BLOCK_MB) {
                break;
            }

            BlockState currentState = level.getBlockState(pos);

            if (!canPlaceSolidHere(currentState)) {
                continue;
            }

            BlockState slagBlock = material.getSolidifiedBlock().defaultBlockState();

            if (transferSolidToWorld(level, basinKey, pos, currentState, slagBlock, BLOCK_MB)) {
                solidAmountMb -= BLOCK_MB;
            }
        }

        if (solidAmountMb >= SLAB_MB) {

            for (BlockPos pos : positions) {

                BlockState currentState = level.getBlockState(pos);

                if (!canPlaceSolidHere(currentState)) {
                    continue;
                }

                BlockState slabState = material.getSolidifiedSlab().defaultBlockState();

                if (slabState.hasProperty(SlabBlock.TYPE)) {
                    slabState = slabState.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
                }

                if (transferSolidToWorld(level, basinKey, pos, currentState, slabState, SLAB_MB)) {
                    solidAmountMb -= SLAB_MB;
                }

                break;
            }
        }

        if (solidAmountMb > 0 && solidAmountMb < SLAB_MB) {
            TrialMod.LOGGER.info(
                    "[Foundry] Basin {} retained {} mB {} as solid residue.",
                    basinKey, solidAmountMb, material.getSerializedName()
            );
        }
    }

    private static boolean canPlaceSolidHere(
            BlockState state) {

        return state.isAir() || state.getBlock() instanceof FoundryMoltenDisplayBlock;
    }

    private static boolean transferSolidToWorld(ServerLevel level, BlockPos basinKey, BlockPos pos, BlockState oldState,
                                                BlockState newState, int amountMb) {

        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);

        boolean placed = level.setBlock(pos, newState, Block.UPDATE_ALL);

        if (!placed) {
            return false;
        }

        boolean removed = savedData.tryRemoveSolidMaterial(basinKey, amountMb);

        if (!removed) {
            level.setBlock(pos, oldState, Block.UPDATE_ALL);

            TrialMod.LOGGER.warn(
                    "[Foundry] Failed to transfer {} mB solid material "
                            + "from basin {} into world block at {}. Restored previous state.",
                    amountMb, basinKey, pos
            );

            return false;
        }

        TrialMod.LOGGER.info(
                "[Foundry] Physicalized {} mB solid material "
                        + "from basin {} at {}.",
                amountMb, basinKey, pos
        );

        return true;
    }
}