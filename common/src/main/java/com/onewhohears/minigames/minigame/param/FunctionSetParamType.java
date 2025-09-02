package com.onewhohears.minigames.minigame.param;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class FunctionSetParamType extends StringSetParamType {

    public FunctionSetParamType(@NotNull String id) {
        super(id);
    }

    public FunctionSetParamType(@NotNull String id, SuggestionProvider<CommandSourceStack> addSuggestions,
                                SuggestionProvider<CommandSourceStack> removeSuggestions) {
        super(id, addSuggestions, removeSuggestions);
    }

    public FunctionSetParamType(@NotNull String id, @NotNull Set<String> defaultValue) {
        super(id, defaultValue);
    }

    public FunctionSetParamType(@NotNull String id, @NotNull Set<String> defaultValue,
                                SuggestionProvider<CommandSourceStack> addSuggestions,
                                SuggestionProvider<CommandSourceStack> removeSuggestions) {
        super(id, defaultValue, addSuggestions, removeSuggestions);
    }

    @Override
    protected String getInputtedListMember(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        ResourceLocation id = FunctionArgument.getFunctionOrTag(context, name).getFirst();
        return id.toString();
    }

    @Override
    protected ArgumentType<?> getSetterArgumentType() {
        return FunctionArgument.functions();
    }
}
