package com.onewhohears.minigames.data.kits;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.onewhohears.minigames.data.JsonData;
import com.onewhohears.minigames.util.JsonToNBTUtil;
import com.onewhohears.minigames.util.UtilParse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class GameKit extends JsonData {
	
	public static class Builder extends JsonData.Builder<GameKit.Builder> {
		public static Builder create(String namespace, String id) {
			return new Builder(namespace, id);
		}
		public Builder addItem(String itemkey, int num, boolean unbreakable, JsonObject nbt) {
			if (num < 1) num = 1;
			else if (num > 64) num = 64;
			JsonObject json = new JsonObject();
			json.addProperty("item", itemkey);
			json.addProperty("num", num);
			if (unbreakable) {
				if (nbt == null) nbt = new JsonObject();
				nbt.addProperty("Unbreakable", true);
			}
			if (nbt != null) json.add("nbt", nbt);
			jsonData.get("kitItems").getAsJsonArray().add(json);
			return this;
		}
		public Builder addItem(String itemkey, boolean unbreakable) {
			return addItem(itemkey, 1, unbreakable, null);
		}
		public Builder addItem(String itemkey, boolean unbreakable, JsonObject nbt) {
			return addItem(itemkey, 1, unbreakable, nbt);
		}
		public Builder addItem(String itemkey, int num, JsonObject nbt) {
			return addItem(itemkey, num, false, nbt);
		}
		public Builder addItem(String itemkey, int num) {
			return addItem(itemkey, num, null);
		}
		public Builder addItem(String itemkey, JsonObject nbt) {
			return addItem(itemkey, 1, nbt);
		}		
		public Builder addItem(String itemkey) {
			return addItem(itemkey, 1, null);
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
	
	public void giveItems(ServerPlayer player) {
		for (KitItem item : items) player.addItem(item.getItem());
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
			CompoundTag nbt = null;
			if (json.has("nbt")) {
				JsonObject nbtJson = json.get("nbt").getAsJsonObject();
				nbt = JsonToNBTUtil.getTagFromJson(nbtJson);
			}
			return new KitItem(item, num, nbt);
		}
		private final Item item;
		private final int num;
		private final CompoundTag nbt;
		private KitItem(Item item, int num, CompoundTag nbt) {
			this.item = item;
			this.num = num;
			this.nbt = nbt;
		}
		public ItemStack getItem() {
			ItemStack stack = new ItemStack(item, num);
			if (nbt != null) stack.setTag(nbt);
			return stack;
		}
	}
	
}
