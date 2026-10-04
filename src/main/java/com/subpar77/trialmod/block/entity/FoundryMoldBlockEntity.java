package com.subpar77.trialmod.block.entity;

import com.subpar77.trialmod.block.custom.FoundryMoldBlock;
import com.subpar77.trialmod.fluid.ModFluids;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.Optional;
import java.util.function.Predicate;

public class FoundryMoldBlockEntity extends BlockEntity {
    public FoundryMoldBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FOUNDRY_MOLD_ENTITY.get(), pos, blockState);
    }




    private final FluidTank reservoir = new FluidTank(4000, stack ->
            FoundryMaterial.fromFluid(stack.getFluid()).isPresent()) {
        @Override
        protected void onContentsChanged() {
            FoundryMoldBlockEntity.this.setChanged();
        }
    };

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Reservoir", reservoir.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        reservoir.readFromNBT(registries, tag.getCompound("Reservoir"));
    }

    public IFluidHandler getFluidHandler() {return reservoir;}
}