package com.onewhohears.minigames.common.container;

import com.onewhohears.minigames.data.shops.GameShop;

import net.minecraft.world.SimpleContainer;

public class ShopContainer extends SimpleContainer {
	
	public ShopContainer(GameShop shop) {
		super(shop.getProductNum()*3);
	}
	
}
