package com.onewhohears.minigames.minigame.data;

import com.onewhohears.minigames.entity.FlagEntity;
import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.phase.buyattackrounds.*;
import com.onewhohears.minigames.minigame.phase.flag.KillFlagAttackPhase;
import com.onewhohears.minigames.minigame.phase.flag.KillFlagBuyPhase;
import com.onewhohears.onewholibs.util.UtilMCText;
import com.onewhohears.onewholibs.util.UtilParse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KillFlagData extends BuyAttackData {

    public static KillFlagData createKillFlagMatch(String instanceId, String gameTypeId) {
        KillFlagData game = new KillFlagData(instanceId, gameTypeId);
        game.setPhases(new BuyAttackSetupPhase<>(game),
                new KillFlagBuyPhase<>(game),
                new KillFlagAttackPhase<>(game),
                new BuyAttackAttackEndPhase<>(game),
                new BuyAttackEndPhase<>(game));
        game.addKits("standard", "builder", "archer");
        game.addShops("survival");
        return game;
    }

    private final Set<String> defenders = new HashSet<>();
    private final Set<String> attackers = new HashSet<>();

    public KillFlagData(String instanceId, String gameTypeId) {
        super(instanceId, gameTypeId);
        this.canAddTeams = true;
        this.canAddIndividualPlayers = false;
        this.requiresSetRespawnPos = true;
        this.initialLives = 1;
        this.roundsToWin = 3;
        this.buyTime = 600;
    }

    @Override
    public void onGameStart(MinecraftServer server) {
        super.onGameStart(server);
    }

    public void resetFlags(MinecraftServer server) {
        discardAllFlags();
        for (String id : defenders) {
            GameAgent team = getAgentById(id);
            if (team == null) continue;
            if (!FlagEntity.spawnFlag(this, team, server.getLevel(Level.OVERWORLD))) {
                LOGGER.error("Could not spawn {}'s flag! The game is now soft locked! Reset the mini-game " +
                        "and consider force loading the chunk! If that still doesn't work contact the dev!", id);
                chatToAllPlayers(server, UtilMCText.literal("Could not spawn "+id+"'s flag! The game is now " +
                        "soft locked! Reset the mini-game and consider force loading "+id+"'s spawn position! " +
                        "If that still doesn't work contact the dev!"), SoundEvents.VILLAGER_NO);
            }
        }
    }

    public List<GameAgent> getLivingAttackers() {
        return getLivingAgents().stream().filter(agent -> attackers.contains(agent.getId())).toList();
    }

    public void awardAllAttackers() {
        attackers.forEach(id -> {
            GameAgent agent = getAgentById(id);
            if (agent == null) return;
            agent.addScore(1);
        });
    }

    @Override
    public CompoundTag save() {
        CompoundTag nbt = super.save();
        UtilParse.writeStrings(nbt, "defenders", defenders);
        UtilParse.writeStrings(nbt, "attackers", attackers);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        defenders.addAll(UtilParse.readStringSet(nbt, "defenders"));
        attackers.addAll(UtilParse.readStringSet(nbt, "attackers"));
    }

    @Override
    public void reset(MinecraftServer server) {
        super.reset(server);
    }

    public boolean addAttacker(String id) {
        if (!hasAgentById(id)) return false;
        defenders.remove(id);
        attackers.add(id);
        return true;
    }

    public boolean addDefender(String id) {
        if (!hasAgentById(id)) return false;
        attackers.remove(id);
        defenders.add(id);
        return true;
    }

    public boolean removeAttacker(String id) {
        attackers.remove(id);
        return true;
    }

    public boolean removeDefender(String id) {
        defenders.remove(id);
        return true;
    }

    @Override
    protected @Nullable String getAdditionalStartFailReasons(MinecraftServer server) {
        String other = super.getAdditionalStartFailReasons(server);
        if (other != null) return other;
        if (defenders.isEmpty()) return "There must be at least one defenders team!";
        if (attackers.isEmpty()) return "There must be at least one attackers team!";
        return null;
    }
}
