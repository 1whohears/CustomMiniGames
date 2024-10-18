package com.onewhohears.minigames.data.kits;

import com.google.gson.JsonObject;
import com.onewhohears.minigames.MiniGamesMod;

import com.onewhohears.onewholibs.data.jsonpreset.JsonPresetGenerator;
import net.minecraft.data.DataGenerator;

public class MiniGameKitsGenerator extends JsonPresetGenerator<GameKit> {

	public static void register(DataGenerator generator) {
		generator.addProvider(true, new MiniGameKitsGenerator(generator));
	}

	public MiniGameKitsGenerator(DataGenerator output) {
		super(output, MiniGameKitsManager.KIND);
	}

	@Override
	protected void registerPresets() {
		JsonObject healthPotionNbt = new JsonObject();
		healthPotionNbt.addProperty("Potion", "minecraft:strong_healing");
		addPresetToGenerate(GameKit.Builder.create(MiniGamesMod.MODID, "standard")
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
		addPresetToGenerate(GameKit.Builder.create(MiniGamesMod.MODID, "archer")
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
		addPresetToGenerate(GameKit.Builder.create(MiniGamesMod.MODID, "builder")
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

	@Override
	public String getName() {
		return MiniGamesMod.MODID+":"+MiniGameKitsManager.KIND;
	}

}
