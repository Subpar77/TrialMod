package com.subpar77.trialmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class FoundryTestTankBlockEntity extends BlockEntity {

    public FoundryTestTankBlockEntity(BlockPos pos, BlockState state) {

        super(ModBlockEntities.TEST_TANK_ENTITY.get(), pos, state);
    }

    private final FluidTank fluidTank = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            FoundryTestTankBlockEntity.this.setChanged();
        }
    };

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Tank", fluidTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluidTank.readFromNBT(registries, tag.getCompound("Tank"));
    }

    public IFluidHandler getFluidHandler() {
        return fluidTank;
    }
}
