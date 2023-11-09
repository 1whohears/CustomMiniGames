package com.onewhohears.minigames.data.shops;

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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class GameShop extends JsonData {
	
	protected List<Product> products = new ArrayList<>();
	public GameShop(ResourceLocation key, JsonObject json) {
		super(key, json);
		JsonArray list = json.get("products").getAsJsonArray();
		for (int i = 0; i < list.size(); ++i) {
			JsonObject jo = list.get(i).getAsJsonObject();
			Product product = Product.create(jo);
			if (product != null) products.add(product);
		}
	}
	public int getProductNum() {
		return products.size();
	}
	public ItemStack[] getContainerList() {
		ItemStack[] items = new ItemStack[products.size()*2];
		int i = 0;
		for (Product product : products) {
			items[i++] = product.getCostItem();
			items[i++] = product.getProductItem();
		}
		return items;
	}
	public Product getProductByMenuSlot(int slotNum) {
		int index = slotNum/2;
		return products.get(index);
	}
	
	public static class Product {
		@Nullable
		public static Product create(JsonObject json) {
			if (!verifySafe(json)) return null;
			return new Product(json);
		}
		public static boolean verifySafe(JsonObject json) {
			JsonObject productJson = json.get("product").getAsJsonObject();
			String productItemKey = UtilParse.getStringSafe(productJson, "item", "");
			if (productItemKey.isEmpty()) return false;
			ResourceLocation prl = new ResourceLocation(productItemKey);
			if (!ForgeRegistries.ITEMS.containsKey(prl)) return false;
			JsonObject costJson = json.get("cost").getAsJsonObject();
			String costItemKey = UtilParse.getStringSafe(costJson, "item", "");
			if (costItemKey.isEmpty()) return false;
			ResourceLocation crl = new ResourceLocation(costItemKey);
			if (!ForgeRegistries.ITEMS.containsKey(crl)) return false;
			return true;
		}
		private final Item productItem, costItem;
		private final int productNum, costNum;
		private final CompoundTag productNbt, costNbt;
		protected Product(Item productItem, int productNum, CompoundTag productNbt,
				Item costItem, int costNum, CompoundTag costNbt) {
			this.productItem = productItem;
			this.productNum = productNum;
			this.productNbt = productNbt;
			this.costItem = costItem;
			this.costNum = costNum;
			this.costNbt = costNbt;
		}
		protected Product(JsonObject json) {
			JsonObject productJson = json.get("product").getAsJsonObject();
			String productItemKey = UtilParse.getStringSafe(productJson, "item", "");
			ResourceLocation prl = new ResourceLocation(productItemKey);
			productItem = ForgeRegistries.ITEMS.getDelegate(prl).get().get();
			productNum = UtilParse.getIntSafe(productJson, "num", 1);
			if (productJson.has("nbt")) {
				JsonObject nbtJson = productJson.get("nbt").getAsJsonObject();
				productNbt = JsonToNBTUtil.getTagFromJson(nbtJson);
			} else productNbt = null;
			JsonObject costJson = json.get("cost").getAsJsonObject();
			String costItemKey = UtilParse.getStringSafe(costJson, "item", "");
			ResourceLocation crl = new ResourceLocation(costItemKey);
			costItem = ForgeRegistries.ITEMS.getDelegate(crl).get().get();
			costNum = UtilParse.getIntSafe(costJson, "num", 1);
			if (costJson.has("nbt")) {
				JsonObject nbtJson = costJson.get("nbt").getAsJsonObject();
				costNbt = JsonToNBTUtil.getTagFromJson(nbtJson);
			} else costNbt = null;
		}
		public ItemStack getProductItem() {
			ItemStack stack = new ItemStack(productItem, productNum);
			if (productNbt != null) stack.setTag(productNbt);
			return stack;
		}
		public ItemStack getCostItem() {
			ItemStack stack = new ItemStack(costItem, costNum);
			if (costNbt != null) stack.setTag(costNbt);
			return stack;
		}
		public boolean canBuy(Player player) {
			Inventory inv = player.getInventory();
			int costFound = 0;
			ItemStack cost = getCostItem();
			for (int i = 0; i < inv.getContainerSize(); ++i) {
				ItemStack stack = inv.getItem(i);
				if (isCostItem(cost, stack)) {
					costFound += stack.getCount();
					if (costFound >= costNum) return true;
				}
			}
			return false;
		}
		public boolean handlePurchase(Player player, boolean gui) {
			if (!canBuy(player)) return false;
			Inventory inv = player.getInventory();
			int costFound = 0;
			ItemStack cost = getCostItem();
			for (int i = 0; i < inv.getContainerSize(); ++i) {
				ItemStack stack = inv.getItem(i);
				if (isCostItem(cost, stack)) {
					costFound += stack.getCount();
					stack.shrink(costNum);
					if (costFound >= costNum) break;
				}
			}
			if (!gui) player.addItem(getProductItem());
			return true;
		}
		private boolean isCostItem(ItemStack cost, ItemStack stack) {
			if (stack.isEmpty() || stack.isDamaged()) return false;
			if (costNbt != null && !ItemStack.tagMatches(cost, stack)) return false;
			else if (costNbt == null && !ItemStack.isSame(cost, stack)) return false;
			return true;
		}
		@Override
		public String toString() {
			return "[Product:"+getProductItem().toString()+"]";
		}
	}
	
	public static class Builder extends JsonData.Builder<GameShop.Builder> {
		public static Builder create(String namespace, String id) {
			return new Builder(namespace, id);
		}
		public Builder addProduct(String productItem, int productNum, boolean productUnbreakable, 
				JsonObject productNbt, String costItem, int costNum, JsonObject costNbt) {
			JsonObject productJson = new JsonObject();
			productJson.addProperty("item", productItem);
			productJson.addProperty("num", productNum);
			if (productUnbreakable) {
				if (productNbt == null) productNbt = new JsonObject();
				productNbt.addProperty("Unbreakable", true);
			}
			if (productNbt != null) productJson.add("nbt", productNbt);
			JsonObject costJson = new JsonObject();
			costJson.addProperty("item", costItem);
			costJson.addProperty("num", costNum);
			if (costNbt != null) costJson.add("nbt", costNbt);
			JsonObject json = new JsonObject();
			json.add("product", productJson);
			json.add("cost", costJson);
			jsonData.get("products").getAsJsonArray().add(json);
			return this;
		}
		public Builder addProduct(String productItem, int productNum, boolean productUnbreakable, JsonObject productNbt, String costItem, int costNum) {
			return addProduct(productItem, productNum, productUnbreakable, productNbt, costItem, costNum, null);
		}
		public Builder addProduct(String productItem, boolean productUnbreakable, JsonObject productNbt, String costItem, int costNum) {
			return addProduct(productItem, 1, productUnbreakable, productNbt, costItem, costNum, null);
		}
		public Builder addProduct(String productItem, boolean productUnbreakable, String costItem, int costNum) {
			return addProduct(productItem, 1, productUnbreakable, null, costItem, costNum, null);
		}
		public Builder addProduct(String productItem, int productNum, JsonObject productNbt, String costItem, int costNum) {
			return addProduct(productItem, productNum, false, productNbt, costItem, costNum, null);
		}
		public Builder addProduct(String productItem, int productNum, String costItem, int costNum) {
			return addProduct(productItem, productNum, false, null, costItem, costNum, null);
		}
		public Builder addProduct(String productItem, JsonObject productNbt, String costItem, int costNum) {
			return addProduct(productItem, 1, false, productNbt, costItem, costNum, null);
		}
		public Builder addProduct(String productItem, String costItem, int costNum) {
			return addProduct(productItem, 1, false, null, costItem, costNum, null);
		}
		protected Builder(String namespace, String id) {
			super(namespace, id, (key, json) -> new GameShop(key, json));
			jsonData.add("products", new JsonArray());
		}
	}
	
}
