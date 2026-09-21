package com.subpar77.trialmod.foundry.material;

import com.subpar77.trialmod.foundry.FoundryBasinSavedData;
import com.subpar77.trialmod.foundry.recipe.FoundryMeltingRecipe;
import com.subpar77.trialmod.foundry.recipe.ModFoundryRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;

public class FoundryItemMetling {

    private FoundryItemMetling() {
    }

    public static void process(ServerLevel level, BlockPos basinKey, Set<BlockPos> interior, float temperature) {

        if (interior.isEmpty()) {
            return;
        }

        Set<ItemEntity> itemEntities = new HashSet<>();

        for (BlockPos pos : interior) {
            itemEntities.addAll(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos)));
        }

        if (itemEntities.isEmpty()) {
            return;
        }

        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);

        int capacityMb = interior.size() * 1000;

        for (ItemEntity itemEntity : itemEntities) {
            ItemStack stack = itemEntity.getItem();

            if (stack.isEmpty()) {
                continue;
            }

            SingleRecipeInput input = new SingleRecipeInput(stack);
            var recipeHolder = level.getRecipeManager().getRecipeFor(
                    ModFoundryRecipes.FOUNDRY_MELTING_TYPE.get(), input, level);

            if (recipeHolder.isEmpty()) {
                continue;
            }

            FoundryMeltingRecipe recipe = recipeHolder.get().value();
            FoundryMaterial material = recipe.getMaterial();

            if (temperature < material.getMeltingTemperature()) {
                continue;
            }

            boolean accepted = savedData.tryAddMaterial(basinKey, material, recipe.getAmountMb(), capacityMb);

            if (!accepted) {
                continue;
            }

            stack.shrink(1);

            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack);
            }
            return;
        }
    }
}
