package com.subpar77.trialmod.block;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.custom.FoundryBrickBlock;
import com.subpar77.trialmod.block.custom.FoundryTapBlock;
import com.subpar77.trialmod.fluid.ModFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TrialMod.MODID);

    public static final DeferredBlock<FoundryBrickBlock> FOUNDRY_BRICK =
            BLOCKS.registerBlock("foundry_brick", FoundryBrickBlock::new,
                    BlockBehaviour.Properties.of().strength(2.0F, 8.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<FoundryTapBlock> FOUNDRY_TAP =
            BLOCKS.registerBlock("foundry_tap", FoundryTapBlock::new,
                    BlockBehaviour.Properties.of().strength(2.0F, 8.0F).sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<LiquidBlock> MOLTEN_COPPER_BLOCK =
            BLOCKS.register("molten_copper", () -> new LiquidBlock(ModFluids.MOLTEN_COPPER_SOURCE.get(),
                    BlockBehaviour.Properties.of().noCollission().strength(100.0F).noLootTable()));


public static void register(IEventBus modEventBus) {
    BLOCKS.register(modEventBus);
}


}
