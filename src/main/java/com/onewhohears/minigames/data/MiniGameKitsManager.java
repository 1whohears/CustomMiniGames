package com.onewhohears.minigames.data;

import java.util.Map;

import com.google.gson.JsonElement;
import com.onewhohears.minigames.util.UtilParse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class MiniGameKitsManager extends SimpleJsonResourceReloadListener {
	
	public static String KIND = "minigamekits";
	
	private static MiniGameKitsManager instance;
	
	public static MiniGameKitsManager get() {
		if (instance == null) instance = new MiniGameKitsManager();
		return instance;
	}
	
	// TODO 3.6 kit system
	
	protected MiniGameKitsManager() {
		super(UtilParse.GSON, KIND);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
		
	}

}