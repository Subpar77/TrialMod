package com.subpar77.trialmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.Nullable;

public class FoundryChannelBlock extends Block {
    public static final MapCodec<FoundryChannelBlock> CODEC = simpleCodec(FoundryChannelBlock::new);

    public static final BooleanProperty  NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty  EAST = BlockStateProperties.EAST;
    public static final BooleanProperty  SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty  WEST = BlockStateProperties.WEST;


    public FoundryChannelBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.getStateDefinition().any().setValue(NORTH, false)
                .setValue(EAST, false).setValue(SOUTH,false).setValue(WEST, false));
    }

    private static BooleanProperty getConnectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP, DOWN ->
                throw new IllegalArgumentException("Channel connections must be horizontal");

        };
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {


        BlockState channelState = this.defaultBlockState();

        for(Direction direction : new Direction[] {
                Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {

            BlockPos neighborPos = context.getClickedPos().relative(direction);
            BlockState neighborState = context.getLevel().getBlockState(neighborPos);

            if (neighborState.getBlock() instanceof FoundryChannelBlock) {
                channelState = channelState.setValue(getConnectionProperty(direction), true);
            }
        }

        return channelState;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }


    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {

        BlockState channelState = state;


        if(direction == Direction.UP || direction == Direction.DOWN) {
            return state;
        }

        boolean connected = neighborState.getBlock() instanceof FoundryChannelBlock;

        channelState = state.setValue(getConnectionProperty(direction), connected);

        return channelState;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
