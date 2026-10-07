package com.subpar77.trialmod.foundry.recipe;

import com.subpar77.trialmod.block.entity.FoundryMoldBlockEntity;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.items.IItemHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.Level;

public final class FoundryCastingRecipes {
    private FoundryCastingRecipes() {}

    public static List<ItemStack> copyIngredientGrid(IItemHandler inventory) {
        List<ItemStack> moldStacks = new ArrayList<>();

        for(int slot = 0; slot < inventory.getSlots(); slot++) {
            moldStacks.add(inventory.getStackInSlot(slot).copy());
        }
        return moldStacks;
    }

    public static CraftingInput createInput(FoundryMoldBlockEntity mold, ItemStack virtualIngredient) {
        List<ItemStack> grid = copyIngredientGrid(mold.getItemHandler());

        for(int slot = 0; slot < grid.size(); slot++) {
            if(grid.get(slot).isEmpty() && !mold.isSlotDisabled(slot)) {
                grid.set(slot, virtualIngredient.copy());
            }
        }
        return CraftingInput.of(3, 3, grid);
    }

    public static List<RecipeHolder<CraftingRecipe>> findMatches(Level level, CraftingInput input) {
        return level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, input, level);
    }

    public static List<FoundryCastingMatch> findCastingMatches(Level level, FoundryMoldBlockEntity mold,
                                                               FoundryMaterial material) {

        ArrayList<FoundryCastingMatch> matches = new ArrayList<>();

        for(Item virtualItem : material.getVirtualIngredientItems()) {
            CraftingInput input = createInput(mold, new ItemStack(virtualItem));
            List<RecipeHolder<CraftingRecipe>> recipeMatches = findMatches(level, input);

            for(RecipeHolder<CraftingRecipe> recipeMatch : recipeMatches) {
                FoundryCastingMatch castingMatch = new FoundryCastingMatch(recipeMatch, virtualItem);

                matches.add(castingMatch);
            }

        }

        return matches;
    }
}
