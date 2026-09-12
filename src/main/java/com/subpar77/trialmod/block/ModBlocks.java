package com.subpar77.trialmod.block;

import com.subpar77.trialmod.TrialMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TrialMod.MODID);

    public static final DeferredBlock<Block> FOUNDRY_BRICK =
            BLOCKS.registerSimpleBlock("foundry_brick", BlockBehaviour.Properties.of().strength(2.0F, 8F).sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

public static void register(IEventBus modEventBus) {
    BLOCKS.register(modEventBus);
}


}
