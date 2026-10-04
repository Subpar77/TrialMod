package com.subpar77.trialmod;

import com.subpar77.trialmod.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB, TrialMod.MODID);


    public static final Supplier<CreativeModeTab> TRIAL_MOD_TAB = CREATIVE_MODE_TABS.register(
            "trial_mod", () -> CreativeModeTab.builder().title(Component.translatable("itemgroup.trial_mod"))
                    .icon(()-> new ItemStack(ModItems.STONE_BASIN_WALL.get())).displayItems((parameters, output) -> {
                    output.accept(ModItems.STONE_BASIN_WALL.get());
                    output.accept(ModItems.FOUNDRY_BRICK.get());
                    output.accept(ModItems.FOUNDRY_CHANNEL_BLOCK_ITEM.get());
                    output.accept(ModItems.FOUNDRY_BRICK_SLAB_ITEM.get());
                    output.accept(ModItems.TAP_BLOCK_ITEM.get());
                    output.accept(ModItems.MOLTEN_COPPER_BUCKET.get());
                    output.accept(ModItems.COPPER_SLAG_ITEM.get());
                    output.accept(ModItems.COPPER_SLAG_SLAB_ITEM.get());
                    output.accept(ModItems.COPPER_SLAG_CLUMP.get());
                    output.accept(ModItems.COPPER_SLAG_NUGGET.get());
                    output.accept(ModItems.FOUNDRY_MOLD_BLOCK_ITEM.get());
                    }).build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
