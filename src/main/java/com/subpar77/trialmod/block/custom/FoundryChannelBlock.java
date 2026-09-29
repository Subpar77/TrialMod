package com.subpar77.trialmod.block.custom;

import com.mojang.serialization.MapCodec;
import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.foundry.FoundryChannelNetwork;
import com.subpar77.trialmod.foundry.FoundryTransportConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level instanceof ServerLevel level1) {
            Set<BlockPos> possiblePos = FoundryChannelNetwork.findConnectedChannels(level1, pos);

            TrialMod.LOGGER.info(
                    "[Foundry] Channel Block at: {} found {} connected channels. Positions: {}",
                    pos, possiblePos.size(), possiblePos
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
            boolean connected = false;

            if (neighborState.getBlock() instanceof FoundryTransportConnectable connectable) {
                connected = connectable.canTransportConnect(neighborState, direction.getOpposite());
            }

            channelState = channelState.setValue(getConnectionProperty(direction), connected);
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

        boolean connected = false;

        if(neighborState.getBlock() instanceof FoundryTransportConnectable connectable) {
            connected = connectable.canTransportConnect(neighborState, direction.getOpposite());
        }

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
}
