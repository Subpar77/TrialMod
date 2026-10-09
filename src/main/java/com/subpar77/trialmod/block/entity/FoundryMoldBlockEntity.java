package com.subpar77.trialmod.block.entity;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import com.subpar77.trialmod.foundry.recipe.FoundryCastingMatch;
import com.subpar77.trialmod.foundry.recipe.FoundryCastingRecipes;
import com.subpar77.trialmod.menu.FoundryMoldMenu;
import com.subpar77.trialmod.menu.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class FoundryMoldBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_ENABLED = 0;
    public static final int SLOT_DISABLED = 1;
    private final int[] slotStates = new int[9];
    private int castingRevision = 0;

    public FoundryMoldBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FOUNDRY_MOLD_ENTITY.get(), pos, blockState);
    }

    public boolean isSlotDisabled(int slot) {
        if (slot < 0 || slot >= slotStates.length) {
            return false;
        }

        if (slotStates[slot] == SLOT_DISABLED) {
            return true;
        }

        return false;
    }

    public boolean setSlotDisabled(int slot, boolean disabled) {
        if (slot < 0 || slot >= slotStates.length) {
            return false;
        }

        if (disabled && !inputInventory.getStackInSlot(slot).isEmpty()) {
            return false;
        }

        int targetState = SLOT_ENABLED;
        if (disabled) {
            targetState = SLOT_DISABLED;
        } else {
            targetState = SLOT_ENABLED;
        }

        if (slotStates[slot] == targetState) {
            return false;
        }

        slotStates[slot] = targetState;
        markCastingInputsChanged();
        return true;

    }

    private final ContainerData fluidData = new ContainerData() {
        @Override
        public int get(int index) {
            if (index < 0 || index > 2) {
                return 0;
            }

            if (index == 0) {
                return getFluidAmount();
            }

            if (index == 1) {
                return getFluidCapacity();
            }

            if (index == 2) {
                return getFluidId();
            }

            return 0;
        }

        @Override
        public void set(int index, int value) {
//      Server values are read directly from the reservoir.
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public int getCastingRevision() {return castingRevision;}

    public Optional<FoundryMaterial> getStoredMaterial() {
        return FoundryMaterial.fromFluid(reservoir.getFluid().getFluid());
    }

    public final ContainerData getFluidData() {
        return fluidData;
    }

    public int getFluidId() {
        return BuiltInRegistries.FLUID.getId(reservoir.getFluid().getFluid());
    }

    private final ContainerData slotData = new ContainerData() {
        @Override
        public int get(int index) {
            if (isSlotDisabled(index)) {
                return SLOT_DISABLED;
            }
            return SLOT_ENABLED;
        }

        @Override
        public void set(int index, int value) {
            setSlotDisabled(index, value == SLOT_DISABLED);
        }

        @Override
        public int getCount() {
            return slotStates.length;
        }
    };

    public final ContainerData getSlotData() {
        return slotData;
    }

    private final ItemStackHandler inputInventory = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            FoundryMoldBlockEntity.this.markCastingInputsChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (isSlotDisabled(slot)) {
                return false;
            }

            return true;
        }
    };

    public final int getFluidAmount() {
        return reservoir.getFluidAmount();
    }

    public final int getFluidCapacity() {
        return reservoir.getCapacity();
    }

    public void markCastingInputsChanged() {
        castingRevision++;
        setChanged();
    }

    private final FluidTank reservoir = new FluidTank(4000, stack ->
            FoundryMaterial.fromFluid(stack.getFluid()).isPresent()) {
        @Override
        protected void onContentsChanged() {
            FoundryMoldBlockEntity.this.markCastingInputsChanged();
        }
    };

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Reservoir", reservoir.writeToNBT(registries, new CompoundTag()));
        tag.put("InputInventory", inputInventory.serializeNBT(registries));
        tag.putIntArray("SlotStates", slotStates);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        reservoir.readFromNBT(registries, tag.getCompound("Reservoir"));
        inputInventory.deserializeNBT(registries, tag.getCompound("InputInventory"));

        int[] savedSlotData = tag.getIntArray("SlotStates");
        for (int slot = 0; slot < slotStates.length; slot++) {
            slotStates[slot] = SLOT_ENABLED;

            if (slot < savedSlotData.length && savedSlotData[slot] == SLOT_DISABLED) {
                if (inputInventory.getStackInSlot(slot).isEmpty()) {
                    slotStates[slot] = SLOT_DISABLED;
                }
            }
        }
    }

    public IFluidHandler getFluidHandler() {
        return reservoir;
    }

    public IItemHandler getItemHandler() {
        return inputInventory;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.trial_mod.foundry_mold");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {

        return new FoundryMoldMenu(ModMenus.FOUNDRY_MOLD_MENU.get(), containerId, inventory, this);
    }
}