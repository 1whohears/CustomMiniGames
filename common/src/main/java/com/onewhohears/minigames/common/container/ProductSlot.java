package com.onewhohears.minigames.common.container;

import com.onewhohears.minigames.data.shops.GameShop;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ProductSlot extends Slot {
	
	public final GameShop.Product product;
	
	public ProductSlot(ShopContainer pContainer, int pSlot, int pX, int pY, GameShop.Product product) {
		super(pContainer, pSlot, pX, pY);
		this.product = product;
	}
	
	@Override
	public void onTake(Player player, ItemStack stack) {
		product.handlePurchase(player, true);
		container.setItem(getContainerSlot(), product.getProductItem());
	}
	
	@Override
	public boolean mayPickup(Player player) {
		return product.canBuy(player);
	}
	
	@Override
	public boolean mayPlace(ItemStack pStack) {
		return false;
	}

}
