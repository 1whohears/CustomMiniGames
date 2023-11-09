package com.onewhohears.minigames;

import com.onewhohears.minigames.client.screen.ShopScreen;
import com.onewhohears.minigames.common.network.PacketHandler;
import com.onewhohears.minigames.data.kits.MiniGameKitsGenerator;
import com.onewhohears.minigames.data.shops.MiniGameShopsGenerator;
import com.onewhohears.minigames.init.ModContainers;
import com.onewhohears.minigames.minigame.MiniGameManager;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MiniGamesMod.MODID)
public class MiniGamesMod {
	
	public static final String MODID = "minigames";
	
	public MiniGamesMod() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		ModContainers.register(eventBus);
		
		eventBus.addListener(this::commonSetup);
    	eventBus.addListener(this::clientSetup);
    	eventBus.addListener(this::onGatherData);
	}
	
	private void commonSetup(FMLCommonSetupEvent event) {
		PacketHandler.register();
		MiniGameManager.registerGames();
	}
	
	private void clientSetup(FMLClientSetupEvent event) {
		MenuScreens.register(ModContainers.SHOP_MENU.get(), ShopScreen::new);
    }
	
	private void onGatherData(GatherDataEvent event) {
    	DataGenerator generator = event.getGenerator();
    	if (event.includeServer()) {
    		MiniGameKitsGenerator.register(generator);
    		MiniGameShopsGenerator.register(generator);
    	}
    }
	
}
