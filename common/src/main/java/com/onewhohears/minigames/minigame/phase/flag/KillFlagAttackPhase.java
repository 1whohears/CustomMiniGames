package com.onewhohears.minigames.minigame.phase.flag;

import com.onewhohears.minigames.entity.FlagEntity;
import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.agent.TeamAgent;
import com.onewhohears.minigames.minigame.condition.*;
import com.onewhohears.minigames.minigame.data.KillFlagData;
import com.onewhohears.minigames.minigame.phase.attackdefend.AttackDefendAttackPhase;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;

import static com.onewhohears.minigames.minigame.data.MiniGameData.RED;
import static com.onewhohears.minigames.minigame.param.MiniGameParamTypes.*;

public class KillFlagAttackPhase<T extends KillFlagData> extends AttackDefendAttackPhase<T> {

    public KillFlagAttackPhase(T gameData) {
        this("buy_attack_attack", gameData,
                new KillFlagAttackTimeoutCondition<>(phase -> phase.getGameData().getAttackTime()),
                new BuyAttackGameWinCondition<>(), new KillFlagAttackersDeadCondition<>(),
                new KillFlagFlagsDeadCondition<>());
    }

    @SafeVarargs
    public KillFlagAttackPhase(String id, T gameData, PhaseExitCondition<T>... exitConditions) {
        super(id, gameData, exitConditions);
    }

    @Override
    public void onStart(MinecraftServer server) {
        super.onStart(server);
        getGameData().getForceFFSafeTimeMap().clear();
    }

    @Override
    public void tickPhase(MinecraftServer server) {
        super.tickPhase(server);
        if (getAge() % 20 == 0) tickForceForfeit(server);
    }

    protected void tickForceForfeit(MinecraftServer server) {
        List<FlagEntity> flags = getGameData().getLivingFlags();
        if (flags.size() != 1) return;
        FlagEntity flag = flags.get(0);
        Vec3 center = flag.position();

        int ffRadiusStart = getGameData().getIntParam(FORCE_FF_RADIUS_START);
        int ffRadiusEnd = getGameData().getIntParam(FORCE_FF_RADIUS_END);
        if (ffRadiusStart == 0 && ffRadiusEnd == 0) return;

        int ffWarnTime = getGameData().getIntParam(FORCE_FF_WARN_TIME);
        int ffShrinkTime = getGameData().getIntParam(FORCE_FF_RADIUS_SHRINK_TIME);
        int ffRadius;
        if (ffShrinkTime != 0) {
            ffRadius = (int) ((ffRadiusEnd - ffRadiusStart)
                    * Math.min((double) getAge() / (double) ffShrinkTime, 1d))
                    + ffRadiusStart;
        } else if (ffRadiusStart != 0) {
            ffRadius = ffRadiusStart;
        } else {
            ffRadius = ffRadiusEnd;
        }
        int ffRadiusSqr = ffRadius * ffRadius;

        List<GameAgent> attackers = getGameData().getLivingAttackers();
        for (GameAgent agent : attackers) {
            if (agent.isTeam()) {
                TeamAgent team = (TeamAgent) agent;
                Collection<PlayerAgent> players = team.getLivingPlayerAgents();
                for (PlayerAgent player : players) {
                    ffRadiusPlayerCheck(server, player, ffRadiusSqr, center, ffWarnTime);
                }
            } else if (agent.isPlayer()) {
                ffRadiusPlayerCheck(server, (PlayerAgent) agent, ffRadiusSqr, center, ffWarnTime);
            }
        }
    }

    protected void ffRadiusPlayerCheck(MinecraftServer server, PlayerAgent agent, int ffRadiusSqr, Vec3 center, int ffWarnTime) {
        if (getGameData().isPlayerForfeit(agent)) return;
        if (!isPlayerOutsideFFRadius(server, agent, ffRadiusSqr, center)) {
            getGameData().getForceFFSafeTimeMap().put(agent.getId(), getAge());
            return;
        }
        int forceFFSafeTime = 0;
        if (getGameData().getForceFFSafeTimeMap().containsKey(agent.getId()))
            forceFFSafeTime = getGameData().getForceFFSafeTimeMap().get(agent.getId());
        int timeDiff = getAge() - forceFFSafeTime;
        if (timeDiff > ffWarnTime) {
            Component message = UtilMCText.literal("Have been outside the Forfeit Radius for too long.").setStyle(RED);
            agent.sendMessage(server, message);
            agent.forfeitRound(server);
        } else {
            Component message = UtilMCText.literal("WARNING: You are outside the Forfeit Radius! " +
                    "If you don't get inside in " + ((ffWarnTime - timeDiff) / 20) + " seconds " +
                    "you will automatically forfeit! Go to ["+(int)center.x+","+(int)center.y+","+(int)center.z+"]")
                    .setStyle(RED);
            agent.sendMessage(server, message);
        }
    }

    protected boolean isPlayerOutsideFFRadius(MinecraftServer server, PlayerAgent agent, int ffRadiusSqr, Vec3 center) {
        ServerPlayer player = agent.getPlayer(server);
        if (player == null) return false;
        double distanceSqr = player.distanceToSqr(center);
        return distanceSqr > ffRadiusSqr;
    }
}
