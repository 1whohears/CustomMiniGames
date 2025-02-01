package com.onewhohears.minigames.data.kits;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.onewhohears.minigames.data.MiniGamePresetType;
import com.onewhohears.onewholibs.util.JsonToNBTUtil;
import com.onewhohears.onewholibs.util.UtilParse;

import com.onewhohears.onewholibs.data.jsonpreset.JsonPresetInstance;
import com.onewhohears.onewholibs.data.jsonpreset.JsonPresetStats;
import com.onewhohears.onewholibs.data.jsonpreset.JsonPresetType;
import com.onewhohears.onewholibs.data.jsonpreset.PresetBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class GameKit extends JsonPresetStats {

	public static final MiniGamePresetType GAMEKIT = new MiniGamePresetType("gamekit", GameKit::new);

	private final List<KitItem> items = new ArrayList<>();
	public GameKit(ResourceLocation key, JsonObject json) {
		super(key, json);
		JsonArray list = json.get("kitItems").getAsJsonArray();
		for (int i = 0; i < list.size(); ++i) {
			JsonObject jo = list.get(i).getAsJsonObject();
			KitItem kitItem = KitItem.create(jo);
			if (kitItem != null) items.add(kitItem);
		}
	}

	@Override
	public JsonPresetType getType() {
		return GAMEKIT;
	}

	@Override
	public @org.jetbrains.annotations.Nullable JsonPresetInstance<?> createPresetInstance() {
		return null;
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
		Inventory inv = player.getInventory();
		if (clearOther) for (int i = 0; i < inv.getContainerSize(); ++i) {
			 ItemStack stack = inv.getItem(i);
			 if (stack.isEmpty()) continue;
			 KitItem kititem = getKitItemData(stack);
			 if (kititem == null || !kititem.canKeep()) 
				 inv.removeItem(stack);
		}
		for (KitItem item : items) {
			if (item.canRefill() || !(item.canKeep() && inv.hasAnyMatching(item.sameChecker())))
				player.addItem(item.getItem());
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
			if (!verifySafe(json)) return null;
			return new KitItem(json);
		}
		public static boolean verifySafe(JsonObject json) {
			String itemKey = UtilParse.getStringSafe(json, "item", "");
			if (itemKey.isEmpty()) return false;
			ResourceLocation rl = new ResourceLocation(itemKey);
			if (!ForgeRegistries.ITEMS.containsKey(rl)) return false;
			return true;
		}
		private final Item item;
		private final int num;
		private final CompoundTag nbt;
		private final boolean keep, refill, ignoreNbt;
		protected KitItem(Item item, int num, CompoundTag nbt, boolean keep, boolean refill, boolean ignoreNbt) {
			this.item = item;
			this.num = num;
			this.nbt = nbt;
			this.keep = keep;
			this.refill = refill;
			this.ignoreNbt = ignoreNbt;
		}
		protected KitItem(JsonObject json) {
			String itemKey = UtilParse.getStringSafe(json, "item", "");
			ResourceLocation rl = new ResourceLocation(itemKey);
			item = ForgeRegistries.ITEMS.getDelegate(rl).get().get();
			num = UtilParse.getIntSafe(json, "num", 1);
			if (json.has("nbt")) {
				JsonObject nbtJson = json.get("nbt").getAsJsonObject();
				nbt = JsonToNBTUtil.getTagFromJson(nbtJson);
			} else nbt = null;
			keep = UtilParse.getBooleanSafe(json, "keep", false);
			refill = UtilParse.getBooleanSafe(json, "refill", false);
			ignoreNbt = UtilParse.getBooleanSafe(json, "ignoreNbt", false);
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
			return this::isSameItem;
		}
	}
	
	public static class Builder extends PresetBuilder<GameKit.Builder> {
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
			getData().get("kitItems").getAsJsonArray().add(json);
			return this;
		}
		public Builder addItemKeep(String itemkey, int num, boolean unbreakable, JsonObject nbt) {
			return addItem(itemkey, num, unbreakable, nbt, true, false, false);
		}
		public Builder addItemRefill(String itemkey, int num, boolean unbreakable, JsonObject nbt) {
			return addItem(itemkey, num, unbreakable, nbt, true, true, false);
		}
		public Builder addItem(String itemkey, int num, boolean unbreakable, JsonObject nbt) {
			return addItem(itemkey, num, unbreakable, nbt, false, false, false);
		}
		public Builder addItemKeep(String itemkey, boolean unbreakable) {
			return addItemKeep(itemkey, 1, unbreakable, null);
		}
		public Builder addItem(String itemkey, boolean unbreakable) {
			return addItem(itemkey, 1, unbreakable, null);
		}
		public Builder addItemKeep(String itemkey, boolean unbreakable, JsonObject nbt) {
			return addItemKeep(itemkey, 1, unbreakable, nbt);
		}
		public Builder addItem(String itemkey, boolean unbreakable, JsonObject nbt) {
			return addItem(itemkey, 1, unbreakable, nbt);
		}
		public Builder addItemKeep(String itemkey, int num, JsonObject nbt) {
			return addItemKeep(itemkey, num, false, nbt);
		}
		public Builder addItemRefill(String itemkey, int num, JsonObject nbt) {
			return addItemRefill(itemkey, num, false, nbt);
		}
		public Builder addItem(String itemkey, int num, JsonObject nbt) {
			return addItem(itemkey, num, false, nbt);
		}
		public Builder addItemKeep(String itemkey, int num) {
			return addItemKeep(itemkey, num, null);
		}
		public Builder addItemRefill(String itemkey, int num) {
			return addItemRefill(itemkey, num, null);
		}
		public Builder addItem(String itemkey, int num) {
			return addItem(itemkey, num, null);
		}
		public Builder addItemKeep(String itemkey, JsonObject nbt) {
			return addItemKeep(itemkey, 1, nbt);
		}
		public Builder addItemRefill(String itemkey, JsonObject nbt) {
			return addItemRefill(itemkey, 1, nbt);
		}
		public Builder addItem(String itemkey, JsonObject nbt) {
			return addItem(itemkey, 1, nbt);
		}
		public Builder addItemKeep(String itemkey) {
			return addItemKeep(itemkey, 1, null);
		}
		public Builder addItemRefill(String itemkey) {
			return addItemRefill(itemkey, 1, null);
		}
		public Builder addItem(String itemkey) {
			return addItem(itemkey, 1, null);
		}
		protected Builder(String namespace, String id) {
			super(namespace, id, GAMEKIT);
			this.getData().add("kitItems", new JsonArray());
		}
	}
	
}
