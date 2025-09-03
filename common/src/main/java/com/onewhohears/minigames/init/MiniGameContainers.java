package com.onewhohears.minigames.init;

import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.common.container.ShopMenu;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public class MiniGameContainers {

	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(
            MiniGamesMod.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<ShopMenu>> SHOP_MENU =
            CONTAINERS.register("shop_menu", () -> MenuRegistry.ofExtended(ShopMenu::new));

    public static void register() {
        CONTAINERS.register();
    }
	
}
