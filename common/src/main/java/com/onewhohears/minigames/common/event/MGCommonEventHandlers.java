package com.onewhohears.minigames.common.event;

import com.mojang.brigadier.CommandDispatcher;
import com.onewhohears.minigames.command.admin.MiniGameAdminCommands;
import com.onewhohears.minigames.command.all.MiniGameAllCommands;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.entity.FlagEntity;
import com.onewhohears.minigames.init.MiniGameEntities;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.minigames.util.CMGUtil;
import com.onewhohears.onewholibs.common.event.OWLEvents;
import com.onewhohears.onewholibs.data.jsonpreset.JsonPresetReloadListener;
import com.onewhohears.onewholibs.util.UtilEntity;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MGCommonEventHandlers {

    public static void init() {
        EntityEvent.LIVING_HURT.register(MGCommonEventHandlers::livingHurtEvent);
        EntityEvent.LIVING_DEATH.register(MGCommonEventHandlers::livingDeathEvent);
        BlockEvent.PLACE.register(MGCommonEventHandlers::placeBlockEvent);
        PlayerEvent.PLAYER_RESPAWN.register(MGCommonEventHandlers::playerRespawn);
        PlayerEvent.PLAYER_JOIN.register(MGCommonEventHandlers::playerJoin);
        PlayerEvent.PLAYER_QUIT.register(MGCommonEventHandlers::playerQuit);
        TickEvent.SERVER_POST.register(MGCommonEventHandlers::serverTick);
        LifecycleEvent.SERVER_STARTED.register(MGCommonEventHandlers::serverStarted);
        OWLEvents.GET_JSON_PRESET_LISTENERS.register(MGCommonEventHandlers::getJsonPresetListeners);
        EntityAttributeRegistry.register(MiniGameEntities.FLAG, FlagEntity::createAttributes);
        CommandRegistrationEvent.EVENT.register(MGCommonEventHandlers::registerCommands);
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher,
                                        CommandBuildContext context,
                                        Commands.CommandSelection selection) {
        new MiniGameAdminCommands(dispatcher);
        new MiniGameAllCommands(dispatcher);
    }

    public static void getJsonPresetListeners(List<JsonPresetReloadListener<?>> event) {
        event.add(MiniGameKitsManager.get());
        event.add(MiniGameShopsManager.get());
    }

    public static void serverStarted(MinecraftServer server) {
        MiniGameManager.serverStarted(server);
    }

    public static void serverTick(MinecraftServer server) {
        MiniGameManager.get().serverTick(server);
    }

    public static void playerJoin(ServerPlayer player) {
        if (UtilEntity.getLevel(player).isClientSide()) return;
        List<PlayerAgent> agents = MiniGameManager.get().getActiveGamePlayerAgents(player);
        if (agents.isEmpty() && MiniGameManager.get().isForceNonMemberSpectator()) {
            player.setGameMode(GameType.SPECTATOR);
        } else {
            for (PlayerAgent agent : agents)
                if (agent.shouldRunOnRespawn())
                    agent.onLogIn(player.getServer());
        }
    }

    public static void playerQuit(ServerPlayer player) {
        if (UtilEntity.getLevel(player).isClientSide()) return;
        for (PlayerAgent agent : MiniGameManager.get().getActiveGamePlayerAgents(player))
            if (agent.shouldRunOnRespawn())
                agent.onLogOut(player.getServer());
    }

    public static void playerRespawn(ServerPlayer player, boolean conqueredEnd) {
        if (UtilEntity.getLevel(player).isClientSide() || conqueredEnd) return;
        for (PlayerAgent agent : MiniGameManager.get().getActiveGamePlayerAgents(player))
            if (agent.shouldRunOnRespawn())
                agent.onRespawn(player.getServer());
    }

    public static EventResult placeBlockEvent(Level level, BlockPos pos, BlockState state, @Nullable Entity placer) {
        if (placer == null) return EventResult.pass();
        if (UtilEntity.getLevel(placer).isClientSide()) return EventResult.pass();
        if (!(placer instanceof ServerPlayer player)) return EventResult.pass();
        BlockItem blockItem = null;
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (player.getUsedItemHand() == InteractionHand.MAIN_HAND &&
                player.getMainHandItem().getItem() instanceof BlockItem item) {
            blockItem = item;
        } else if (player.getOffhandItem().getItem() instanceof BlockItem item) {
            blockItem = item;
            hand = InteractionHand.OFF_HAND;
        }
        if (blockItem == null) return EventResult.pass();
        for (PlayerAgent agent : MiniGameManager.get().getActiveGamePlayerAgents(player)) {
            if (!agent.allowBlockPlace(player.getServer(), pos, blockItem.getBlock())) {
                CMGUtil.forceHeldItemSync(player, hand);
                return EventResult.interruptDefault();
            }
        }
        return EventResult.pass();
    }

    public static EventResult livingHurtEvent(LivingEntity entity, DamageSource source, float amount) {
        if (UtilEntity.getLevel(entity).isClientSide()) return EventResult.pass();
        if (entity instanceof ServerPlayer player) {
            for (PlayerAgent agent : MiniGameManager.get().getActiveGamePlayerAgents(player)) {
                if (!agent.getGameData().getCurrentPhase().allowPVP()) {
                    return EventResult.interruptDefault();
                }
            }
        } else if (entity instanceof FlagEntity flag) {
            MiniGameData data = flag.getGameData();
            if (data == null) return EventResult.pass();
            if (!data.getCurrentPhase().allowPVP()) {
                return EventResult.interruptDefault();
            }
        }
        return EventResult.pass();
    }

    public static EventResult livingDeathEvent(LivingEntity entity, DamageSource source) {
        if (UtilEntity.getLevel(entity).isClientSide()) return EventResult.pass();
        if (entity instanceof FlagEntity flag) {
            flag.onDeath(source);
            return EventResult.pass();
        }
        if (!(entity instanceof ServerPlayer player)) return EventResult.pass();
        for (PlayerAgent agent : MiniGameManager.get().getActiveGamePlayerAgents(player)) {
            if (agent.shouldRunOnDeath()) {
                agent.onDeath(player.getServer(), source);
            }
        }
        return EventResult.pass();
    }

}
