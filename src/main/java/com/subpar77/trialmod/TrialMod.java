package com.subpar77.trialmod;

import com.subpar77.trialmod.block.ModBlocks;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TrialMod.MODID)
public class TrialMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "trial_mod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Items which will all be registered under the "trial_mod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus and pass them in automatically.
    public TrialMod(IEventBus modEventBus) {

        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        ModBlocks.register(modEventBus);

    }
}
