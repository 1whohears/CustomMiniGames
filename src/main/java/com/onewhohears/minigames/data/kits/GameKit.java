package com.onewhohears.minigames.data.kits;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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
		public Builder addItem(String itemkey, int num, boolean unbreakable, JsonObject nbt, 
				boolean keep, boolean refill, boolean ignoreNbt) {
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
			if (keep) json.addProperty("keep", keep);
			if (refill) json.addProperty("refill", refill);
			if (ignoreNbt) json.addProperty("ignoreNbt", ignoreNbt);
			jsonData.get("kitItems").getAsJsonArray().add(json);
			return this;
		}
		public Builder addItem(String itemkey, int num, boolean unbreakable, JsonObject nbt) {
			return addItem(itemkey, num, unbreakable, nbt, false, false, false);
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
	public void giveItemsClearAll(ServerPlayer player) {
		player.getInventory().clearContent();
		giveItems(player);
	}
	public void giveItemsClearOther(ServerPlayer player) {
		giveItemsRefill(player, true);
	}
	public void giveItemsRefill(ServerPlayer player) {
		giveItemsRefill(player, false);
	}
	public void giveItemsRefill(ServerPlayer player, boolean clearOther) {
		for (KitItem item : items) {
			if (item.canRefill()) 
				player.addItem(item.getItem());
			else if (item.canKeep() && !player.getInventory().hasAnyMatching(item.sameChecker())) 
				player.addItem(item.getItem());
		}
		if (clearOther) for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
			 ItemStack stack = player.getInventory().getItem(i);
			 if (stack.isEmpty()) continue;
			 KitItem kititem = getKitItemData(stack);
			 if (kititem == null || !kititem.canKeep()) 
				 player.getInventory().removeItem(stack);
		}
	}
	public boolean hasKitItem(ItemStack stack) {
		if (stack.isEmpty()) return false;
		for (KitItem item : items) if (item.isSameItem(stack)) return true;
		return false;
	}
	@Nullable
	public KitItem getKitItemData(ItemStack stack) {
		if (stack.isEmpty()) return null;
		for (KitItem item : items) if (item.isSameItem(stack)) return item;
		return null;
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
			boolean keep = UtilParse.getBooleanSafe(json, "keep", false);
			boolean refill = UtilParse.getBooleanSafe(json, "refill", false);
			boolean ignoreNbt = UtilParse.getBooleanSafe(json, "ignoreNbt", false);
			return new KitItem(item, num, nbt, keep, refill, ignoreNbt);
		}
		private final Item item;
		private final int num;
		private final CompoundTag nbt;
		private final boolean keep, refill, ignoreNbt;
		private KitItem(Item item, int num, CompoundTag nbt, boolean keep, boolean refill, boolean ignoreNbt) {
			this.item = item;
			this.num = num;
			this.nbt = nbt;
			this.keep = keep;
			this.refill = refill;
			this.ignoreNbt = ignoreNbt;
		}
		public ItemStack getItem() {
			ItemStack stack = new ItemStack(item, num);
			if (nbt != null) stack.setTag(nbt);
			return stack;
		}
		public boolean canKeep() {
			return keep;
		}
		public boolean canRefill() {
			return refill;
		}
		public boolean sameCheckIgnoreNbt() {
			return ignoreNbt;
		}
		public boolean isSameItem(ItemStack stack) {
			if (stack.isEmpty()) return false;
			if (sameCheckIgnoreNbt()) return ItemStack.isSameIgnoreDurability(getItem(), stack);
			return ItemStack.isSameItemSameTags(getItem(), stack);
		}
		public Predicate<ItemStack> sameChecker() {
			return (stack) -> isSameItem(stack);
		}
	}
	
}
