package com.onewhohears.minigames.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.onewhohears.minigames.common.container.ShopMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class ShopScreen extends AbstractContainerScreen<ShopMenu> {
	
	public static final ResourceLocation SHOP_BG = new ResourceLocation("textures/gui/container/generic_54.png");
	private final int rows;
	
	public ShopScreen(ShopMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
		rows = menu.getRows();
		imageHeight = 114 + rows * 18;
		inventoryLabelY = imageHeight - 94;
	}
	
	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = (width - imageWidth) / 2;
		int j = (height - imageHeight) / 2;
        guiGraphics.blit(SHOP_BG, i, j, 0, 0, imageWidth, rows * 18 + 17);
        guiGraphics.blit(SHOP_BG, i, j + rows * 18 + 17, 0, 126, imageWidth, 96);
	}

}
