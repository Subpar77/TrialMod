package com.subpar77.trialmod.client.screen;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.menu.FoundryMoldMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class FoundryMoldScreen extends AbstractContainerScreen<FoundryMoldMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(TrialMod.MODID,
            "textures/container/mold.png");
    private static final ResourceLocation DISABLED_SLOT = ResourceLocation.withDefaultNamespace(
            "container/crafter/disabled_slot");

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
