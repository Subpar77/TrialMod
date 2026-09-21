package com.subpar77.trialmod.block.custom;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.entity.FoundryTapBlockEntity;
import com.subpar77.trialmod.block.entity.ModBlockEntities;
import com.subpar77.trialmod.foundry.*;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FoundryTapBlock extends Block implements EntityBlock {
    public FoundryTapBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(OPEN, false));

    }

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 11, 16);

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new FoundryTapBlockEntity(blockPos, blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        boolean isOpen = state.getValue(OPEN);


        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (isOpen) {
            level.setBlock(pos, state.setValue(OPEN, false), Block.UPDATE_ALL);
            TrialMod.LOGGER.info(
                    "[Foundry] Tap {} closed.",
                    pos
            );

            return InteractionResult.SUCCESS;
        }

        if(!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
    }

        Direction outputDirection = state.getValue(FACING);
        Direction basinDirection = outputDirection.getOpposite();
        BlockPos wallPos = pos.relative(basinDirection);
        Optional<BasinDetails> basin = FoundryBasin.inspectBasin(serverLevel, wallPos);

        if(basin.isEmpty()) {
            TrialMod.LOGGER.debug(
                    "[Foundry] Tap {} could not open: no valid basin behind wall {}.",
                    pos, wallPos
            );

            return InteractionResult.SUCCESS;
        }

        level.setBlock(pos, state.setValue(OPEN, true), Block.UPDATE_ALL);

        BasinDetails details = basin.get();

        TrialMod.LOGGER.info(
                "[Foundry] Tap {} opened for basin {}. "
                + "Material={}, total={} mB, molten={} mB, solid={} mB, temperature={}F.",
                pos, details.basinKey(), details.material().map(FoundryMaterial::getSerializedName).orElse("none"),
                details.amountMb(), details.moltenAmountMb(), details.solidAmountMb(), details.temperature()
        );

        return InteractionResult.SUCCESS;
    }



    private static <E extends BlockEntity, A extends  BlockEntity>
    @Nullable BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> type, BlockEntityType<E> checkedType,
                                                      BlockEntityTicker<? super E> ticker) {
        return checkedType == type ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return createTickerHelper(type, ModBlockEntities.FOUNDRY_TAP_BLOCK_ENTITY.get(), FoundryTapBlockEntity::clientTick);
        }

        return createTickerHelper(type, ModBlockEntities.FOUNDRY_TAP_BLOCK_ENTITY.get(), FoundryTapBlockEntity::serverTick);
    }


    private String formatBuckets(int amount) {
        return amount + " " + (amount == 1 ? "bucket" : "buckets");
    }

}
