package com.onewhohears.minigames.command;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public interface PlayerAgentSuggestion extends SuggestionProvider<CommandSourceStack> {
	
	default CompletableFuture<Suggestions> getSuggestions(final CommandContext<CommandSourceStack> context, 
			final SuggestionsBuilder builder) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayer();
		if (player == null) return null;
		List<PlayerAgent<?>> agents = MiniGameManager.get().getActiveGamePlayerAgents(player);
		if (agents.size() == 0) return null;
		return getPlayerAgentsSuggestions(context, builder, agents);
	}
	
	CompletableFuture<Suggestions> getPlayerAgentsSuggestions(final CommandContext<CommandSourceStack> context, 
			final SuggestionsBuilder builder, List<PlayerAgent<?>> agents) throws CommandSyntaxException;
	
}
