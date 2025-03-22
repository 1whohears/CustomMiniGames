package com.onewhohears.minigames.minigame.event;

import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.util.CommandUtil;
import com.onewhohears.onewholibs.util.UtilEntity;
import com.onewhohears.onewholibs.util.UtilMCText;
import com.onewhohears.onewholibs.util.UtilParse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.function.TriFunction;

import static com.onewhohears.minigames.minigame.data.MiniGameData.RED;

public interface FunctionEvent extends TriFunction<ServerPlayer, PlayerAgent, CompoundTag, Boolean> {

    @Override
    default Boolean apply(ServerPlayer player, PlayerAgent agent, CompoundTag params) {
        String functionId = params.getString("function");
        CommandUtil.runFunction(player.getServer(), functionId);
        return postFunction(player, agent, params);
    }

    Boolean postFunction(ServerPlayer player, PlayerAgent agent, CompoundTag params);

    static void sendError(Player player, String msg) {
        player.sendSystemMessage(UtilMCText.literal(msg).setStyle(RED));
    }
}
