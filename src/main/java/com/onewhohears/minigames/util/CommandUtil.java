package com.onewhohears.minigames.util;

import java.util.function.Supplier;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

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
	
}
