package com.onewhohears.minigames.command.admin;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class MiniGameAdminCommands {
	
	public MiniGameAdminCommands(CommandDispatcher<CommandSourceStack> d) {
		d.register(Commands.literal("minigame").requires((stack) -> stack.hasPermission(2))
				.then(new SubComSetup().setup())
				.then(new SubComCreateNew().createNew())
				.then(new SubComReset().reset())
				.then(new SubComPause().pause())
				.then(new SubComResume().resume())
				.then(new SubComInfo().info())
				.then(new SubComKit().kit())
				.then(new SubComShop().shop())
				.then(new SubComOverride().override())
				.then(new SubComRemove().remove())
		);
	}
	
}
