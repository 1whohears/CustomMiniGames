package com.onewhohears.minigames.minigame.param;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.onewhohears.minigames.command.admin.GameSetupCom;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public abstract class SetParamType<C extends Set<E>, E> extends MiniGameParamType<C> {

    private final SuggestionProvider<CommandSourceStack> addSuggestions, removeSuggestions;

    public SetParamType(@NotNull String id) {
        this(id, (C) new HashSet<>());
    }

    public SetParamType(@NotNull String id, @NotNull C defaultValue) {
        this(id, defaultValue, suggestNothing(), suggestNothing());
    }

    public SetParamType(@NotNull String id, SuggestionProvider<CommandSourceStack> addSuggestions,
                        SuggestionProvider<CommandSourceStack> removeSuggestions) {
        this(id, (C) new HashSet<>(), addSuggestions, removeSuggestions);
    }

    public SetParamType(@NotNull String id, @NotNull C defaultValue,
                        SuggestionProvider<CommandSourceStack> addSuggestions,
                        SuggestionProvider<CommandSourceStack> removeSuggestions) {
        super(id, defaultValue);
        this.addSuggestions = addSuggestions;
        this.removeSuggestions = removeSuggestions;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack,?> getCommandArgument() {
        return Commands.literal(getId()).executes(getGetterExecutor())
                .then(Commands.literal("add")
                        .then(Commands.argument(getSetterArgumentName(), StringArgumentType.string())
                                .executes(getModifyExecutor(true))
                                .suggests(getAddSuggestions())
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument(getSetterArgumentName(), StringArgumentType.string())
                                .executes(getModifyExecutor(false))
                                .suggests(getRemoveSuggestions())
                        )
                );
    }

    protected GameSetupCom getModifyExecutor(boolean add) {
        return (context, gameData) -> {
            if (!gameData.usesParam(this)) {
                MutableComponent message = UtilMCText.literal("The Parameter Type ")
                        .append(UtilMCText.translatable(getDisplayName()))
                        .append(" is not used by this game type!");
                context.getSource().sendFailure(message);
                return 0;
            }
            E value = getInputtedListMember(context, getSetterArgumentName());
            C list = gameData.getParam(this);
            if (add && getAdderApplier().apply(context, gameData, list, value)) {
                MutableComponent message = UtilMCText.literal(value+" has been added to ")
                        .append(UtilMCText.translatable(getDisplayName()));
                context.getSource().sendSuccess(message, true);
            } else if (!add && getRemoveApplier().apply(context, gameData, list, value)) {
                MutableComponent message = UtilMCText.literal(value+" has been removed from ")
                        .append(UtilMCText.translatable(getDisplayName()));
                context.getSource().sendSuccess(message, true);
            } else {
                MutableComponent message = UtilMCText.literal("Could not add/remove ")
                        .append(value+"").append("!");
                context.getSource().sendFailure(message);
                return 0;
            }
            return 1;
        };
    }

    protected ListParamModifier<C, E> getAdderApplier() {
        return (context, gameData, list, value) -> {
            list.add(value);
            return true;
        };
    }

    protected ListParamModifier<C, E> getRemoveApplier() {
        return (context, gameData, list, value) -> {
            list.remove(value);
            return true;
        };
    }

    public interface ListParamModifier<C extends Set<E>, E> {
        boolean apply(CommandContext<CommandSourceStack> context, MiniGameData gameData, C list, E value);
    }

    protected SuggestionProvider<CommandSourceStack> getAddSuggestions() {
        return addSuggestions;
    }

    protected SuggestionProvider<CommandSourceStack> getRemoveSuggestions() {
        return removeSuggestions;
    }

    @Override
    protected String getSetterArgumentName() {
        return "value";
    }

    @Override
    protected C getInputtedValue(CommandContext<CommandSourceStack> context, String name) {
        return (C) Set.of();
    }

    protected abstract E getInputtedListMember(CommandContext<CommandSourceStack> context, String name);

}
