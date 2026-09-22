package com.subpar77.trialmod.item;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.fluid.ModFluids;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TrialMod.MODID);

    public static final DeferredItem<BlockItem> FOUNDRY_BRICK_ITEM =
            ITEMS.registerSimpleBlockItem("foundry_brick", ModBlocks.FOUNDRY_BRICK, new Item.Properties());
    public static final DeferredItem<BlockItem> COPPER_SLAG_ITEM =
            ITEMS.registerSimpleBlockItem("copper_slag_block",ModBlocks.COPPER_SLAG_BLOCK, new Item.Properties());
    public static final DeferredItem<BlockItem> COPPER_SLAG_SLAB_ITEM =
            ITEMS.registerSimpleBlockItem("copper_slag_slab",ModBlocks.COPPER_SLAG_SLAB, new Item.Properties());
    public static final DeferredItem<BlockItem> TAP_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("foundry_tap", ModBlocks.FOUNDRY_TAP, new Item.Properties());


    public static final DeferredItem<Item> MOLTEN_COPPER_BUCKET =
            ITEMS.register("molten_copper_bucket", ()-> new BucketItem(ModFluids.MOLTEN_COPPER_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
