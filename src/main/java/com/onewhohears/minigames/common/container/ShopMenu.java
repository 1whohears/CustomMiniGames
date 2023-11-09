package com.onewhohears.minigames.common.container;

import com.onewhohears.minigames.data.shops.GameShop;
import com.onewhohears.minigames.init.ModContainers;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

public class ShopMenu extends AbstractContainerMenu {
	
	public static void openScreen(ServerPlayer player, GameShop shop) {
		NetworkHooks.openScreen(player, 
			new SimpleMenuProvider((windowId, playerInv, p) -> 
				new ShopMenu(windowId, playerInv, shop), 
				Component.translatable(shop.getDisplayName())), 
			(buff) -> buff.writeUtf(shop.getId()));
	}
	
	public final Container shopInv;
	public final Inventory playerInv;
	public final GameShop shop;
	
	public ShopMenu(int windowId, Inventory playerInv, GameShop shop) {
		super(ModContainers.SHOP_MENU.get(), windowId);
		this.shopInv = new ShopContainer(shop);
		this.playerInv = playerInv;
		this.shop = shop;
		// TODO 3.4.3 add slots to shop menu
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return !player.isDeadOrDying();
	}

}
