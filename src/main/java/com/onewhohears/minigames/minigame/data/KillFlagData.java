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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.atomic.AtomicBoolean;

public class KillFlagData extends AttackDefendData {

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

    public int banAllBlocksRadius = 2; // 0 to disable
    public int blockBlackListRadius = 0; // 0 to disable
    public int blockWhiteListRadius = 0; // 0 to disable

    public KillFlagData(String instanceId, String gameTypeId) {
        super(instanceId, gameTypeId);
        this.canAddTeams = true;
        this.canAddIndividualPlayers = false;
        this.requiresSetRespawnPos = true;
        this.defaultInitialLives = 1;
        this.roundsToWin = 3;
        this.buyTime = 600;
    }

    @Override
    public void onGameStart(MinecraftServer server) {
        super.onGameStart(server);
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
    public CompoundTag save() {
        CompoundTag nbt = super.save();
        nbt.putInt("banAllBlocksRadius", banAllBlocksRadius);
        nbt.putInt("blockBlackListRadius", blockBlackListRadius);
        nbt.putInt("blockWhiteListRadius", blockWhiteListRadius);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        banAllBlocksRadius = nbt.getInt("banAllBlocksRadius");
        blockBlackListRadius = nbt.getInt("blockBlackListRadius");
        blockWhiteListRadius = nbt.getInt("blockWhiteListRadius");
    }

    @Override
    public void reset(MinecraftServer server) {
        super.reset(server);
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
