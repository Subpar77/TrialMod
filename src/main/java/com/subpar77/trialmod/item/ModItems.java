package com.subpar77.trialmod.item;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TrialMod.MODID);

    public static final DeferredItem<BlockItem> FOUNDRY_BRICK_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.FOUNDRY_BRICK, new Item.Properties());

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
