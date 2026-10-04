package com.subpar77.trialmod.block.custom;

import com.mojang.serialization.MapCodec;
import com.subpar77.trialmod.block.entity.FoundryMoldBlockEntity;
import com.subpar77.trialmod.block.entity.FoundryTestTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
}
