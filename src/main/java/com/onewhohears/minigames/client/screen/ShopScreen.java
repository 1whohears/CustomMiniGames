package com.onewhohears.minigames.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.onewhohears.minigames.common.container.ShopMenu;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ShopScreen extends AbstractContainerScreen<ShopMenu> {
	
	public static final ResourceLocation SHOP_BG = new ResourceLocation("textures/gui/container/generic_54.png");
	private final int rows;
	
	public ShopScreen(ShopMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
		passEvents = false;
		rows = menu.getRows();
		imageHeight = 114 + rows * 18;
		inventoryLabelY = imageHeight - 94;
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, partialTick);
		renderTooltip(poseStack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, SHOP_BG);
		int i = (width - imageWidth) / 2;
		int j = (height - imageHeight) / 2;
		blit(poseStack, i, j, 0, 0, imageWidth, rows * 18 + 17);
		blit(poseStack, i, j + rows * 18 + 17, 0, 126, imageWidth, 96);
	}

}
