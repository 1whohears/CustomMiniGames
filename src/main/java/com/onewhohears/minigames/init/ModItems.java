package com.onewhohears.minigames.init;

import com.onewhohears.minigames.MiniGamesMod;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MiniGamesMod.MODID);
	
	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
	
	public static final RegistryObject<Item> MONEY = ITEMS.register("money", 
			() -> new Item(new Item.Properties().stacksTo(64).tab(CreativeModeTab.TAB_MISC).food(
					new FoodProperties.Builder().nutrition(-1).alwaysEat()
						.build())));
	
	public static final RegistryObject<Item> EVIL_MONEY = ITEMS.register("evil_money", 
			() -> new Item(new Item.Properties().stacksTo(64).tab(CreativeModeTab.TAB_MISC).food(
					new FoodProperties.Builder().nutrition(-20).alwaysEat()
						.effect(() -> new MobEffectInstance(MobEffects.WITHER, 400, 5), 1)
						.build())));
	
	public static final RegistryObject<Item> MONEY_MONEY = ITEMS.register("money_money", 
			() -> new Item(new Item.Properties().stacksTo(64).tab(CreativeModeTab.TAB_MISC).food(
					new FoodProperties.Builder().nutrition(-20).alwaysEat()
						.effect(() -> new MobEffectInstance(MobEffects.HARM, 1, 100), 1)
						.build())));
	
	public static final RegistryObject<Item> WACKY_MONEY = ITEMS.register("wacky_money", 
			() -> new Item(new Item.Properties().stacksTo(64).tab(CreativeModeTab.TAB_MISC).food(
					new FoodProperties.Builder().nutrition(-5).alwaysEat()
						.effect(() -> new MobEffectInstance(MobEffects.POISON, 400, 5), 1)
						.build())));
	
	public static final RegistryObject<Item> ZELDA_MONEY = ITEMS.register("zelda_money", 
			() -> new Item(new Item.Properties().stacksTo(64).tab(CreativeModeTab.TAB_MISC).food(
					new FoodProperties.Builder().nutrition(-5).alwaysEat()
						.effect(() -> new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 5), 1)
						.build())));
	
	public static final RegistryObject<Item> YAKUZA_MONEY = ITEMS.register("yakuza_money", 
			() -> new Item(new Item.Properties().stacksTo(64).tab(CreativeModeTab.TAB_MISC).food(
					new FoodProperties.Builder().nutrition(-5).alwaysEat()
						.effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 600, 10), 1)
						.build())));
	
}
