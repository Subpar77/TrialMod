package com.subpar77.trialmod;

import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.block.entity.ModBlockEntities;
import com.subpar77.trialmod.client.ClientModEvents;
import com.subpar77.trialmod.fluid.ModFluids;
import com.subpar77.trialmod.item.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TrialMod.MODID)
public class TrialMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "trial_mod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus and pass them in automatically.
    public TrialMod(IEventBus modEventBus) {

        modEventBus.addListener(this::addCreative);

        // Register the Deferred Register to the mod event bus so items get registered
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModFluids.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(ClientModEvents::registerClientExtensions);
        }



    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.FOUNDRY_BRICK_ITEM.get());
            event.accept(ModItems.TAP_BLOCK_ITEM.get());
            event.accept(ModItems.MOLTEN_COPPER_BUCKET.get());
            event.accept(ModBlocks.COPPER_SLAG_BLOCK.get());
        }
    }
}
