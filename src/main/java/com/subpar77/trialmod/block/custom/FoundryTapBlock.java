package com.subpar77.trialmod.block.custom;

import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.block.entity.FoundryTapBlockEntity;
import com.subpar77.trialmod.block.entity.ModBlockEntities;
import com.subpar77.trialmod.fluid.ModFluids;
import com.subpar77.trialmod.foundry.*;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
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

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 11, 16);
    private static final int TRANSFER_INTERVAL_TICKS = 20;
    private int transferCooldown =0;

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

        if (player.isShiftKeyDown()) {
            return useWhileSneaking(state, level, pos, player, hitResult);
        }

        if(isOpen) {
               level.setBlock(pos, state.setValue(OPEN, false), Block.UPDATE_ALL);
           } else {
            Direction outputDirection = state.getValue(FACING);
            Direction basinDirection = outputDirection.getOpposite();
            BlockPos wallPos = pos.relative(basinDirection);
            Optional<BasinDetails> basinDetails = FoundryBasin.inspectBasin(level, wallPos);

            if (basinDetails.isPresent()) {
                level.setBlock(pos, state.setValue(OPEN, true), Block.UPDATE_ALL);
                BasinDetails details = basinDetails.get();



                if (details.storedBuckets() > 0 && details.fluidType().isPresent()) {
                    Component fluidName = details.fluidType().get().getDescription();
                    BlockPos sourcePos = details.sourcePos().get();
                    Component message = Component.literal("Opening tap: " + formatBuckets(details.storedBuckets()) + " of ")
                            .append(fluidName).append(Component.literal("."));

                    player.displayClientMessage(message, false);
                    player.displayClientMessage(Component.literal("Located at: X=" + sourcePos.getX() + ", Y=" +sourcePos.getY() + ", Z=" +sourcePos.getZ()),false);

                } else {
                    player.displayClientMessage(Component.literal("Opening tap: basin is empty."), false);
                }
            }
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
        if (level.isClientSide) {
            return createTickerHelper(type, ModBlockEntities.FOUNDRY_TAP_BLOCK_ENTITY.get(), FoundryTapBlockEntity::clientTick);
        }

        return createTickerHelper(type, ModBlockEntities.FOUNDRY_TAP_BLOCK_ENTITY.get(), FoundryTapBlockEntity::serverTick);
    }

    private InteractionResult useWhileSneaking(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

                Direction outputDirection = state.getValue(FACING);
                Direction basinDirection = outputDirection.getOpposite();
                BlockPos wallPos = pos.relative(basinDirection);

//                Optional<FoundryMaterial> material = FoundryMaterial.fromFluid(ModFluids.MOLTEN_COPPER_SOURCE.get());
//                System.out.println(material);

                if (!level.getBlockState(wallPos).is(ModBlockTags.VALID_FOUNDRY_BLOCKS)) {
                    player.displayClientMessage(Component.literal("No Foundry Brick Found"), false);
                } else {
                    Optional<BasinDetails> basinDetails = FoundryBasin.inspectBasin(level, wallPos);
                    if (basinDetails.isEmpty()) {
                        player.displayClientMessage(Component.literal("Invalid Foundry Basin"), false);
                    } else {
                        BasinDetails details = basinDetails.get();
                        Optional<Set<BlockPos>> interior = FoundryStructure.findBasinFromWall(level, wallPos);
                        Optional<BlockPos> basinKey = FoundryBasin.findBasinKey(interior.get());

                        player.displayClientMessage(Component.literal("Valid Foundry Found!"), false);
                        player.displayClientMessage(Component.literal("Max capacity of: " + formatBuckets(details.capacity())), false);
                        basinKey.ifPresent(key -> player.displayClientMessage(Component.literal("Basin Key: " + key), false));

                        if (details.fluidType().isPresent()) {
                            Component fluidName = details.fluidType().get().getDescription();
                            player.displayClientMessage(Component.literal("Currently Contains: " + formatBuckets(details.storedBuckets()) + " of ").append(fluidName), false);
                            player.displayClientMessage(Component.literal("Located at: ").append(String.valueOf(details.sourcePos())).append(Component.literal(".")), false);
                        }

                        player.displayClientMessage(Component.literal("Current free capacity: " + formatBuckets(details.availableCapacity())), false);

                        Set<BlockPos> checkHeat = interior.get();
                        Optional<HeatSourceData> heat = FoundryHeat.inspect(level, checkHeat);
                        if (heat.isEmpty()) {
                            player.sendSystemMessage(Component.literal("No active heat sources"));
                        } else {
                            player.sendSystemMessage(Component.literal("Heat target: " + heat.get().maxTemperature()));
                            player.sendSystemMessage(Component.literal("Heating rate: " + heat.get().heatingRate() ));
                        }

                        if (level instanceof ServerLevel serverLevel) {
                            BlockPos key = basinKey.get();
                            FoundryBasinSavedData data = FoundryBasinSavedData.get(serverLevel);

                            //data.setTemperature(key, 234.0F);
                            player.sendSystemMessage(Component.literal("Basin temperature: " + data.getTemperature(key)));
                        }
                    }

                }

        return InteractionResult.sidedSuccess(level.isClientSide);

    }

    private String formatBuckets(int amount) {
        return amount + " " + (amount == 1 ? "bucket" : "buckets");
    }

}
