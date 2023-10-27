package com.onewhohears.minigames.data.kits;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.onewhohears.minigames.data.JsonData;
import com.onewhohears.minigames.util.UtilParse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class GameKit extends JsonData {
	
	public static class Builder extends JsonData.Builder<GameKit.Builder> {
		public static Builder create(String namespace, String id) {
			return new Builder(namespace, id);
		}
		public Builder addItem(String itemkey, int num) {
			if (num < 1) num = 1;
			else if (num > 64) num = 64;
			JsonObject json = new JsonObject();
			json.addProperty("item", itemkey);
			json.addProperty("num", num);
			jsonData.get("kitItems").getAsJsonArray().add(json);
			return this;
		}
		protected Builder(String namespace, String id) {
			super(namespace, id, (key, json) -> new GameKit(key, json));
			jsonData.add("kitItems", new JsonArray());
		}
	}
	
	private List<KitItem> items = new ArrayList<>();
	
	public GameKit(ResourceLocation key, JsonObject json) {
		super(key, json);
		JsonArray list = json.get("kitItems").getAsJsonArray();
		for (int i = 0; i < list.size(); ++i) {
			JsonObject jo = list.get(i).getAsJsonObject();
			KitItem kitItem = KitItem.create(jo);
			if (kitItem != null) items.add(kitItem);
		}
	}
	
	public static class KitItem {
		@Nullable
		public static KitItem create(JsonObject json) {
			String itemKey = UtilParse.getStringSafe(json, "item", "");
			if (itemKey.isEmpty()) return null;
			ResourceLocation rl = new ResourceLocation(itemKey);
			if (!ForgeRegistries.ITEMS.containsKey(rl)) return null;
			Item item = ForgeRegistries.ITEMS.getDelegate(rl).get().get();
			int num = UtilParse.getIntSafe(json, "num", 1);
			return new KitItem(item, num);
		}
		private final Item item;
		private final int num;
		private KitItem(Item item, int num) {
			this.item = item;
			this.num = num;
		}
		public ItemStack getItem() {
			return new ItemStack(item, num);
		}
	}
	
}
