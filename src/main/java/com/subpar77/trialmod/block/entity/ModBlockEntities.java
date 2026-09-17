package com.subpar77.trialmod.block.entity;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TrialMod.MODID);

    public static final Supplier<BlockEntityType<FoundryTapBlockEntity>> FOUNDRY_TAP_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "foundry_tap_block_entity", () -> BlockEntityType.Builder.of(FoundryTapBlockEntity::new, ModBlocks.FOUNDRY_TAP.get()).build(null)
    );


    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
