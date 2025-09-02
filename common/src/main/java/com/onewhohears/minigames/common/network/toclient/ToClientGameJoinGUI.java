package com.onewhohears.minigames.common.network.toclient;

import com.onewhohears.minigames.util.UtilClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ToClientGameJoinGUI {
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
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(ids.length);
        for (String kit : ids) buffer.writeUtf(kit);
        buffer.writeInt(teamMap.size());
        teamMap.forEach((id, teams) -> {
            buffer.writeUtf(id);
            buffer.writeInt(teams.length);
            for (String team : teams) buffer.writeUtf(team);
        });
    }
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            ctx.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    success.set(true);
                    UtilClientPacket.handleGameSelectGui(ids, teamMap);
                });
            });
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
