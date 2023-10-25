package com.onewhohears.minigames;

import com.onewhohears.minigames.data.kits.MiniGameKitsGenerator;
import com.onewhohears.minigames.data.shops.MiniGameShopsGenerator;
import com.onewhohears.minigames.minigame.MiniGameManager;

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
		eventBus.addListener(this::commonSetup);
    	eventBus.addListener(this::clientSetup);
    	eventBus.addListener(this::onGatherData);
	}
	
	private void commonSetup(FMLCommonSetupEvent event) {
		MiniGameManager.registerGames();
	}
	
	private void clientSetup(FMLClientSetupEvent event) {
    	
    }
	
	private void onGatherData(GatherDataEvent event) {
    	DataGenerator generator = event.getGenerator();
    	if (event.includeServer()) {
    		MiniGameKitsGenerator.register(generator);
    		MiniGameShopsGenerator.register(generator);
    	}
    }
	
}
