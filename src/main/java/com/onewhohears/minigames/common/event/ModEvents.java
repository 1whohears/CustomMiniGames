package com.onewhohears.minigames.common.event;

import com.onewhohears.minigames.MiniGamesMod;
import com.onewhohears.minigames.command.admin.MiniGameAdminCommands;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MiniGamesMod.MODID)
public final class ModEvents {
	
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		new MiniGameAdminCommands(event.getDispatcher());
	}
	
}
