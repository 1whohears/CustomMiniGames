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
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ToolActions;
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
		for (int i = 0; i < inv.getContainerSize(); ++i) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) continue;
			CompoundTag tag = stack.getTag();
			if (tag == null || !tag.contains("kitOnly")) continue;
			if (tag.getString("kitOnly").equals(getId())) continue;
			inv.removeItem(stack);
		}
		for (KitItem item : items) {
			if (item.canKeep() && !inv.hasAnyMatching(item.sameChecker())) {
				givePlayerItem(player, item, -1);
			}
			if (item.canRefill()) {
				int refill = item.getRefillNum(inv);
				if (refill > 0) givePlayerItem(player, item, refill);
			}
		}
	}
	protected void givePlayerItem(ServerPlayer player, KitItem item, int num) {
		if (num == 0) return;
		ItemStack stack = item.getItem();
		if (num > 0) item.setCount(stack, num);
		Item i = stack.getItem();
		if (i instanceof ArmorItem armorItem && !player.hasItemInSlot(armorItem.getSlot())) {
			player.setItemSlot(armorItem.getSlot(), stack);
		} else if (stack.is(Items.ELYTRA) && !player.hasItemInSlot(EquipmentSlot.CHEST)) {
			player.setItemSlot(EquipmentSlot.CHEST, stack);
		} else if (stack.canPerformAction(ToolActions.SHIELD_BLOCK) && !player.hasItemInSlot(EquipmentSlot.OFFHAND)) {
			player.setItemSlot(EquipmentSlot.OFFHAND, stack);
		} else {
			player.addItem(stack);
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
		private final String checkCountByNBT;
		protected KitItem(Item item, int num, CompoundTag nbt, boolean keep, boolean refill,
						  boolean ignoreNbt, String checkCountByNBT) {
			this.item = item;
			this.num = num;
			this.nbt = nbt;
			this.keep = keep;
			this.refill = refill;
			this.ignoreNbt = ignoreNbt;
            this.checkCountByNBT = checkCountByNBT;
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
			checkCountByNBT = UtilParse.getStringSafe(json, "checkCountByNBT", "");
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
		public int getRefillNum(Container inv) {
			int count = 0;
			for (int i = 0; i < inv.getContainerSize(); ++i) {
				ItemStack stack = inv.getItem(i);
				if (isSameItem(stack)) {
					if (isGetItemCountByNBT() && stack.getTag() != null) {
						count += stack.getTag().getInt(checkCountByNBT);
					} else {
						count += stack.getCount();
					}
				}
				if (count >= num) return 0;
			}
			return num - count;
		}
		public boolean isGetItemCountByNBT() {
			return !checkCountByNBT.isEmpty();
		}
		public void setCount(ItemStack stack, int num) {
			if (isGetItemCountByNBT() && stack.getTag() != null) {
				stack.getTag().putInt(checkCountByNBT, num);
			} else {
				stack.setCount(num);
			}
		}
	}
	
	public static class Builder extends PresetBuilder<GameKit.Builder> {
		public static Builder create(String namespace, String id) {
			return new Builder(namespace, id);
		}
		public Builder addItem(String itemkey, int num, boolean unbreakable, JsonObject nbt, 
				boolean keep, boolean refill, boolean ignoreNbt, boolean kitOnly, String checkCountByNBT) {
			if (num < 1) num = 1;
			else if (num > 64) num = 64;
			JsonObject json = new JsonObject();
			json.addProperty("item", itemkey);
			json.addProperty("num", num);
			if (unbreakable) {
				if (nbt == null) nbt = new JsonObject();
				nbt.addProperty("Unbreakable", true);
			}
			if (kitOnly) {
				if (nbt == null) nbt = new JsonObject();
				nbt.addProperty("kitOnly", getPresetId());
			}
			if (nbt != null) json.add("nbt", nbt);
			if (keep) json.addProperty("keep", keep);
			if (refill) json.addProperty("refill", refill);
			if (ignoreNbt) json.addProperty("ignoreNbt", ignoreNbt);
			if (!checkCountByNBT.isEmpty()) json.addProperty("checkCountByNBT", checkCountByNBT);
			getData().get("kitItems").getAsJsonArray().add(json);
			return this;
		}
		public Builder addItemKeep(String itemkey, int num, boolean unbreakable, JsonObject nbt, boolean kitOnly, String checkCountByNBT) {
			return addItem(itemkey, num, unbreakable, nbt, true, false, false, kitOnly, checkCountByNBT);
		}
		public Builder addItemRefill(String itemkey, int num, boolean unbreakable, JsonObject nbt, boolean kitOnly, String checkCountByNBT) {
			return addItem(itemkey, num, unbreakable, nbt, true, true, false, kitOnly, checkCountByNBT);
		}
		public Builder addItem(String itemkey, int num, boolean unbreakable, JsonObject nbt, boolean kitOnly, String checkCountByNBT) {
			return addItem(itemkey, num, unbreakable, nbt, false, false, false, kitOnly, checkCountByNBT);
		}
		public Builder addItemKeep(String itemkey, int num, boolean unbreakable, JsonObject nbt, boolean kitOnly) {
			return addItem(itemkey, num, unbreakable, nbt, true, false, false, kitOnly, "");
		}
		public Builder addItemRefill(String itemkey, int num, boolean unbreakable, JsonObject nbt, boolean kitOnly) {
			return addItem(itemkey, num, unbreakable, nbt, true, true, false, kitOnly, "");
		}
		public Builder addItem(String itemkey, int num, boolean unbreakable, JsonObject nbt, boolean kitOnly) {
			return addItem(itemkey, num, unbreakable, nbt, false, false, false, kitOnly, "");
		}
		public Builder addItemKeep(String itemkey, boolean unbreakable, boolean ignoreNbt, boolean kitOnly) {
			return addItem(itemkey, 1, unbreakable, null, true, false, ignoreNbt, kitOnly, "");
		}
		public Builder addItemKeep(String itemkey, int num, boolean unbreakable, JsonObject nbt) {
			return addItem(itemkey, num, unbreakable, nbt, true, false, false, true, "");
		}
		public Builder addItemRefill(String itemkey, int num, boolean unbreakable, JsonObject nbt) {
			return addItem(itemkey, num, unbreakable, nbt, true, true, false, false, "");
		}
		public Builder addItemRefill(String itemkey, int num, boolean unbreakable, JsonObject nbt, String checkCountByNBT) {
			return addItem(itemkey, num, unbreakable, nbt, true, true, false, false, checkCountByNBT);
		}
		public Builder addItem(String itemkey, int num, boolean unbreakable, JsonObject nbt) {
			return addItem(itemkey, num, unbreakable, nbt, false, false, false, false, "");
		}
		public Builder addItemKeep(String itemkey, boolean unbreakable, boolean ignoreNbt) {
			return addItem(itemkey, 1, unbreakable, null, true, false, ignoreNbt, true, "");
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
		public Builder addItemRefill(String itemkey, int num, JsonObject nbt, String checkCountByNBT) {
			return addItemRefill(itemkey, num, false, nbt, checkCountByNBT);
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
		public Builder addItemRefill(String itemkey, JsonObject nbt, String checkCountByNBT) {
			return addItemRefill(itemkey, 1, nbt, checkCountByNBT);
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
