package com.subpar77.trialmod.block.custom;

import com.mojang.serialization.MapCodec;
import com.subpar77.trialmod.block.entity.FoundryMoldBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class FoundryMoldBlock extends BaseEntityBlock {

    public static final MapCodec<FoundryMoldBlock> CODEC =
            simpleCodec((FoundryMoldBlock::new));

    public FoundryMoldBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new FoundryMoldBlockEntity(blockPos, blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !state.is(newState.getBlock())) {

            if (level.getBlockEntity(pos) instanceof FoundryMoldBlockEntity mold) {
                IItemHandler inventory = mold.getItemHandler();

                for (int slot = 0; slot < inventory.getSlots(); slot++) {

                    if (!inventory.getStackInSlot(slot).isEmpty()) {
                        popResource(serverLevel, pos, inventory.extractItem(slot, Integer.MAX_VALUE,
                                false));
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
