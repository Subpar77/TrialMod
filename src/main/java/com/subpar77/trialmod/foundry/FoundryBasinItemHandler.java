package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.foundry.material.FoundryItemMelting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FoundryBasinItemHandler implements IItemHandler {
    private final ServerLevel level;
    private final BlockPos wallPos;

    public FoundryBasinItemHandler(ServerLevel level, BlockPos wallPos) {
        this.level = level;
        this.wallPos = wallPos.immutable();
    }

    private @Nullable ItemStackHandler getInventory() {
        if(!level.hasChunkAt(wallPos) || !level.getBlockState(wallPos).is(ModBlockTags.VALID_BASIN_WALLS)) {
            return null;
        }

        Optional<BasinDetails> basin = FoundryBasin.inspectBasin(level, wallPos);

        if(basin.isEmpty()) {
            return null;
        }

        return FoundryBasinSavedData.get(level).getInputInventory(basin.get().basinKey());
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if(slot != 0) {
            return  ItemStack.EMPTY;
        }

        ItemStackHandler inventory = getInventory();
        return inventory == null ? ItemStack.EMPTY : inventory.getStackInSlot(slot).copy();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if(!isItemValid(slot, stack)) {
            return stack;
        }

        ItemStackHandler inventory = getInventory();

        if(inventory == null) {
            return stack;
        }

        var basinResult = FoundryBasin.inspectBasin(level, wallPos);
        var recipeResult = FoundryItemMelting.getMeltingRecipe(level, stack);

        if(basinResult.isEmpty() || recipeResult.isEmpty()) {
            return stack;
        }

        BasinDetails basinDetails = basinResult.get();
        var recipe = recipeResult.get();
        var incomingMaterial = recipe.getMaterial();

        if(basinDetails.temperature() < incomingMaterial.getMeltingTemperature()) {
            return stack;
        }

        if(basinDetails.material().isPresent() && basinDetails.material().get() != incomingMaterial) {
            return stack;
        }

        ItemStack queuedStack = inventory.getStackInSlot(slot);

        if(!queuedStack.isEmpty() && !ItemStack.isSameItemSameComponents(queuedStack, stack)) {
            return stack;
        }

        int amountPerItemMb = recipe.getAmountMb();

        if(amountPerItemMb <= 0) {
            return stack;
        }

        long reservedMb = (long) queuedStack.getCount() * amountPerItemMb;
        long availableMb = Math.max(0L, (long) basinDetails.availableCapacityMb() - reservedMb);
        int allowedCount = (int) Math.min(stack.getCount(), availableMb / amountPerItemMb);

        if(allowedCount == 0) {
            return stack;
        }

        ItemStack offeredStack = stack.copyWithCount(allowedCount);
        ItemStack unaccepted = inventory.insertItem(slot, offeredStack, simulate);
        int acceptedCount = allowedCount - unaccepted.getCount();

        if(acceptedCount == 0) {
            return stack;
        }

        return acceptedCount == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(
                stack.getCount() - acceptedCount);
    }

    @Override
    public int getSlotLimit(int slot) {
        if(slot != 0) {
            return 0;
        }

        ItemStackHandler inventory = getInventory();
        return inventory == null ? 0 : inventory.getSlotLimit(slot);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot == 0 && FoundryItemMelting.isMeltable(level, stack);
    }
}
