package com.onewhohears.minigames.minigame.data;

import com.onewhohears.minigames.minigame.agent.GameAgent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.onewhohears.minigames.minigame.param.MiniGameParamTypes.*;

public class AttackDefendData extends BuyAttackData {

    public AttackDefendData(String instanceId, String gameTypeId) {
        super(instanceId, gameTypeId);
    }

    @Override
    public CompoundTag save() {
        return super.save();
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
    }

    public List<GameAgent> getLivingAttackers() {
        List<GameAgent> list = new ArrayList<>();
        Set<String> attackers = getStringSetParam(ATTACKERS);
        for (GameAgent agent : getLivingAgents())
            if (attackers.contains(agent.getId())) 
                list.add(agent);
        return list;
    }

    public void awardAllAttackers() {
        getStringSetParam(ATTACKERS).forEach(id -> {
            GameAgent agent = getAgentById(id);
            if (agent == null) return;
            agent.addScore(1);
        });
    }

    public void awardAllDefenders() {
        getStringSetParam(DEFENDERS).forEach(id -> {
            GameAgent agent = getAgentById(id);
            if (agent == null) return;
            agent.addScore(1);
        });
    }

    public boolean addAttacker(String id) {
        if (!hasAgentById(id)) return false;
        getStringSetParam(DEFENDERS).remove(id);
        getStringSetParam(ATTACKERS).add(id);
        GameAgent agent = getAgentById(id);
        if (agent == null) return false;
        agent.setShareLives(getBooleanParam(ATTACKERS_SHARE_LIVES));
        return true;
    }

    public boolean addDefender(String id) {
        if (!hasAgentById(id)) return false;
        getStringSetParam(ATTACKERS).remove(id);
        getStringSetParam(DEFENDERS).add(id);
        return true;
    }

    public boolean isAttacker(String id) {
        return getStringSetParam(ATTACKERS).contains(id);
    }

    public boolean isDefender(String id) {
        return getStringSetParam(DEFENDERS).contains(id);
    }

    public boolean isAttackerShop(String id) {
        return getStringSetParam(ATTACKER_SHOPS).contains(id);
    }

    public boolean isDefenderShop(String id) {
        return getStringSetParam(DEFENDER_SHOPS).contains(id);
    }

    @Override
    protected @Nullable String getAdditionalStartFailReasons(MinecraftServer server) {
        String other = super.getAdditionalStartFailReasons(server);
        if (other != null) return other;
        if (getStringSetParam(DEFENDERS).isEmpty()) return "There must be at least one defenders team!";
        if (getStringSetParam(ATTACKERS).isEmpty()) return "There must be at least one attackers team!";
        return null;
    }

    public List<GameAgent> getAttackers() {
        List<GameAgent> list = new ArrayList<>();
        for (String id : getStringSetParam(ATTACKERS)) {
            GameAgent agent = getAgentById(id);
            if (agent == null) continue;
            list.add(agent);
        }
        return list;
    }

    public List<GameAgent> getDefenders() {
        List<GameAgent> list = new ArrayList<>();
        for (String id : getStringSetParam(DEFENDERS)) {
            GameAgent agent = getAgentById(id);
            if (agent == null) continue;
            list.add(agent);
        }
        return list;
    }

    public boolean canOpenAttackDefendShop(GameAgent agent, String shop) {
        if (isAttacker(agent.getId())) return isAttackerShop(shop);
        else if (isDefender(agent.getId())) return isDefenderShop(shop);
        return hasShop(shop);
    }

    @Override
    protected void registerParams() {
        super.registerParams();
        registerParam(ATTACKERS_SHARE_LIVES);
        registerParam(DEFENDERS);
        registerParam(ATTACKERS);
        registerParam(DEFENDER_SHOPS);
        registerParam(ATTACKER_SHOPS);
    }
}
