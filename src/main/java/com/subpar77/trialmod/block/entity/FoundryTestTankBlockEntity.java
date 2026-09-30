package com.subpar77.trialmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class FoundryTestTankBlockEntity extends BlockEntity {

    public FoundryTestTankBlockEntity(BlockPos pos, BlockState state) {

        super(ModBlockEntities.TEST_TANK_ENTITY.get(), pos, state);
    }

    private final FluidTank fluidTank = new FluidTank(4000);

    public IFluidHandler getFluidHandler() {
        return fluidTank;
    }
}
