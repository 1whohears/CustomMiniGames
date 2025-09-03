package com.onewhohears.minigames.common.network.toclient;

import com.onewhohears.minigames.util.UtilClientPacket;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

public class ToClientGameJoinGUI extends BaseS2CMessage {
    private final String[] ids;
    private final Map<String, String[]> teamMap;
    public ToClientGameJoinGUI(String[] ids, Map<String, String[]> teamMap) {
        this.ids = ids;
        this.teamMap = teamMap;
    }
    public ToClientGameJoinGUI(FriendlyByteBuf buffer) {
        int length = buffer.readInt();
        ids = new String[length];
        for (int i = 0; i < length; ++i) ids[i] = buffer.readUtf();
        teamMap = new HashMap<>();
        int teamMapNum = buffer.readInt();
        for (int i = 0; i < teamMapNum; ++i) {
            String id = buffer.readUtf();
            int l = buffer.readInt();
            String[] teams = new String[l];
            for (int j = 0; j < l; ++j) teams[j] = buffer.readUtf();
            teamMap.put(id, teams);
        }
    }
    @Override
    public MessageType getType() {
        return null;
    }
    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(ids.length);
        for (String kit : ids) buffer.writeUtf(kit);
        buffer.writeInt(teamMap.size());
        teamMap.forEach((id, teams) -> {
            buffer.writeUtf(id);
            buffer.writeInt(teams.length);
            for (String team : teams) buffer.writeUtf(team);
        });
    }
    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> UtilClientPacket.handleGameSelectGui(ids, teamMap));
    }
}
