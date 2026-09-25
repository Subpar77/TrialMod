package com.subpar77.trialmod.foundry.recipe;

import com.subpar77.trialmod.TrialMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModFoundryRecipes {

    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, TrialMod.MODID);

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, TrialMod.MODID);

    public static final Supplier<RecipeType<FoundryMeltingRecipe>> FOUNDRY_MELTING_TYPE =
            RECIPE_TYPES.register("foundry_melting", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(
                    TrialMod.MODID, "foundry_melting")));

    public static final Supplier<RecipeSerializer<FoundryMeltingRecipe>> FOUNDRY_MELTING_SERIALIZER =
            RECIPE_SERIALIZER.register("foundry_melting", FoundryMeltingRecipeSerializer::new);

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZER.register(modEventBus);
    }
}
