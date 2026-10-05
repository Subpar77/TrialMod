package com.subpar77.trialmod.menu;

import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.block.entity.FoundryMoldBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public class FoundryMoldMenu extends AbstractContainerMenu {

    private final FoundryMoldBlockEntity mold;
    private final ContainerLevelAccess access;

    public FoundryMoldMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory,
                           FoundryMoldBlockEntity mold) {
        super(menuType, containerId);

        this.mold = mold;
        this.access = ContainerLevelAccess.create(mold.getLevel(), mold.getBlockPos());


        for(int row = 0; row < 3; row++) {
            for(int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(mold.getItemHandler(), row * 3 + column,
                        29 + column * 18, 16 + row * 18));
            }
        }

        for(int row = 0; row < 3; row++) {
            for(int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, 9 + row * 9 + column, 8 + column * 18, 84 + row * 18));
            }
        }

        for(int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        addDataSlots(mold.getSlotData());
    }

    public boolean isSlotDisabled(int slot) {return mold.isSlotDisabled(slot);}

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
                extraData.readBlockPos()));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if(id < 0 || id >= 9) {
            return false;
        }

        if(player.level().isClientSide || player.isSpectator() || !getCarried().isEmpty()) {
            return false;
        }

        return mold.setSlotDisabled(id, !isSlotDisabled(id));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.FOUNDRY_MOLD.get()) && !mold.isRemoved();
    }
}
