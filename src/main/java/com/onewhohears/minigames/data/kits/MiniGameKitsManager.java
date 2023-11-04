package com.onewhohears.minigames.data.kits;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.util.UtilParse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class MiniGameKitsManager extends SimpleJsonResourceReloadListener {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String KIND = "minigamekits";
	private static MiniGameKitsManager instance;
	
	public static MiniGameKitsManager get() {
		if (instance == null) instance = new MiniGameKitsManager();
		return instance;
	}
	
	// TODO 3.6 kit system
	
	private Map<String, GameKit> kits = new HashMap<>();
	
	@Nullable
	public GameKit getKit(String kit_name) {
		return kits.get(kit_name);
	}
	
	protected MiniGameKitsManager() {
		super(UtilParse.GSON, KIND);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
		kits.clear();
		map.forEach((key, je) -> { try {
			JsonObject json = UtilParse.GSON.fromJson(je, JsonObject.class);
			GameKit kit = new GameKit(key, json);
			kits.put(kit.getId(), kit);
			LOGGER.debug("ADDING KIT: "+key.toString());
		} catch (Exception e) {
			LOGGER.error("PARSE KIT FAILED "+key.toString());
			e.printStackTrace();
		}});
	}
	
	public String[] getKitNames() {
		return kits.keySet().toArray(new String[kits.size()]);
	}

}
