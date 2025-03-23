package com.onewhohears.minigames.util;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class CommandUtil {
	
	public static SuggestionProvider<CommandSourceStack> suggestStrings(String[] strings) {
		return (context, builder) -> SharedSuggestionProvider.suggest(strings, builder);
	}
	
	public static SuggestionProvider<CommandSourceStack> suggestStrings(Supplier<String[]> strings) {
		return (context, builder) -> SharedSuggestionProvider.suggest(strings.get(), builder);
	}
	
	public static void suggestStringToBuilder(SuggestionsBuilder builder, String[] strings) {
		for (String s : strings) builder.suggest(s);
	}

	public static void suggestStringToBuilder(SuggestionsBuilder builder, Set<String> strings) {
		for (String s : strings) builder.suggest(s);
	}

	public static void suggestStringToBuilder(SuggestionsBuilder builder, Collection<String> strings) {
		for (String s : strings) builder.suggest(s);
	}

	public static void runFunction(MinecraftServer server, String id, CommandSourceStack stack) {
		if (id.startsWith("#")) {
			Collection<CommandFunction> list = server.getFunctions().getTag(new ResourceLocation(id));
			for (CommandFunction f : list) server.getFunctions().execute(f, stack);
		} else {
			Optional<CommandFunction> function = server.getFunctions().get(new ResourceLocation(id));
			function.ifPresent(func -> server.getFunctions().execute(func, stack));
		}
	}

	public static void runFunction(MinecraftServer server, String id) {
		runFunction(server, id, server.getFunctions().getGameLoopSender());
	}

	public static void runFunctionAs(MinecraftServer server, String id, ServerPlayer player) {
		runFunction(server, id, player.createCommandSourceStack());
	}
	
}
