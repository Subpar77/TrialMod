package com.subpar77.trialmod.foundry.material;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.foundry.FoundryBasin;
import com.subpar77.trialmod.foundry.FoundryBasinSavedData;
import com.subpar77.trialmod.foundry.recipe.FoundryMeltingRecipe;
import com.subpar77.trialmod.foundry.recipe.ModFoundryRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FoundryItemMelting {

    private FoundryItemMelting() {
    }

    public static void process(ServerLevel level, BlockPos basinKey, Set<BlockPos> interior, float temperature) {

        if (interior.isEmpty()) {
            return;
        }

        int capacityMb = FoundryBasin.getStateCapacityMb(level, interior);

        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);
        ItemStackHandler inputInventory = savedData.getInputInventory(basinKey);

        if(inputInventory != null) {
            ItemStack queuedStack = inputInventory.getStackInSlot(0);

            if(tryMeltOne(level, basinKey, queuedStack, temperature, capacityMb)) {
                inputInventory.extractItem(0, 1, false);
                return;
            }
        }

        Set<ItemEntity> itemEntities = new HashSet<>();

        for (BlockPos pos : interior) {
            itemEntities.addAll(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos)));
        }

        if (itemEntities.isEmpty()) {
            return;
        }

        for (ItemEntity itemEntity : itemEntities) {
            ItemStack stack = itemEntity.getItem();

            if (!tryMeltOne(level, basinKey, stack, temperature, capacityMb)) {
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

    private static boolean tryMeltOne(ServerLevel level, BlockPos basinKey, ItemStack stack,
                                       float temperature, int capacityMb) {
        if (stack.isEmpty()) {
            return false;
        }

        SingleRecipeInput input = new SingleRecipeInput(stack);
        var recipeHolder = level.getRecipeManager().getRecipeFor(
                ModFoundryRecipes.FOUNDRY_MELTING_TYPE.get(), input, level);

        if (recipeHolder.isEmpty()) {
            return false;
        }

        FoundryMeltingRecipe recipe = recipeHolder.get().value();
        FoundryMaterial material = recipe.getMaterial();

        if (temperature < material.getMeltingTemperature()) {
            TrialMod.LOGGER.debug(
                    "[Foundry] {} at basin {} required {}F; basin is {}F.",
                    stack.getHoverName().getString(), basinKey, material.getMeltingTemperature(), temperature
            );
            return false;
        }

        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);
        boolean accepted = savedData.tryAddMoltenMaterial(basinKey, material, recipe.getAmountMb(), capacityMb);

        if (!accepted) {
            TrialMod.LOGGER.debug(
                    "[Foundry] Could not melt {} at basin {}. Stored={} mB, molten={} mB, capacity={} mB.",
                    stack.getHoverName().getString(), basinKey, savedData.getAmountMb(basinKey),
                    savedData.getMoltenAmountMb(basinKey), capacityMb
            );
            return false;
        }

        String itemName = stack.getHoverName().getString();

        TrialMod.LOGGER.info(
                "[Foundry] Melted 1x {} into {} mB {} at basin {}. "
                        + "Stored={} mB, molten={} mB.",
                itemName, recipe.getAmountMb(), material.getSerializedName(), basinKey,
                savedData.getAmountMb(basinKey), savedData.getMoltenAmountMb(basinKey)
        );

        return true;
    }

    public static Optional<FoundryMeltingRecipe> getMeltingRecipe(ServerLevel level, ItemStack stack) {

        if(stack.isEmpty()) {
            return Optional.empty();
        }

        SingleRecipeInput input = new SingleRecipeInput(stack);

        var recipeHolder = level.getRecipeManager().getRecipeFor(ModFoundryRecipes.FOUNDRY_MELTING_TYPE.get(), input, level);

        return recipeHolder.map(holder -> holder.value());
    }

    public static boolean isMeltable(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

//        SingleRecipeInput input = new SingleRecipeInput(stack);

        return getMeltingRecipe(level, stack).isPresent();
    }
}
