package com.onewhohears.minigames.data;

import java.util.Map;

import com.google.gson.JsonElement;
import com.onewhohears.minigames.util.UtilParse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class MiniGameShopsManager extends SimpleJsonResourceReloadListener {
	
	public static String KIND = "minigameshops";
	
	private static MiniGameShopsManager instance;
	
	public static MiniGameShopsManager get() {
		if (instance == null) instance = new MiniGameShopsManager();
		return instance;
	}
	
	// TODO 3.4 shop system
	
	protected MiniGameShopsManager() {
		super(UtilParse.GSON, KIND);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
		
	}

}
