package com.subpar77.trialmod.block.custom;

import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.foundry.FoundryStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class TapBlock extends Block {
    public TapBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));

    }

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            Direction outputDirection = state.getValue(FACING);
            Direction basinDirection = outputDirection.getOpposite();
            BlockPos wallPos = pos.relative(basinDirection);

            if (!level.getBlockState(wallPos).is(ModBlocks.FOUNDRY_BRICK)) {
                player.displayClientMessage(Component.literal("No Foundry Brick Found"), false);
            } else {
                Optional<Set<BlockPos>> result = FoundryStructure.findBasinFromWall(level, wallPos);
                if (result.isEmpty()){
                    player.displayClientMessage(Component.literal("Invalid Foundry Basin"), false);
                } else {
                    Set<BlockPos> interiorPositions = result.get();
                    player.displayClientMessage(Component.literal("Valid Foundry Basin! Capacity: "
                    + interiorPositions.size()), false);
                }

            }

        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
