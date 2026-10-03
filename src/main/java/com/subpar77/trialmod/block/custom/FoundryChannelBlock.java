package com.subpar77.trialmod.block.custom;

import com.mojang.serialization.MapCodec;
import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.foundry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class FoundryChannelBlock extends Block implements FoundryTransportConnectable {
    public static final MapCodec<FoundryChannelBlock> CODEC = simpleCodec(FoundryChannelBlock::new);

    public static final BooleanProperty  NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty  EAST = BlockStateProperties.EAST;
    public static final BooleanProperty  SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty  WEST = BlockStateProperties.WEST;


    public FoundryChannelBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.getStateDefinition().any().setValue(NORTH, false)
                .setValue(EAST, false).setValue(SOUTH,false).setValue(WEST, false)
                .setValue(VISUAL, FoundryChannelVisual.NONE));
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

    private static int countNeighbors(BlockState state) {
        int count = 0;

        for(Direction direction : new Direction[] {
                Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {

            BooleanProperty property = getConnectionProperty(direction);

            if(state.getValue(property)) {
                count++;
            }
        }

        return count;
    }

    public static boolean isTransportOpen(BlockState state, Direction direction){

        if(direction == Direction.UP || direction == Direction.DOWN) {
            return false;
        }

        int connections = countNeighbors(state);
        boolean isOpen = false;

        switch (connections) {
            case 0 -> isOpen = direction == Direction.EAST || direction == Direction.WEST;
            case 1 -> isOpen = state.getValue(getConnectionProperty(direction)) || state.getValue(getConnectionProperty(direction.getOpposite()));
            case 2, 3, 4 -> isOpen = state.getValue(getConnectionProperty(direction));
            default -> throw new IllegalStateException("Connections can not exceed 4.");

        }

        return isOpen;
    }

    private static boolean connectsToNeighbor(LevelAccessor level, BlockPos neighborPos, BlockState neighborState,
                                              Direction direction) {

        if (neighborState.getBlock() instanceof FoundryTransportConnectable connectable) {
            return connectable.canTransportConnect(neighborState, direction.getOpposite());
        }

        if(level instanceof Level actuallevel)  {
            IFluidHandler handler = actuallevel.getCapability(Capabilities.FluidHandler.BLOCK,
                    neighborPos, direction.getOpposite());

            return handler != null;
        }

        return false;
    }

    public static final EnumProperty<FoundryChannelVisual> VISUAL = EnumProperty.create("visual",
            FoundryChannelVisual.class);

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level instanceof ServerLevel level1) {
            FoundryChannelNetworkResult result = FoundryChannelNetwork.findConnectedChannels(level1, pos);

            Set<BlockPos> possiblePos = result.channels();
            List<FoundryFluidDestination> destinations = result.destinations();
            TrialMod.LOGGER.info(
                    "[Foundry] Channel Block at: {} found {} connected channels and {} fluid destinations.",
                    pos, possiblePos.size(), destinations.size()
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {


        BlockState channelState = this.defaultBlockState();

        for (Direction direction : new Direction[] {
                Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {

            BlockPos neighborPos = context.getClickedPos().relative(direction);
            BlockState neighborState = context.getLevel().getBlockState(neighborPos);
            boolean connected = connectsToNeighbor(context.getLevel(), neighborPos, neighborState, direction);

            channelState = channelState.setValue(getConnectionProperty(direction), connected);
        }

            return channelState;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, VISUAL);
    }


    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {

        BlockState channelState = state;


        if(direction == Direction.UP || direction == Direction.DOWN) {
            return state;
        }

        boolean connected = connectsToNeighbor(level, neighborPos, neighborState, direction);

        channelState = state.setValue(getConnectionProperty(direction), connected);

        return channelState;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public boolean canTransportConnect(BlockState state, Direction direction) {

        if(direction == Direction.UP || direction == Direction.DOWN) {
            return false;
        }

        return true;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        FoundryChannelVisuals.onScheduledTick(level, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(level instanceof  ServerLevel serverLevel && !state.is(newState.getBlock())) {
            FoundryChannelVisuals.forget(serverLevel, pos);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
