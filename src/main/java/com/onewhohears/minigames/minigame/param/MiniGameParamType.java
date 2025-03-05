package com.onewhohears.minigames.minigame.param;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.onewhohears.minigames.command.PlayerAgentSuggestion;
import com.onewhohears.minigames.command.admin.GameSetupCom;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

public abstract class MiniGameParamType<E> {

    @NotNull private final String id;
    @NotNull private final E defaultValue;

    public MiniGameParamType(@NotNull String id, @NotNull E defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
    }

    public abstract void save(CompoundTag tag, E value);
    public abstract E load(CompoundTag tag);

    public ArgumentBuilder<CommandSourceStack,?> getCommandArgument() {
        return Commands.literal(getId())
                .executes(getGetterExecutor())
                .then(getSetterArgument());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, ?> getSetterArgument() {
        return Commands.argument(getSetterArgumentName(), getSetterArgumentType())
                .executes(getSetterExecutor())
                .suggests(getSuggestions());
    }

    protected abstract String getSetterArgumentName();
    protected abstract ArgumentType<E> getSetterArgumentType();
    protected abstract E getInputtedValue(CommandContext<CommandSourceStack> context, String name);
    protected abstract PlayerAgentSuggestion getSuggestions();
    protected abstract MutableComponent getDisplayComponentFromValue(E value);

    protected TriFunction<CommandContext<CommandSourceStack>, MiniGameData, E, Integer> getSetterApplier() {
        return (context, gameData, value) -> {
            if (gameData.setParam(this, value)) {
                MutableComponent v = getDisplayComponentFromValue(value);
                MutableComponent message = UtilMCText.literal("Set ")
                        .append(UtilMCText.translatable(getDisplayName()))
                        .append(" to ").append(v);
                context.getSource().sendSuccess(message, true);
                return 1;
            } else {
                MutableComponent message = UtilMCText.literal("The Parameter Type ")
                        .append(UtilMCText.translatable(getDisplayName()))
                        .append("is not used by this game type!");
                context.getSource().sendFailure(message);
                return 0;
            }
        };
    }

    protected GameSetupCom getSetterExecutor() {
        return (context, gameData) -> {
            E value = getInputtedValue(context, getSetterArgumentName());
            return getSetterApplier().apply(context, gameData, value);
        };
    }

    protected GameSetupCom getGetterExecutor() {
        return (context, gameData) -> {
            MutableComponent value = getDisplayComponentFromValue(gameData.getParam(this));
            MutableComponent message = UtilMCText.literal("The game "+gameData.getInstanceId()
                    +" has parameter ").append(UtilMCText.translatable(getDisplayName()))
                    .append(" set to ").append(value);
            context.getSource().sendSuccess(message, false);
            return 1;
        };
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public E getDefaultValue() {
        return defaultValue;
    }

    @NotNull
    public String getDisplayName() {
        return "param_type."+getId();
    }
}
