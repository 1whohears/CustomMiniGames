package com.onewhohears.minigames.data.kits;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.data.JsonData;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

public class MiniGameKitsGenerator implements DataProvider {
	
	public static void register(DataGenerator generator) {
		generator.addProvider(true, new MiniGameKitsGenerator(generator));
	}
	
	protected final DataGenerator.PathProvider pathProvider;
	private final Map<ResourceLocation, JsonData> gen_map = new HashMap<>();
	
	protected MiniGameKitsGenerator(DataGenerator generator) {
        this.pathProvider = generator.createPathProvider(DataGenerator.Target.DATA_PACK, MiniGameKitsManager.KIND);
    }
	
	protected void registerKits() {
		JsonObject healthPotionNbt = new JsonObject();
		healthPotionNbt.addProperty("Potion", "minecraft:strong_healing");
		registerKit(GameKit.Builder.create(MiniGamesMod.MODID, "standard")
				.addItemKeep("minecraft:diamond_sword", true)
				.addItemKeep("minecraft:iron_axe", true)
				.addItemKeep("minecraft:iron_pickaxe", true)
				.addItemKeep("minecraft:iron_shovel", true)
				.addItemRefill("minecraft:cooked_beef", 32)
				.addItemRefill("minecraft:splash_potion", healthPotionNbt)
				.addItemRefill("minecraft:splash_potion", healthPotionNbt)
				.addItem("minecraft:cobblestone", 64)
				.addItemKeep("minecraft:shield", true)
				.addItemKeep("minecraft:iron_helmet", true)
				.addItemKeep("minecraft:iron_chestplate", true)
				.addItemKeep("minecraft:iron_leggings", true)
				.addItemKeep("minecraft:iron_boots", true)
				.build());
		registerKit(GameKit.Builder.create(MiniGamesMod.MODID, "archer")
				.addItemKeep("minecraft:bow", true)
				.addItemKeep("minecraft:iron_axe", true)
				.addItemKeep("minecraft:iron_pickaxe", true)
				.addItemKeep("minecraft:iron_shovel", true)
				.addItemRefill("minecraft:bread", 32)
				.addItemKeep("minecraft:crossbow", true)
				.addItemRefill("minecraft:splash_potion", healthPotionNbt)
				.addItem("minecraft:cobblestone", 64)
				.addItemKeep("minecraft:shield", true)
				.addItemKeep("minecraft:chainmail_helmet", true)
				.addItemKeep("minecraft:chainmail_chestplate", true)
				.addItemKeep("minecraft:chainmail_leggings", true)
				.addItemKeep("minecraft:chainmail_boots", true)
				.addItemRefill("minecraft:arrow", 64)
				.addItemKeep("minecraft:arrow", 64)
				.addItemKeep("minecraft:arrow", 64)
				.build());
		registerKit(GameKit.Builder.create(MiniGamesMod.MODID, "builder")
				.addItemKeep("minecraft:diamond_axe", true)
				.addItemKeep("minecraft:diamond_pickaxe", true)
				.addItemKeep("minecraft:diamond_shovel", true)
				.addItemRefill("minecraft:bread", 32)
				.addItemKeep("minecraft:water_bucket")
				.addItem("minecraft:bricks", 64)
				.addItem("minecraft:bricks", 64)
				.addItem("minecraft:cobblestone", 64)
				.addItemKeep("minecraft:shield", true)
				.addItemKeep("minecraft:leather_helmet", true)
				.addItemKeep("minecraft:leather_chestplate", true)
				.addItemKeep("minecraft:leather_leggings", true)
				.addItemKeep("minecraft:leather_boots", true)
				.build());
	}
	
	protected void registerKit(JsonData kit) {
		gen_map.put(kit.getKey(), kit);
	}
	
	@Override
	public void run(CachedOutput cache) throws IOException {
		gen_map.clear();
		registerKits();
		Set<ResourceLocation> set = Sets.newHashSet();
		gen_map.forEach((key, kit) -> {
			if (!set.add(kit.getKey())) {
				throw new IllegalStateException("Duplicate Kit Not Allowed! " + kit.getKey());
			} else {
				Path path = pathProvider.json(kit.getKey());
				try {
					DataProvider.saveStable(cache, kit.getJsonData(), path);
				} catch (IOException e) {
					e.printStackTrace();
	            }
			}
		});
	}

	@Override
	public String getName() {
		return MiniGamesMod.MODID+":"+MiniGameKitsManager.KIND;
	}

}
