package com.onewhohears.minigames.data.shops;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.onewhohears.onewholibs.data.jsonpreset.JsonPresetReloadListener;
import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.util.UtilParse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class MiniGameShopsManager extends JsonPresetReloadListener<GameShop> {

	public static final String KIND = "minigameshops";
	private static MiniGameShopsManager instance;

	public static MiniGameShopsManager get() {
		if (instance == null) instance = new MiniGameShopsManager();
		return instance;
	}

	public MiniGameShopsManager() {
		super(KIND);
	}

	@Override
	public GameShop[] getNewArray(int i) {
		return new GameShop[i];
	}

	@Override
	protected void resetCache() {

	}

	@Override
	public void registerDefaultPresetTypes() {
		addPresetType(GameShop.GAMESHOP);
	}

	// TODO 3.4.1 shop system

}
