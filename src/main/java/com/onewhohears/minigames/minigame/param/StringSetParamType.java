package com.onewhohears.minigames.minigame.param;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.onewhohears.minigames.command.PlayerAgentSuggestion;
import com.onewhohears.minigames.command.admin.GameSetupCom;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.onewholibs.util.UtilMCText;
import com.onewhohears.onewholibs.util.UtilParse;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class StringSetParamType extends MiniGameParamType<Set<String>> {

    private final PlayerAgentSuggestion addSuggestions, removeSuggestions;

    public StringSetParamType(@NotNull String id) {
        this(id, Set.of());
    }

    public StringSetParamType(@NotNull String id, @NotNull Set<String> defaultValue) {
        this(id, defaultValue, suggestNothing(), suggestNothing());
    }

    public StringSetParamType(@NotNull String id, PlayerAgentSuggestion addSuggestions,
                              PlayerAgentSuggestion removeSuggestions) {
        this(id, Set.of(), addSuggestions, removeSuggestions);
    }

    public StringSetParamType(@NotNull String id, @NotNull Set<String> defaultValue,
                              PlayerAgentSuggestion addSuggestions, PlayerAgentSuggestion removeSuggestions) {
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
            String value = StringArgumentType.getString(context, getSetterArgumentName());
            Set<String> list = gameData.getParam(this);
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
                        .append(value).append("!");
                context.getSource().sendFailure(message);
                return 0;
            }
            return 1;
        };
    }

    protected ListParamModifier getAdderApplier() {
        return (context, gameData, list, value) -> {
            list.add(value);
            return true;
        };
    }

    protected ListParamModifier getRemoveApplier() {
        return (context, gameData, list, value) -> {
            list.remove(value);
            return true;
        };
    }

    public interface ListParamModifier {
        boolean apply(CommandContext<CommandSourceStack> context, MiniGameData gameData, Set<String> list, String value);
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
    protected String getSetterArgumentName() {
        return "value";
    }

    @Override
    protected ArgumentType<?> getSetterArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Set<String> getInputtedValue(CommandContext<CommandSourceStack> context, String name) {
        return Set.of();
    }

    protected PlayerAgentSuggestion getAddSuggestions() {
        return addSuggestions;
    }

    protected PlayerAgentSuggestion getRemoveSuggestions() {
        return removeSuggestions;
    }

    @Override
    protected MutableComponent getDisplayComponentFromValue(Set<String> value) {
        return UtilMCText.literal(String.join(" ", value));
    }
}
