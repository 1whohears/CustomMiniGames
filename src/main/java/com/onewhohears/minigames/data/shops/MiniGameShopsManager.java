package com.onewhohears.minigames.data.shops;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

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

public class MiniGameShopsManager extends SimpleJsonResourceReloadListener {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String KIND = "minigameshops";
	private static MiniGameShopsManager instance;
	
	public static MiniGameShopsManager get() {
		if (instance == null) instance = new MiniGameShopsManager();
		return instance;
	}
	
	// TODO 3.4.1 shop system
	
	private Map<String, GameShop> shops = new HashMap<>();
	
	@Nullable
	public GameShop getShop(String shop_name) {
		return shops.get(shop_name);
	}
	
	public String[] getShopNames() {
		return shops.keySet().toArray(new String[shops.size()]);
	}
	
	protected MiniGameShopsManager() {
		super(UtilParse.GSON, KIND);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
		map.forEach((key, je) -> { try {
			JsonObject json = UtilParse.GSON.fromJson(je, JsonObject.class);
			GameShop shop = new GameShop(key, json);
			shops.put(shop.getId(), shop);
			LOGGER.debug("ADDING SHOP: "+key.toString());
		} catch (Exception e) {
			LOGGER.error("PARSE SHOP FAILED "+key.toString());
			e.printStackTrace();
		}});
	}
	
	public void writeToBuffer(FriendlyByteBuf buffer) {
		buffer.writeInt(shops.size());
		shops.forEach((id, shop) -> {
			buffer.writeUtf(shop.getKey().toString());
			buffer.writeUtf(shop.getJsonData().toString());
		});
	}
	
	public void readBuffer(FriendlyByteBuf buffer) {
		LOGGER.debug("READING SHOP DATA FROM SERVER");
		int length = buffer.readInt();
		for (int i = 0; i < length; ++i) {
			String key_string = buffer.readUtf();
			String json_string = buffer.readUtf();
			ResourceLocation key = new ResourceLocation(key_string);
			JsonObject json = UtilParse.GSON.fromJson(json_string, JsonObject.class);
			GameShop shop = new GameShop(key, json);
			shops.put(shop.getId(), shop);
			LOGGER.debug("READ SHOP: "+key.toString());
		}
	}

}
