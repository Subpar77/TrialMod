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

    public FoundryMoldScreen(FoundryMoldMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
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
