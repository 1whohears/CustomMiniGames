package com.onewhohears.minigames.minigame.data;

import com.onewhohears.minigames.entity.FlagEntity;
import com.onewhohears.minigames.init.CMGTags;
import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.phase.buyattackrounds.*;
import com.onewhohears.minigames.minigame.phase.flag.KillFlagAttackPhase;
import com.onewhohears.minigames.minigame.phase.flag.KillFlagBuyPhase;
import com.onewhohears.onewholibs.util.UtilMCText;
import com.onewhohears.onewholibs.util.UtilParse;
import com.onewhohears.onewholibs.util.math.UtilGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class KillFlagData extends BuyAttackData {

    public static KillFlagData createKillFlagMatch(String instanceId, String gameTypeId) {
        KillFlagData game = new KillFlagData(instanceId, gameTypeId);
        game.setPhases(new BuyAttackSetupPhase<>(game),
                new KillFlagBuyPhase<>(game),
                new KillFlagAttackPhase<>(game),
                new BuyAttackAttackEndPhase<>(game),
                new BuyAttackEndPhase<>(game));
        game.addKits("standard", "builder", "archer");
        game.addAttackerShop("survival");
        game.addDefenderShop("survival");
        return game;
    }

    private final Set<String> defenders = new HashSet<>();
    private final Set<String> attackers = new HashSet<>();
    private final Set<String> attackerShops = new HashSet<>();
    private final Set<String> defenderShops = new HashSet<>();

    public int banAllBlocksRadius = 2; // 0 to disable
    public int blockBlackListRadius = 0; // 0 to disable
    public int blockWhiteListRadius = 0; // 0 to disable

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
        List<GameAgent> list = new ArrayList<>();
        for (GameAgent agent : getLivingAgents())
            if (attackers.contains(agent.getId())) list.add(agent);
        return list;
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
        UtilParse.writeStrings(nbt, "attackerShops", attackerShops);
        UtilParse.writeStrings(nbt, "defenderShops", defenderShops);
        nbt.putInt("banAllBlocksRadius", banAllBlocksRadius);
        nbt.putInt("blockBlackListRadius", blockBlackListRadius);
        nbt.putInt("blockWhiteListRadius", blockWhiteListRadius);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        defenders.addAll(UtilParse.readStringSet(nbt, "defenders"));
        attackers.addAll(UtilParse.readStringSet(nbt, "attackers"));
        attackerShops.addAll(UtilParse.readStringSet(nbt, "attackerShops"));
        defenderShops.addAll(UtilParse.readStringSet(nbt, "defenderShops"));
        banAllBlocksRadius = nbt.getInt("banAllBlocksRadius");
        blockBlackListRadius = nbt.getInt("blockBlackListRadius");
        blockWhiteListRadius = nbt.getInt("blockWhiteListRadius");
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

    public boolean isAttacker(String id) {
        return attackers.contains(id);
    }

    public boolean isDefender(String id) {
        return defenders.contains(id);
    }

    public boolean addAttackerShop(String id) {
        addShops(id);
        defenderShops.remove(id);
        attackerShops.add(id);
        return true;
    }

    public boolean addDefenderShop(String id) {
        addShops(id);
        attackerShops.remove(id);
        defenderShops.add(id);
        return true;
    }

    public boolean removeAttackerShop(String id) {
        removeShops(id);
        attackerShops.remove(id);
        return true;
    }

    public boolean removeDefenderShop(String id) {
        removeShops(id);
        defenderShops.remove(id);
        return true;
    }

    public boolean isAttackerShop(String id) {
        return attackerShops.contains(id);
    }

    public boolean isDefenderShop(String id) {
        return defenderShops.contains(id);
    }

    @Override
    protected @Nullable String getAdditionalStartFailReasons(MinecraftServer server) {
        String other = super.getAdditionalStartFailReasons(server);
        if (other != null) return other;
        if (defenders.isEmpty()) return "There must be at least one defenders team!";
        if (attackers.isEmpty()) return "There must be at least one attackers team!";
        return null;
    }

    @Override
    public boolean allowBlockPlace(PlayerAgent agent, MinecraftServer server, BlockPos pos, Block block) {
        int all = banAllBlocksRadius * banAllBlocksRadius;
        int white = blockWhiteListRadius * blockWhiteListRadius;
        int black = blockBlackListRadius * blockBlackListRadius;
        AtomicBoolean allow = new AtomicBoolean(true);
        Vec3 vpos = UtilGeometry.toVec3(pos);
        Holder.Reference<Block> holder = block.builtInRegistryHolder();
        forEachFlag(flag -> {
            if (!allow.get()) return;
            double distSqr = flag.distanceToSqr(vpos);
            if (distSqr < all) {
                allow.set(false);
                agent.consumeForPlayer(server, player -> player.sendSystemMessage(UtilMCText.literal(
                        "Cannot place blocks in the NO BLOCKS range of the flag!").setStyle(RED)));
                return;
            }
            if (distSqr < white && !holder.is(CMGTags.Blocks.FLAG_PLACE_WHITE_LIST)) {
                allow.set(false);
                agent.consumeForPlayer(server, player -> player.sendSystemMessage(UtilMCText.literal(
                        "Cannot place this block in the WHITE LIST range of the flag!").setStyle(RED)));
                return;
            }
            if (distSqr < black && holder.is(CMGTags.Blocks.FLAG_PLACE_BLACK_LIST)) {
                allow.set(false);
                agent.consumeForPlayer(server, player -> player.sendSystemMessage(UtilMCText.literal(
                        "Cannot place this block in the BLACK LIST range of the flag!").setStyle(RED)));
                return;
            }
        });
        return allow.get();
    }

}
