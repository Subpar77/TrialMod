package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.block.custom.FoundryChannelBlock;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class FoundryChannelVisuals {
    private static final int DISPLAY_DURATION_TICKS = 40;

    private static final Map<ServerLevel, Map<BlockPos, Long>> EXPIRY_TIMES =
            new WeakHashMap<>();

    private FoundryChannelVisuals() {}

    public static void showFlow(ServerLevel level, Set<BlockPos> channels, FoundryMaterial material) {
        FoundryChannelVisual visual = FoundryChannelVisual.fromMaterial(material);
        long expiresAt = level.getGameTime() + DISPLAY_DURATION_TICKS;

        Map<BlockPos, Long> expiryTimes = EXPIRY_TIMES.computeIfAbsent(level, unused -> new HashMap<>());

        for(BlockPos channelPos : channels) {
            if(!level.hasChunkAt(channelPos)) {
                continue;
            }

            BlockState state = level.getBlockState(channelPos);

            if(!(state.getBlock() instanceof FoundryChannelBlock)) {
                continue;
            }

            expiryTimes.put(channelPos.immutable(), expiresAt);

            if(state.getValue(FoundryChannelBlock.VISUAL) != visual) {
                level.setBlock(channelPos, state.setValue(FoundryChannelBlock.VISUAL, visual), Block.UPDATE_CLIENTS);
            }

            level.scheduleTick(channelPos, state.getBlock(), DISPLAY_DURATION_TICKS);
        }
    }

    public static void forget(ServerLevel level, BlockPos pos) {
        Map<BlockPos, Long> expiryTimes = EXPIRY_TIMES.get(level);

        if(expiryTimes == null) {
            return;
        }

        expiryTimes.remove(pos);

        if(expiryTimes.isEmpty()) {
            EXPIRY_TIMES.remove(level);
        }
    }

    public static void onScheduledTick(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if(!(state.getBlock() instanceof FoundryChannelBlock)) {
            forget (level, pos);
            return;
        }

        Map<BlockPos, Long> expiryTimes = EXPIRY_TIMES.get(level);
        Long expiresAt = expiryTimes == null ? null : expiryTimes.get(pos);

        if(expiresAt != null && expiresAt > level.getGameTime()) {
            int remaingTicks = (int) (expiresAt - level.getGameTime());
            level.scheduleTick(pos, state.getBlock(), remaingTicks);
            return;
        }

        forget(level, pos);

        if(state.getValue(FoundryChannelBlock.VISUAL) != FoundryChannelVisual.NONE) {
            level.setBlock(pos, state.setValue(FoundryChannelBlock.VISUAL, FoundryChannelVisual.NONE),
                    Block.UPDATE_CLIENTS);
        }
    }
}
