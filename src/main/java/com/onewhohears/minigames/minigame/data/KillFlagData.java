package com.onewhohears.minigames.minigame.data;

import com.onewhohears.minigames.entity.FlagEntity;
import com.onewhohears.minigames.init.CMGTags;
import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.phase.buyattackrounds.*;
import com.onewhohears.minigames.minigame.phase.flag.KillFlagAttackPhase;
import com.onewhohears.minigames.minigame.phase.flag.KillFlagBuyPhase;
import com.onewhohears.onewholibs.util.UtilMCText;
import com.onewhohears.onewholibs.util.math.UtilGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.onewhohears.minigames.minigame.param.MiniGameParamTypes.*;

public class KillFlagData extends AttackDefendData {

    public static KillFlagData createKillFlagMatch(String instanceId, String gameTypeId) {
        KillFlagData game = new KillFlagData(instanceId, gameTypeId);
        game.setPhases(new BuyAttackSetupPhase<>(game),
                new KillFlagBuyPhase<>(game),
                new KillFlagAttackPhase<>(game),
                new BuyAttackAttackEndPhase<>(game),
                new BuyAttackEndPhase<>(game));
        game.setParam(CAN_ADD_PLAYERS, false);
        game.setParam(CAN_ADD_TEAMS, true);
        game.setParam(REQUIRE_SET_SPAWN, true);
        game.setParam(USE_WORLD_BORDER, false);
        game.setParam(DEFAULT_LIVES, 1);
        game.setParam(ROUNDS_TO_WIN, 3);
        game.addKits("standard", "builder", "archer");
        game.addShops("survival");
        game.getParam(ATTACKER_SHOPS).add("survival");
        game.getParam(DEFENDER_SHOPS).add("survival");
        return game;
    }

    public KillFlagData(String instanceId, String gameTypeId) {
        super(instanceId, gameTypeId);
    }

    public void resetFlags(MinecraftServer server) {
        discardAllFlags();
        for (GameAgent agent : getDefenders()) {
            if (!FlagEntity.spawnFlag(this, agent, server.getLevel(Level.OVERWORLD))) {
                LOGGER.error("Could not spawn {}'s flag! The game is now soft locked! Reset the mini-game " +
                        "and consider force loading the chunk! If that still doesn't work contact the dev!", agent.getId());
                chatToAllPlayers(server, UtilMCText.literal("Could not spawn "+agent.getId()+"'s flag! The game is now " +
                        "soft locked! Reset the mini-game and consider force loading "+agent.getId()+"'s spawn position! " +
                        "If that still doesn't work contact the dev!"), SoundEvents.VILLAGER_NO);
            }
        }
    }

    @Override
    public void reset(MinecraftServer server) {
        super.reset(server);
    }

    @Override
    public boolean allowBlockPlace(PlayerAgent agent, MinecraftServer server, BlockPos pos, Block block) {
        Optional<Holder.Reference<Block>> optional = ForgeRegistries.BLOCKS.getDelegate(block);
        if (optional.isEmpty()) return true;
        Holder.Reference<Block> holder = optional.get();
        int all = getIntParam(BAN_ALL_BLOCKS_RADIUS)^2;
        int white = getIntParam(WHITE_LIST_BLOCKS_RADIUS)^2;
        int black = getIntParam(BLACK_LIST_BLOCKS_RADIUS)^2;
        AtomicBoolean allow = new AtomicBoolean(true);
        Vec3 vpos = UtilGeometry.toVec3(pos);
        forEachFlag(flag -> {
            if (!allow.get()) return;
            double distSqr = flag.distanceToSqr(vpos);
            if (distSqr <= all) {
                allow.set(false);
                agent.consumeForPlayer(server, player -> player.sendSystemMessage(UtilMCText.literal(
                        "Cannot place blocks in the NO BLOCKS range of the flag!").setStyle(RED)));
                return;
            }
            if (distSqr <= white && !holder.is(CMGTags.Blocks.FLAG_PLACE_WHITE_LIST)) {
                allow.set(false);
                agent.consumeForPlayer(server, player -> player.sendSystemMessage(UtilMCText.literal(
                        "Cannot place this block in the WHITE LIST range of the flag!").setStyle(RED)));
                return;
            }
            if (distSqr <= black && holder.is(CMGTags.Blocks.FLAG_PLACE_BLACK_LIST)) {
                allow.set(false);
                agent.consumeForPlayer(server, player -> player.sendSystemMessage(UtilMCText.literal(
                        "Cannot place this block in the BLACK LIST range of the flag!").setStyle(RED)));
                return;
            }
        });
        return allow.get();
    }

    @Override
    protected void registerParams() {
        super.registerParams();
        registerParam(BAN_ALL_BLOCKS_RADIUS);
        registerParam(BLACK_LIST_BLOCKS_RADIUS);
        registerParam(WHITE_LIST_BLOCKS_RADIUS);
    }
}
