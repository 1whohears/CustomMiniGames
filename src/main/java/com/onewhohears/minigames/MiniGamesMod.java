package com.onewhohears.minigames;

import com.onewhohears.minigames.client.screen.ShopScreen;
import com.onewhohears.minigames.common.network.PacketHandler;
import com.onewhohears.minigames.data.kits.MiniGameKitsGenerator;
import com.onewhohears.minigames.data.shops.MiniGameShopsGenerator;
import com.onewhohears.minigames.init.CMGTags;
import com.onewhohears.minigames.init.MiniGameContainers;
import com.onewhohears.minigames.init.MiniGameEntities;
import com.onewhohears.minigames.init.MiniGameItems;
import com.onewhohears.minigames.minigame.MiniGameManager;

import com.onewhohears.minigames.minigame.param.MiniGameParamTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MiniGamesMod.MODID)
public class MiniGamesMod {
	
	public static final String MODID = "minigames";

	public static boolean XAERO_MINIMAP_LOADED = false;
	public static boolean XAERO_MINIMAP_FAIRPLAY_LOADED = false;

	public static boolean isXaeroMinimapLoaded() {
		return XAERO_MINIMAP_LOADED || XAERO_MINIMAP_FAIRPLAY_LOADED;
	}

	public MiniGamesMod() {
		XAERO_MINIMAP_LOADED = ModList.get().isLoaded("xaerominimap");
		XAERO_MINIMAP_FAIRPLAY_LOADED = ModList.get().isLoaded("xaerominimapfair");

		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		MiniGameContainers.register(eventBus);
		MiniGameEntities.register(eventBus);
		MiniGameItems.register(eventBus);
		CMGTags.init();
		
		eventBus.addListener(this::commonSetup);
    	eventBus.addListener(this::clientSetup);
    	eventBus.addListener(this::onGatherData);
	}
	
	private void commonSetup(FMLCommonSetupEvent event) {
		PacketHandler.register();
		MiniGameParamTypes.registerGameParamTypes();
		MiniGameManager.registerGames();
		MiniGameManager.registerItemEvents();
		MiniGameManager.registerPOIGens();
		MiniGameManager.registerGameAgentGens();
	}
	
	private void clientSetup(FMLClientSetupEvent event) {
		MenuScreens.register(MiniGameContainers.SHOP_MENU.get(), ShopScreen::new);
    }
	
	private void onGatherData(GatherDataEvent event) {
    	DataGenerator generator = event.getGenerator();
    	if (event.includeServer()) {
    		MiniGameKitsGenerator.register(generator);
    		MiniGameShopsGenerator.register(generator);
    	}
    }
	
}
