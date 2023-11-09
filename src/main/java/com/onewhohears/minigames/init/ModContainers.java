package com.onewhohears.minigames.init;

import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.common.container.ShopMenu;
import com.onewhohears.minigames.data.shops.GameShop;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModContainers {
	
	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MiniGamesMod.MODID);
	
	public static void register(IEventBus eventBus) {
		CONTAINERS.register(eventBus);
    }
	
	public static final RegistryObject<MenuType<ShopMenu>> SHOP_MENU = 
			register("shop_menu", (windowId, playerInv, data) -> {
				String shop_name = data.readUtf();
				GameShop shop = MiniGameShopsManager.get().getShop(shop_name);
				return new ShopMenu(windowId, playerInv, shop);
			});
	
	private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(String id, IContainerFactory<T> factory){
        return CONTAINERS.register(id, () -> new MenuType<>(factory));
    }
	
}
