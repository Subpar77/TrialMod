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

import static com.subpar77.trialmod.foundry.FoundryStructure.findFoundryCenter;

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
            Optional<BlockPos> centerCandidate = findFoundryCenter(level, pos);
            BlockPos center = centerCandidate.get();

            if (centerCandidate.isEmpty()) {
                player.displayClientMessage(Component.literal("No Foundry Found"), false);
            } else {
                player.displayClientMessage(Component.literal("Valid Foundry Found! Center at: "
                        + center.getX() + ", "
                        + center.getY() + ", "
                        + center.getZ()), false);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
