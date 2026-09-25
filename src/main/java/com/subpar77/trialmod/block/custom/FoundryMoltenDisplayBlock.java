package com.subpar77.trialmod.block.custom;

import com.subpar77.trialmod.foundry.material.FoundryItemMelting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FoundryMoltenDisplayBlock extends Block {

    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, 16);

    public FoundryMoltenDisplayBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.getStateDefinition().any().setValue(LEVEL, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {

        super.entityInside(state, level, pos, entity
        );

        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(entity instanceof ItemEntity itemEntity && FoundryItemMelting.isMeltable(serverLevel, itemEntity.getItem())) {
            return;
        }

        entity.lavaHurt();
    }
}
