package com.subpar77.trialmod.block;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.custom.*;
import com.subpar77.trialmod.fluid.ModFluids;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TrialMod.MODID);

    public static final DeferredBlock<StoneBasinWallBlock> STONE_BASIN_WALL =
            BLOCKS.registerBlock("stone_basin_wall", StoneBasinWallBlock::new,
                    BlockBehaviour.Properties.of().strength(2.0F, 8.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()
                            .noOcclusion());
    public static final DeferredBlock<Block> FOUNDRY_BRICK =
            BLOCKS.registerSimpleBlock("foundry_brick", BlockBehaviour.Properties.of().strength(2.0F,
                            8.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()
                            .noOcclusion());
    public static final DeferredBlock<SlabBlock> FOUNDRY_BRICK_SLAB =
            BLOCKS.registerBlock("foundry_brick_slab", SlabBlock::new, BlockBehaviour.Properties.of()
                            .strength(2.0F, 8.0F).sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final DeferredBlock<FoundryChannelBlock> FOUNDRY_CHANNEL_BLOCK =
            BLOCKS.registerBlock("foundry_channel", FoundryChannelBlock::new, BlockBehaviour.Properties.of().strength(2.0F,
                            8.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()
                    .noOcclusion());
    public static final DeferredBlock<FoundryTestTankBlock> TEST_TANK_BLOCK =
            BLOCKS.registerBlock("test_tank_block", FoundryTestTankBlock::new, BlockBehaviour.Properties.of().strength(2.0F,
                            8.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()
                    .noOcclusion().noLootTable());

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
