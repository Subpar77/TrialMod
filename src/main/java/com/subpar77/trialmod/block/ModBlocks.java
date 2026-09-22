package com.subpar77.trialmod.block;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.custom.*;
import com.subpar77.trialmod.fluid.ModFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TrialMod.MODID);

    public static final DeferredBlock<FoundryBrickBlock> FOUNDRY_BRICK =
            BLOCKS.registerBlock("foundry_brick", FoundryBrickBlock::new,
                    BlockBehaviour.Properties.of().strength(2.0F, 8.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<FoundryCopperSlagBlock> COPPER_SLAG_BLOCK =
            BLOCKS.registerBlock("copper_slag_block", FoundryCopperSlagBlock::new,
                    BlockBehaviour.Properties.of().strength(2.0F, 8.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
    public static final DeferredBlock<SlabBlock> COPPER_SLAG_SLAB =
            BLOCKS.registerBlock("copper_slag_slab", SlabBlock::new,
                    BlockBehaviour.Properties.of().strength(2.0F, 8.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<FoundryTapBlock> FOUNDRY_TAP =
            BLOCKS.registerBlock("foundry_tap", FoundryTapBlock::new,
                    BlockBehaviour.Properties.of().strength(2.0F, 8.0F).sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<MoltenCopperBlock> MOLTEN_COPPER_BLOCK =
            BLOCKS.register("molten_copper", () -> new MoltenCopperBlock(ModFluids.MOLTEN_COPPER_SOURCE.get(),
                    BlockBehaviour.Properties.of().replaceable().noCollission().strength(100.0F).pushReaction(PushReaction.DESTROY)
                            .noLootTable().liquid().sound(SoundType.EMPTY)));
    public static final DeferredBlock<FoundryMoltenDisplayBlock> MOLTEN_COPPER_DISPLAY =
            BLOCKS.registerBlock("molten_copper_display", FoundryMoltenDisplayBlock::new,
                    BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().strength(100.0F).pushReaction(PushReaction.DESTROY)
                            .sound(SoundType.EMPTY).lightLevel(state -> 12));


public static void register(IEventBus modEventBus) {
    BLOCKS.register(modEventBus);
}


}
