package com.subpar77.trialmod.block.custom;

import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.block.entity.FoundryTapBlockEntity;
import com.subpar77.trialmod.block.entity.ModBlockEntities;
import com.subpar77.trialmod.foundry.FoundryStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockEventData;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class FoundryTapBlock extends Block implements EntityBlock {
    public FoundryTapBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(OPEN, false));

    }

    private record BasinDetails(int capacity, int storedBuckets,
                                int availableCapacity, Optional<FluidType> fluidType) {}

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
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide && player.isShiftKeyDown()) {
            return useWhileSneaking(state, level, pos, player, hitResult);
        }

        if (!level.isClientSide) {
            // Debugging
            BlockState newState = state.cycle(OPEN);

            level.setBlock(pos, state.cycle(OPEN), Block.UPDATE_ALL);

            player.sendSystemMessage(Component.literal("Tap open: " + newState.getValue(OPEN)));

        }

            return InteractionResult.SUCCESS;
        }

    private static <E extends BlockEntity, A extends  BlockEntity>
    @Nullable BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> type, BlockEntityType<E> checkedType,
                                                      BlockEntityTicker<? super E> ticker) {
        return checkedType == type ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {

        if (!level.isClientSide) {
            return null;
        }


        return createTickerHelper(type, ModBlockEntities.FOUNDRY_TAP_BLOCK_ENTITY.get(), FoundryTapBlockEntity::clientTick);
    }

    private InteractionResult useWhileSneaking(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

                Direction outputDirection = state.getValue(FACING);
                Direction basinDirection = outputDirection.getOpposite();
                BlockPos wallPos = pos.relative(basinDirection);

                if (!level.getBlockState(wallPos).is(ModBlocks.FOUNDRY_BRICK)) {
                    player.displayClientMessage(Component.literal("No Foundry Brick Found"), false);
                } else {
                    Optional<BasinDetails> basinDetails = examineStructure(level, wallPos);
                    if (basinDetails.isEmpty()) {
                        player.displayClientMessage(Component.literal("Invalid Foundry Basin"), false);
                    } else {
                        BasinDetails details = basinDetails.get();

                        player.displayClientMessage(Component.literal("Valid Foundry Found!"), false);
                        player.displayClientMessage(Component.literal("Max capacity of: " + formatBuckets(details.capacity())), false);

                        if (details.fluidType().isPresent()) {
                            Component fluidName = details.fluidType().get().getDescription();
                            player.displayClientMessage(Component.literal("Currently Contains: " + formatBuckets(details.storedBuckets()) + " of ").append(fluidName), false);
                        }

                        player.displayClientMessage(Component.literal("Current free capacity: " + formatBuckets(details.availableCapacity())), false);
                    }

                }

        return InteractionResult.sidedSuccess(level.isClientSide);

    }

    private Optional<BasinDetails> examineStructure(Level level, BlockPos wallPos) {

        Optional<Set<BlockPos>> result = FoundryStructure.findBasinFromWall(level, wallPos);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Set<BlockPos> interiorPositions = result.get();
        int storedBuckets = 0;
        FluidType detectedFluidType = null;

        for (BlockPos interiorPos : interiorPositions) {
            FluidState fluidState = level.getFluidState(interiorPos);

            if (fluidState.is(Tags.Fluids.LAVA)) {
                detectedFluidType = fluidState.getFluidType();

                if (fluidState.isSource()) {
                    storedBuckets++;
                }
            }
        }

        int capacity = interiorPositions.size();
        int availableCapacity = capacity - storedBuckets;

        return Optional.of(new BasinDetails(capacity, storedBuckets, availableCapacity, Optional.ofNullable(detectedFluidType)));

    }

    private String formatBuckets(int amount) {
        return amount + " " + (amount == 1 ? "bucket" : "buckets");
    }

}
