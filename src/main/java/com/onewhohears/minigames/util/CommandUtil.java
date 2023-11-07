package com.onewhohears.minigames.util;

import java.util.function.Supplier;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public class CommandUtil {
	
	public static SuggestionProvider<CommandSourceStack> suggestStrings(String[] strings) {
		return (context, builder) -> {
			return SharedSuggestionProvider.suggest(strings, builder);
		};
	}
	
	public static SuggestionProvider<CommandSourceStack> suggestStrings(Supplier<String[]> strings) {
		return (context, builder) -> {
			return SharedSuggestionProvider.suggest(strings.get(), builder);
		};
	}
	
}
