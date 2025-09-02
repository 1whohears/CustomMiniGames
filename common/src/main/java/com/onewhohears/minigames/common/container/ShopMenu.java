package com.onewhohears.minigames.common.container;

import com.onewhohears.minigames.data.shops.GameShop;
import com.onewhohears.minigames.init.MiniGameContainers;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
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
	
	public static final int SHOP_SLOTS_PER_ROW = 8;
	public final ShopContainer shopInv;
	public final Inventory playerInv;
	public final GameShop shop;
	
	public ShopMenu(int windowId, Inventory playerInv, GameShop shop) {
		super(MiniGameContainers.SHOP_MENU.get(), windowId);
		this.shopInv = new ShopContainer(shop);
		this.playerInv = playerInv;
		this.shop = shop;
		int rows = getRows();
		int slotNum = shopInv.getContainerSize();
		int c = 0;
		for(int j = 0; j < rows; ++j) for(int k = 0; k < SHOP_SLOTS_PER_ROW; ++k) {
			if (c >= slotNum) break;
			if (c%2==0) addSlot(new CostSlot(shopInv, c, 8+k*18, 18+j*18));
			else addSlot(new ProductSlot(shopInv, c, 8+k*18, 18+j*18, shop.getProductByMenuSlot(c)));
			++c;
		}
		int i = (rows - 4) * 18;
		for(int l = 0; l < 3; ++l) for(int j1 = 0; j1 < 9; ++j1) 
			addSlot(new Slot(playerInv, j1+l*9+9, 8+j1*18, 103+l*18+i));
		for(int i1 = 0; i1 < 9; ++i1) 
			addSlot(new Slot(playerInv, i1, 8+i1*18, 161+i));
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return !player.isDeadOrDying();
	}
	
	public int getRows() {
		return (int)Math.ceil(shop.getProductNum()*0.25);
	}

}
