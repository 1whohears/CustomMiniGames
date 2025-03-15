package com.onewhohears.minigames.minigame.param;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.onewhohears.onewholibs.util.UtilMCText;
import com.onewhohears.onewholibs.util.UtilParse;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StringSetParamType extends SetParamType<Set<String>, String> {

    public StringSetParamType(@NotNull String id) {
        this(id, new HashSet<>());
    }

    public StringSetParamType(@NotNull String id, @NotNull Set<String> defaultValue) {
        this(id, defaultValue, suggestNothing(), suggestNothing());
    }

    public StringSetParamType(@NotNull String id, SuggestionProvider<CommandSourceStack> addSuggestions,
                              SuggestionProvider<CommandSourceStack> removeSuggestions) {
        this(id, new HashSet<>(), addSuggestions, removeSuggestions);
    }

    public StringSetParamType(@NotNull String id, @NotNull Set<String> defaultValue,
                              SuggestionProvider<CommandSourceStack> addSuggestions,
                              SuggestionProvider<CommandSourceStack> removeSuggestions) {
        super(id, defaultValue, addSuggestions, removeSuggestions);
    }

    @Override
    public Collection<String> toStringList(Set<String> param) {
        return param;
    }

    @Override
    protected String getInputtedListMember(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return StringArgumentType.getString(context, name);
    }

    @Override
    public void save(CompoundTag tag, Set<String> value) {
        UtilParse.writeStrings(tag, getId(), value);
    }

    @Override
    public Set<String> load(CompoundTag tag) {
        return UtilParse.readStringSet(tag, getId());
    }

    @Override
    protected ArgumentType<?> getSetterArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected MutableComponent getDisplayComponentFromValue(Set<String> value) {
        return UtilMCText.literal(String.join(" ", value));
    }
}
