package com.onewhohears.minigames.data;

import com.google.gson.JsonObject;
import com.onewhohears.minigames.util.UtilParse;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public abstract class JsonData {
	
	private final ResourceLocation key;
	private final JsonObject jsonData;
	private final String id;
	private final String displayName;
	
	protected JsonData(ResourceLocation key, JsonObject json) {
		this.key = key;
		this.jsonData = json;
		this.id = UtilParse.getStringSafe(json, "id", "");
		this.displayName = UtilParse.getStringSafe(json, "displayName", "name.minigames."+id);
	}
	
	public ResourceLocation getKey() {
		return key;
	}
	
	public JsonObject getJsonData() {
		return jsonData.deepCopy();
	}
	
	public String getId() {
		return id;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getNameSpace() {
		return getKey().getNamespace();
	}
	
	public MutableComponent getDisplayNameComponent() {
		String dn = getDisplayName();
		if (dn.startsWith("preset.")) return Component.translatable(dn);
		return Component.literal(dn);
	}
	
}
