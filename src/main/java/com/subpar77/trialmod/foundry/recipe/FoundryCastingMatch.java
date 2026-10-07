package com.subpar77.trialmod.foundry.recipe;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public record FoundryCastingMatch(RecipeHolder<CraftingRecipe> recipe, Item virtualIngredient) {
}
