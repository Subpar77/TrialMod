package com.subpar77.trialmod.menu;

import com.subpar77.trialmod.TrialMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {

    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, TrialMod.MODID);

    public static final Supplier<MenuType<FoundryMoldMenu>> FOUNDRY_MOLD_MENU = MENU_TYPES.register(
            "foundry_mold", () -> IMenuTypeExtension.create(FoundryMoldMenu::new));

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
