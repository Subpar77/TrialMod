package com.subpar77.trialmod.menu;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.block.entity.FoundryMoldBlockEntity;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import com.subpar77.trialmod.foundry.recipe.FoundryCastingMatch;
import com.subpar77.trialmod.foundry.recipe.FoundryCastingRecipes;
import com.subpar77.trialmod.network.FoundryMoldRecipesPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoundryMoldMenu extends AbstractContainerMenu {

    private final FoundryMoldBlockEntity mold;
    private final ContainerLevelAccess access;
    private final ContainerData fluidData;
    private List<FoundryCastingMatch> castingMatches = List.of();
    private int lastCastingRevision = -1;
    private int selectedRecipeIndex = -1;
    public static final int RECIPE_BUTTON_OFFSET = 9;
    private final Player menuPlayer;
    private boolean recipePreviewUpdatePending = true;
    private List<ItemStack> recipePreviews = List.of();

    private FoundryMoldMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory,
                            FoundryMoldBlockEntity mold, ContainerData fluidData) {
        super(menuType, containerId);

        checkContainerDataCount(fluidData, 3);
        this.fluidData = fluidData;
        this.mold = mold;
        this.access = ContainerLevelAccess.create(mold.getLevel(), mold.getBlockPos());
        this.menuPlayer = playerInventory.player;


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
        refreshCastingMatches();
    }

    private void sendRecipePreviews() {
        if(menuPlayer instanceof ServerPlayer serverPlayer) {
            List<ItemStack> previews = createRecipePreviews(serverPlayer.level());

            FoundryMoldRecipesPayload payload = new FoundryMoldRecipesPayload(containerId, previews,
                    selectedRecipeIndex);

            PacketDistributor.sendToPlayer(serverPlayer, payload);
        }
    }

    public void receiveRecipePreviews(List<ItemStack> previews, int selectedIndex) {
        recipePreviews = List.copyOf(previews);
        selectedRecipeIndex = selectedIndex;
    }

    public FoundryMoldMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory,
                           FoundryMoldBlockEntity mold) {
        this(menuType, containerId, playerInventory, mold, mold.getFluidData());
    }

    public boolean isSlotDisabled(int slot) {
        return mold.isSlotDisabled(slot);
    }

    public final int getFluidAmount() {
        return fluidData.get(0);
    }

    public final int getFluidCapacity() {
        return fluidData.get(1);
    }

    public List<ItemStack> getRecipePreviews() {return recipePreviews;}

    public int getSelectedRecipeIndex() {return selectedRecipeIndex;}

    public Fluid getFluid() {
        return BuiltInRegistries.FLUID.byId(fluidData.get(2));
    }

    private static FoundryMoldBlockEntity findMold(Inventory playerInventory, BlockPos pos) {
        BlockEntity entity = playerInventory.player.level().getBlockEntity(pos);

        if (entity instanceof FoundryMoldBlockEntity mold) {
            return mold;
        } else {
            throw new IllegalStateException("No foundry mold at " + pos);
        }
    }

    private void refreshCastingMatches() {
        Level level = mold.getLevel();

        if (level == null || level.isClientSide) {
            return;
        }

        FoundryCastingMatch previousSelection = getSelectedMatch();

        castingMatches = List.of();
        Optional<FoundryMaterial> storedMaterial = mold.getStoredMaterial();

        if (storedMaterial.isPresent()) {
            castingMatches = FoundryCastingRecipes.findCastingMatches(level, mold, storedMaterial.get());
        }

       selectedRecipeIndex = -1;

        if(previousSelection != null) {
            selectedRecipeIndex = findMatchIndex(previousSelection);
        }

        if(selectedRecipeIndex == -1 && castingMatches.size() == 1) {
            selectedRecipeIndex = 0;
        }

        lastCastingRevision = mold.getCastingRevision();
        recipePreviewUpdatePending = true;

        TrialMod.LOGGER.info("[Foundry] Menu casting matches: {} | Selected index: {}", castingMatches.size(),
                selectedRecipeIndex);

        FoundryCastingMatch selectedMatch = getSelectedMatch();

        if (selectedMatch != null) {
            TrialMod.LOGGER.info("[Foundry] Selected recipe: {} | Virtual ingredient: {}",
                    selectedMatch.recipe().id(), selectedMatch.virtualIngredient().getDescriptionId());
        }
    }

    private @Nullable FoundryCastingMatch getSelectedMatch() {

        if (selectedRecipeIndex < 0 || selectedRecipeIndex >= castingMatches.size()) {
            return null;
        }

        return castingMatches.get(selectedRecipeIndex);
    }

    public FoundryMoldMenu(int containerID, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(ModMenus.FOUNDRY_MOLD_MENU.get(), containerID, playerInventory, findMold(playerInventory,
                extraData.readBlockPos()), new SimpleContainerData(3));
    }

    private boolean selectRecipe(int index) {
        if (index < 0 || index >= castingMatches.size()) {
            return false;
        }

        if (selectedRecipeIndex == index) {
            return false;
        }

        selectedRecipeIndex = index;
        recipePreviewUpdatePending = true;

        return true;
    }

    private int findMatchIndex(FoundryCastingMatch target) {
        for (int index = 0; index < castingMatches.size(); index++) {
            FoundryCastingMatch candidate = castingMatches.get(index);

            if (candidate.recipe().id().equals(target.recipe().id()) &&
                    candidate.virtualIngredient() == target.virtualIngredient()) {
                return index;
            }
        }
        return -1;
    }

    private List<ItemStack> createRecipePreviews(Level level) {
        List<ItemStack> previews = new ArrayList<>();

        for(FoundryCastingMatch match : castingMatches) {
            CraftingInput input = FoundryCastingRecipes.createInput(mold, new ItemStack(match.virtualIngredient()));
            ItemStack preview = match.recipe().value().assemble(input, level.registryAccess());
            previews.add(preview.copy());
        }
        return previews;
    }


    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0) {
            return false;
        }

        if (player.level().isClientSide || player.isSpectator() || !getCarried().isEmpty()) {
            return false;
        }

        if(id < RECIPE_BUTTON_OFFSET) {
            return mold.setSlotDisabled(id, !isSlotDisabled(id));
        }

        if(lastCastingRevision != mold.getCastingRevision()) {
            refreshCastingMatches();
        }

        return selectRecipe(id - RECIPE_BUTTON_OFFSET);
    }

    @Override
    public void broadcastChanges() {
        Level level = mold.getLevel();

        if (level != null && !level.isClientSide && lastCastingRevision != mold.getCastingRevision()) {
            refreshCastingMatches();
        }

        super.broadcastChanges();

        if(level != null && !level.isClientSide && recipePreviewUpdatePending) {
            sendRecipePreviews();
            recipePreviewUpdatePending = false;
        }
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
        mold.markCastingInputsChanged();
        return originalStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.FOUNDRY_MOLD.get()) && !mold.isRemoved();
    }
}
