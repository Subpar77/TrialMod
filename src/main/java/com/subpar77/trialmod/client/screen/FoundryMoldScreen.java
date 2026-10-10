package com.subpar77.trialmod.client.screen;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.menu.FoundryMoldMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class FoundryMoldScreen extends AbstractContainerScreen<FoundryMoldMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(TrialMod.MODID,
            "textures/container/mold.png");
    private static final ResourceLocation DISABLED_SLOT = ResourceLocation.withDefaultNamespace(
            "container/crafter/disabled_slot");

    private static final int FLUID_X = 10;
    private static final int FLUID_Y = 18;
    private static final int FLUID_WIDTH = 12;
    private static final int FLUID_HEIGHT = 49;
    private static final int RECIPE_X = 88;
    private static final int RECIPE_Y = 53;
    private static final int RECIPE_SIZE = 16;
    private static final int RECIPE_VISIBLE_COUNT = 3;
    private int firstVisibleRecipe = 0;
    private boolean dragginRecipeScrollbar = false;
    private static final int RECIPE_GAP = 2;
    private static final int SCROLL_X = 155;
    private static final int SCROLL_Y = 53;
    private static final int SCROLL_WIDTH = 11;
    private static final int SCROLL_HEIGHT = 16;
    private static final int SCROLL_THUMB_HEIGHT = 6;

    public FoundryMoldScreen(FoundryMoldMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        if(isHovering(FLUID_X, FLUID_Y, FLUID_WIDTH, FLUID_HEIGHT, mouseX, mouseY)) {
            Component tooltip = Component.translatable("tooltip.trial_mod.foundry_mold.fluid_amount",
                    menu.getFluidAmount(), menu.getFluidCapacity());

            guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
        }

        int visibleCount = getVisibleRecipeCount();

        for(int index = 0; index < visibleCount; index++) {
            int aX = RECIPE_X + index * (RECIPE_SIZE+ RECIPE_GAP);

            if(isHovering(aX, RECIPE_Y, RECIPE_SIZE, RECIPE_SIZE, mouseX, mouseY)) {
                ItemStack preview = menu.getRecipePreviews().get(firstVisibleRecipe + index);
                guiGraphics.renderTooltip(font, preview, mouseX, mouseY);;
                break;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        for(int row = 0; row < 3; row++) {
            for(int column = 0; column < 3; column++) {
                if(menu.isSlotDisabled(row * 3 + column)) {
                    guiGraphics.blitSprite(DISABLED_SLOT, leftPos + 28 + column * 18, topPos + 15 + row * 18,
                            18, 18);
                }
            }
        }

        int amount = menu.getFluidAmount();
        int capacity = menu.getFluidCapacity();
        IClientFluidTypeExtensions fluidExtensions = IClientFluidTypeExtensions.of(menu.getFluid());
        ResourceLocation flowingTexture = fluidExtensions.getFlowingTexture();

        if(amount > 0 && capacity > 0) {
            int filledHeight = Math.min(amount, capacity) * FLUID_HEIGHT / capacity;

            if(flowingTexture != null) {
                TextureAtlasSprite sprite = minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(flowingTexture);
                int x = leftPos + FLUID_X;
                int bottom = topPos + FLUID_Y + FLUID_HEIGHT;
                int filledTop = bottom - filledHeight;

                guiGraphics.enableScissor(x, filledTop, x + FLUID_WIDTH, bottom);

                for(int tileY = bottom - 16; tileY + 16 > filledTop; tileY -= 16) {
                    guiGraphics.blit(x, tileY, 0, 16, 16, sprite);
                }

                guiGraphics.disableScissor();
            }
        }

        int visibleCount = getVisibleRecipeCount();

        for(int index = 0; index < visibleCount; index++) {
            ItemStack preview = menu.getRecipePreviews().get(firstVisibleRecipe + index);

            int x = leftPos + RECIPE_X + index * (RECIPE_SIZE + RECIPE_GAP);
            int y = topPos + RECIPE_Y;
            int recipeIndex = firstVisibleRecipe + index;

            if(recipeIndex == menu.getSelectedRecipeIndex()) {
                guiGraphics.fill(x -1, y -1, x + RECIPE_SIZE + 1, y + RECIPE_SIZE + 1,
                        0x80FFFFFF);
            }

            guiGraphics.renderItem(preview, x, y);
            guiGraphics.renderItemDecorations(font, preview, x, y);
        }

        renderRecipeScrollBar(guiGraphics);
    }

    private void renderRecipeScrollBar(GuiGraphics guiGraphics) {
        int maxStart = Math.max(0, menu.getRecipePreviews().size() - RECIPE_VISIBLE_COUNT);
        int thumbOffset = 0;
        int thumbHeight = SCROLL_HEIGHT;
        int color = 0xFF888888;

        if(maxStart > 0) {
            thumbHeight = SCROLL_THUMB_HEIGHT;

            int travel = SCROLL_HEIGHT - thumbHeight;
            float progress = (float) firstVisibleRecipe / maxStart;

            thumbOffset = Math.round(progress * travel);
            color = 0xFFE0E0E0;
        }

        int x = leftPos + SCROLL_X;
        int y = topPos + SCROLL_Y + thumbOffset;

        guiGraphics.fill(x, y, x + SCROLL_WIDTH, y + thumbHeight, color);
    }

    private int getVisibleRecipeCount() {
        int total = menu.getRecipePreviews().size();
        int maxStart = Math.max(0, total - RECIPE_VISIBLE_COUNT);

        firstVisibleRecipe = Math.max(0, Math.min(firstVisibleRecipe, maxStart));

        return Math.min(RECIPE_VISIBLE_COUNT, total - firstVisibleRecipe);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        int areaWidth = RECIPE_VISIBLE_COUNT * (RECIPE_SIZE + RECIPE_GAP) - RECIPE_GAP;
        boolean overRecipes = isHovering(RECIPE_X, RECIPE_Y, areaWidth, RECIPE_SIZE, mouseX, mouseY);
        boolean overScrollbar = isHovering(SCROLL_X, SCROLL_Y, SCROLL_WIDTH, SCROLL_HEIGHT, mouseX, mouseY);

        if((overRecipes || overScrollbar) && menu.getRecipePreviews().size() > RECIPE_VISIBLE_COUNT
            && scrollY != 0) {

            if(scrollY > 0) {
                firstVisibleRecipe--;
            } else {
                firstVisibleRecipe++;
            }

            getVisibleRecipeCount();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void scrollRecipesTo(double mouseY) {
        int maxStart = Math.max(0, menu.getRecipePreviews().size() - RECIPE_VISIBLE_COUNT);

        if(maxStart == 0) {
            firstVisibleRecipe = 0;
            return;
        }

        int travel = SCROLL_HEIGHT - SCROLL_THUMB_HEIGHT;

        float thumbTop = (float) (mouseY - topPos - SCROLL_Y) - SCROLL_THUMB_HEIGHT / 2.0F;
        float progress = Math.max(0.0F, Math.min(thumbTop / travel, 1.0F));

        firstVisibleRecipe = Math.round(progress * maxStart);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(button == 0 && menu.getRecipePreviews().size() > RECIPE_VISIBLE_COUNT && isHovering(
                SCROLL_X, SCROLL_Y, SCROLL_WIDTH, SCROLL_HEIGHT, mouseX, mouseY)) {
            dragginRecipeScrollbar = true;
            scrollRecipesTo(mouseY);
            return true;
        }

        if(button == 0 && menu.getCarried().isEmpty() && !minecraft.player.isSpectator()) {
            int visibleCount = getVisibleRecipeCount();

            for(int index = 0; index < visibleCount; index++) {
                int x = RECIPE_X + index * (RECIPE_SIZE + RECIPE_GAP);

                if(isHovering(x, RECIPE_Y, RECIPE_SIZE, RECIPE_SIZE, mouseX, mouseY)) {
                    int recipeIndex = firstVisibleRecipe + index;
                    int buttonId = FoundryMoldMenu.RECIPE_BUTTON_OFFSET + recipeIndex;

                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);

                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {

        if(button == 0 && dragginRecipeScrollbar) {
            scrollRecipesTo(mouseY);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        if(button == 0 && dragginRecipeScrollbar) {
            dragginRecipeScrollbar = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if(slot != null) {
            if(slotId >= 0 && slotId <= 8) {
                if(type == ClickType.PICKUP && !slot.hasItem()) {
                    if(menu.getCarried().isEmpty() && !minecraft.player.isSpectator()) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, slotId);
                        return;
                    }
                }
            }
        }
        super.slotClicked(slot, slotId, mouseButton, type);
    }
}
