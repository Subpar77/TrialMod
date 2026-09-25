package com.subpar77.trialmod.block.custom;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.foundry.FoundryHeat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

public class MoltenCopperBlock extends LiquidBlock {

    private static final int COOLING_DELAY_TICKS = 20 * 50;

    public MoltenCopperBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if(level.isClientSide) {
            return;
        }

        if(state.getFluidState().isSource()) {
            level.scheduleTick(pos, this, COOLING_DELAY_TICKS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        FluidState fluidState = state.getFluidState();

        if(!fluidState.isSource()) {
            return;
        }

        if(FoundryHeat.getHeatSource(level, pos).isPresent()) {
            level.scheduleTick(pos, this, COOLING_DELAY_TICKS);
            return;
        }

        level.setBlock(pos, ModBlocks.COPPER_SLAG_BLOCK.get().defaultBlockState(), MoltenCopperBlock.UPDATE_ALL);

        TrialMod.LOGGER.info(
                "[Foundry] Molten copper source at {} cooled into slag.",
                pos
        );
    }



    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (!level.isClientSide) {
            entity.lavaHurt();
        }
    }
}
