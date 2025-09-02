package com.onewhohears.minigames.minigame.event;

import com.onewhohears.minigames.minigame.agent.PlayerAgent;
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

public interface SummonEvent extends TriFunction<ServerPlayer, PlayerAgent, CompoundTag, Boolean> {

    @Override
    default Boolean apply(ServerPlayer player, PlayerAgent agent, CompoundTag params) {
        String entityTypeKey = params.getString("entity");
        EntityType<?> type = UtilEntity.getEntityType(entityTypeKey, null);
        if (type == null) {
            sendError(player, "Entity Type "+entityTypeKey+" does not exist.");
            return false;
        }
        Entity entity = type.create(player.getLevel());
        if (entity == null) {
            sendError(player, "Entity couldn't be created.");
            return false;
        }
        if (params.contains("nbt")) entity.load(params.getCompound("nbt"));
        if (params.contains("pos")) entity.setPos(UtilParse.readVec3(params, "pos"));
        else entity.setPos(player.position());
        if (params.contains("yaw")) entity.setYRot(params.getFloat("yaw"));
        if (!player.getLevel().addFreshEntity(entity)) {
            sendError(player, "Entity couldn't be added to the world.");
            return false;
        }
        return apply(player, agent, params, entity);
    }

    Boolean apply(ServerPlayer player, PlayerAgent agent, CompoundTag params, Entity entity);

    static void sendError(Player player, String msg) {
        player.sendSystemMessage(UtilMCText.literal(msg).setStyle(RED));
    }
}
