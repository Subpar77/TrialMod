package com.subpar77.trialmod.block.custom;

import com.mojang.serialization.MapCodec;
import com.subpar77.trialmod.block.entity.FoundryTestTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FoundryTestTankBlock extends BaseEntityBlock {

    public static final MapCodec<FoundryTestTankBlock> CODEC =
            simpleCodec((FoundryTestTankBlock::new));

    public FoundryTestTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new FoundryTestTankBlockEntity(blockPos, blockState);
    }
}
