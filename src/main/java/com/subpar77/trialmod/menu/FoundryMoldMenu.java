package com.subpar77.trialmod.menu;

import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.block.entity.FoundryMoldBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public class FoundryMoldMenu extends AbstractContainerMenu {

    private final FoundryMoldBlockEntity mold;
    private final ContainerLevelAccess access;
    private final ContainerData fluidData;

    private FoundryMoldMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory,
                           FoundryMoldBlockEntity mold, ContainerData fluidData) {
        super(menuType, containerId);

        checkContainerDataCount(fluidData, 3);
        this.fluidData = fluidData;
        this.mold = mold;
        this.access = ContainerLevelAccess.create(mold.getLevel(), mold.getBlockPos());


        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(mold.getItemHandler(), row * 3 + column,
                        29 + column * 18, 16 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, 9 + row * 9 + column, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        addDataSlots(mold.getSlotData());
        addDataSlots(this.fluidData);
    }

    public FoundryMoldMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory,
                           FoundryMoldBlockEntity mold) {
        this(menuType, containerId, playerInventory, mold, mold.getFluidData());
    }

    public boolean isSlotDisabled(int slot) {
        return mold.isSlotDisabled(slot);
    }

    public final int getFluidAmount() {return fluidData.get(0);}

    public final int getFluidCapacity() {return fluidData.get(1);}

    public Fluid getFluid() { return BuiltInRegistries.FLUID.byId(fluidData.get(2));}

    private static FoundryMoldBlockEntity findMold(Inventory playerInventory, BlockPos pos) {
        BlockEntity entity = playerInventory.player.level().getBlockEntity(pos);

        if (entity instanceof FoundryMoldBlockEntity mold) {
            return mold;
        } else {
            throw new IllegalStateException("No foundry mold at " + pos);
        }
    }

    public FoundryMoldMenu(int containerID, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(ModMenus.FOUNDRY_MOLD_MENU.get(), containerID, playerInventory, findMold(playerInventory,
                extraData.readBlockPos()), new SimpleContainerData(3));
    }


    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= 9) {
            return false;
        }

        if (player.level().isClientSide || player.isSpectator() || !getCarried().isEmpty()) {
            return false;
        }

        return mold.setSlotDisabled(id, !isSlotDisabled(id));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index > slots.size() - 1) {
            return ItemStack.EMPTY;
        }

        Slot sourceSlot = slots.get(index);

        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack originalStack = sourceStack.copy();

        if (index < 9) {
            if (!moveItemStackTo(sourceStack, 9, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(sourceStack, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, sourceStack);
        mold.setChanged();
        return originalStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.FOUNDRY_MOLD.get()) && !mold.isRemoved();
    }
}
