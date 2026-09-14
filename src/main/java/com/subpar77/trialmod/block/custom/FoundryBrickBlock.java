package com.subpar77.trialmod.block.custom;

import com.subpar77.trialmod.foundry.FoundryStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;


public class FoundryBrickBlock extends Block {

    public FoundryBrickBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            Optional<BlockPos> centerCandidate = FoundryStructure.findFoundryCenter(level, pos);

            if (centerCandidate.isEmpty()) {
                player.displayClientMessage(Component.literal("No Foundry Found"), false);
            } else {
                BlockPos center = centerCandidate.orElse(BlockPos.of(0));
                player.displayClientMessage(Component.literal("Valid Foundry Found! Center at: "
                        + center.getX() + ", "
                        + center.getY() + ", "
                        + center.getZ()), false);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
