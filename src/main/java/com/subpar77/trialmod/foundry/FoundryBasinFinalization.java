package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;

import java.util.Set;

public class FoundryBasinFinalization {
    private FoundryBasinFinalization() {}

    public static boolean finalizeBasin(ServerLevel level, BlockPos basinKey, Set<BlockPos> rememberedInterior) {

        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);
        FoundryMaterial material = savedData.getMaterial(basinKey);

        if(material == null) {
            return savedData.getAmountMb(basinKey) == 0;
        }

        int amountMb = savedData.getAmountMb(basinKey);

        if(amountMb < FoundryBasin.MB_PER_BUCKET) {
            return false;
        }

        if(hasMoltenMaterialNearby(level, rememberedInterior, material)) {
            return false;
        }

        BlockPos dropPos = findDropPosition(level, basinKey, rememberedInterior);
        ItemStack slagStack = new ItemStack(material.getSolidifiedBlock().asItem());
        ItemEntity itemEntity = new ItemEntity(level, dropPos.getX() + 0.5, dropPos.getY() + 0.5, dropPos.getZ() + 0.5,
                slagStack);

        boolean spawned = level.addFreshEntity(itemEntity);

        if(!spawned) {
            TrialMod.LOGGER.warn(
            "[Foundry] Failed to physicalize 1000 mB {} from basin {}",
                    material.getSerializedName(), basinKey
            );

            return false;
        }

        boolean removed = savedData.tryRemoveMoltenMaterial(basinKey, FoundryBasin.MB_PER_BUCKET);

        if(!removed) {
            itemEntity.discard();

            TrialMod.LOGGER.error(
                    "[Foundry] Failed to remove 1000 mB {} from basin {} during finalization.",
                    material.getSerializedName(), basinKey
            );

            return false;
        }

        TrialMod.LOGGER.info(
                "[Foundry] Finalized 1000 mB {} from basin {} as recoverable slag at {}.",
                material.getSerializedName(), basinKey, dropPos
        );

        return savedData.getAmountMb(basinKey) == 0;
    }

    private static boolean hasMoltenMaterialNearby(ServerLevel level, Set<BlockPos> rememberedInterior, FoundryMaterial material) {
        final int HORIZONTAL_RANGE = 6;
        final int DOWN_RANGE = 6;
        final int UP_RANGE = 2;

        for(BlockPos interiorPos : rememberedInterior) {

            BlockPos minPos = interiorPos.offset(-HORIZONTAL_RANGE, -DOWN_RANGE, -HORIZONTAL_RANGE);
            BlockPos masPos = interiorPos.offset(HORIZONTAL_RANGE, UP_RANGE, HORIZONTAL_RANGE);

            for(BlockPos checkPos : BlockPos.betweenClosed(minPos, masPos)) {
                FluidState fluidState = level.getFluidState(checkPos);

                if(fluidState.isEmpty()) {
                    continue;
                }

                if(material.getMoltenFluid().isSame(fluidState.getType())) {
                    return true;
                }
            }
        }

        return false;

    }

    private static BlockPos findDropPosition(ServerLevel level, BlockPos basinKey, Set<BlockPos> rememberedInterior) {

        for(BlockPos pos : rememberedInterior) {
            if(level.getBlockState(pos).isAir()) {
                return pos.immutable();
            }
        }

        for(BlockPos pos : rememberedInterior) {

            BlockPos above = pos.above();
            if(level.getBlockState(above).isAir()) {
                return above.immutable();
            }
        }

        return basinKey.immutable();
    }
}
