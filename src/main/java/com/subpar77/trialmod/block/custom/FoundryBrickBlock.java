package com.subpar77.trialmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.jetbrains.annotations.Nullable;


public class FoundryBrickBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<FoundryBrickBlock> CODEC = simpleCodec(FoundryBrickBlock::new);

    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;

    public FoundryBrickBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH)
                .setValue(SHAPE, StairsShape.STRAIGHT));
    }

    private static StairsShape getBrickShape(BlockState state, BlockGetter level, BlockPos pos) {

        Direction facing = state.getValue(FACING);
        BlockState frontState = level.getBlockState(pos.relative(facing));

        if(isFoundryBrick(frontState)) {
            Direction frontFacing = frontState.getValue(FACING);

            if(frontFacing.getAxis() != facing.getAxis() && canTakeShape(state, level, pos, frontFacing.getOpposite())) {

                if(frontFacing == facing.getCounterClockWise()) {
                    return StairsShape.INNER_LEFT;
                }

                return StairsShape.INNER_RIGHT;
            }
        }

        BlockState backState = level.getBlockState(pos.relative(facing.getOpposite()));

        if(isFoundryBrick(backState)) {
            Direction backFacing = backState.getValue(FACING);

            if(backFacing.getAxis() != facing.getAxis() && canTakeShape(state, level, pos, backFacing)) {

                if(backFacing == facing.getCounterClockWise()) {
                    return StairsShape.OUTER_LEFT;
                }

                return StairsShape.OUTER_RIGHT;
            }
        }

        return StairsShape.STRAIGHT;
    }

    private static boolean canTakeShape(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {

        BlockState sideState = level.getBlockState(pos.relative(direction));

        return !isFoundryBrick(sideState) || sideState.getValue(FACING) != state.getValue(FACING);
    }

    private static boolean isFoundryBrick(BlockState state) {
        return state.getBlock() instanceof FoundryBrickBlock;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {

        BlockState state = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());

        return state.setValue(SHAPE, getBrickShape(state, context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SHAPE);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(SHAPE, getBrickShape(state, level, pos));
    }
}
