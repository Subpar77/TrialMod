package com.subpar77.trialmod.foundry.recipe;

import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class FoundryMeltingRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final FoundryMaterial material;
    private final int amountMb;

    public FoundryMeltingRecipe(Ingredient ingredient, FoundryMaterial material, int amountMb) {
        this.ingredient = ingredient;
        this.material = material;
        this.amountMb = amountMb;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public FoundryMaterial getMaterial() {
        return material;
    }

    public int getAmountMb() {
        return amountMb;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();

        ingredients.add(ingredient);

        return ingredients;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModFoundryRecipes.FOUNDRY_MELTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModFoundryRecipes.FOUNDRY_MELTING_TYPE.get();
    }
}
