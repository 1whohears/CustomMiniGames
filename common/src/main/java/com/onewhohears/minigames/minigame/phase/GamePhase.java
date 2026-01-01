package com.onewhohears.minigames.minigame.phase;

import com.mojang.logging.LogUtils;
import com.onewhohears.minigames.minigame.agent.GameAgent;
import com.onewhohears.minigames.minigame.agent.PlayerAgent;
import com.onewhohears.minigames.minigame.agent.TeamAgent;
import com.onewhohears.minigames.minigame.condition.PhaseExitCondition;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.onewholibs.util.UtilMCText;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static com.onewhohears.minigames.minigame.data.MiniGameData.RED;

public abstract class GamePhase<T extends MiniGameData> {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final String id;
	private final T gameData;
	private final PhaseExitCondition<T>[] exitConditions;
	private int age;

	private boolean announceTimeLeft = false;
	private Function<T, Integer> maxTime = data->0;
	
	@SafeVarargs
	protected GamePhase(String id, T gameData, PhaseExitCondition<T>...exitConditions) {
		this.id = id;
		this.gameData = gameData;
		this.exitConditions = exitConditions;
	}
	
	public CompoundTag save() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("id", id);
		nbt.putInt("age", age);
		return nbt;
	}
	
	public void load(CompoundTag tag) {
		age = tag.getInt("age");
	}
	
	public void tickPhase(MinecraftServer server) {
		if (announceTimeLeft) announceTimeLeft(server);
		++age;
		checkExitConditions(server);
	}

	public void announceTimeLeft(MinecraftServer server) {
		int ticksRemaining = maxTime.apply(getGameData()) - age;
		if (ticksRemaining % 20 != 0) return;
		int secondsRemaining = ticksRemaining / 20;
		int minutes = -1, seconds = -1;
		if (secondsRemaining == 1800) minutes = 30;
		else if (secondsRemaining == 1500) minutes = 25;
		else if (secondsRemaining == 1200) minutes = 20;
		else if (secondsRemaining == 900) minutes = 15;
		else if (secondsRemaining == 600) minutes = 10;
		else if (secondsRemaining == 300) minutes = 5;
		else if (secondsRemaining == 240) minutes = 4;
		else if (secondsRemaining == 180) minutes = 3;
		else if (secondsRemaining == 120) minutes = 2;
		else if (secondsRemaining == 60) seconds = 60;
		else if (secondsRemaining == 30) seconds = 30;
		else if (secondsRemaining == 10) seconds = 10;
		else if (secondsRemaining == 5) seconds = 5;
		else if (secondsRemaining == 4) seconds = 4;
		else if (secondsRemaining == 3) seconds = 3;
		else if (secondsRemaining == 2) seconds = 2;
		else if (secondsRemaining == 1) seconds = 1;
		Component message = null;
		if (minutes != -1) message = UtilMCText.literal(minutes+" minutes left!").setStyle(MiniGameData.AQUA);
		else if (seconds != -1) message = UtilMCText.literal(seconds+" seconds left!").setStyle(MiniGameData.AQUA);
		else return;
		getGameData().chatToAllPlayers(server, message, SoundEvents.UI_BUTTON_CLICK.value());
	}
	
	public void tickPlayerAgent(MinecraftServer server, PlayerAgent agent) {
		ServerPlayer player = agent.getPlayer(server);
		if (player == null) return;
		setGameMode(agent, player);
        tickFoodExhaustion(agent, player);
	}

    public void setGameMode(@NotNull PlayerAgent agent, @NotNull ServerPlayer player) {
        if (player.gameMode.isCreative()) return;
        if (!isSetupPhase() && agent.isDead()) setGameMode(player, GameType.SPECTATOR);
        else if (isForceSurvivalMode()) setGameMode(player, GameType.SURVIVAL);
        else if (isForceAdventureMode()) setGameMode(player, GameType.ADVENTURE);
    }

    public void setGameMode(@NotNull ServerPlayer player, GameType gameMode) {
        if (player.gameMode.getGameModeForPlayer() == gameMode) return;
        System.out.println(getId()+" force adventure "+isForceAdventureMode()+" survival "+isForceSurvivalMode());
        System.out.println("GAMEMODE PRE "+player.gameMode.getGameModeForPlayer()+" -> "+gameMode);
        boolean success = player.setGameMode(gameMode);
        System.out.println("GAMEMODE POST "+player.gameMode.getGameModeForPlayer()+" "+success);
        //new Exception().printStackTrace();
    }

    protected void tickFoodExhaustion(@NotNull PlayerAgent agent, @NotNull ServerPlayer player) {
        if (getGameData().getWaterFoodExhaustionRate() > 0 && player.isInWater() && hungerPlayersInWater()) {
            player.causeFoodExhaustion(getGameData().getWaterFoodExhaustionRate());
        }
    }

	public void tickTeamAgent(MinecraftServer server, TeamAgent agent) {
		
	}
	
	public void onReset(MinecraftServer server) {
        LOGGER.debug("PHASE RESET {}", id);
		age = 0;
	}
	
	public void onStart(MinecraftServer server) {
        LOGGER.debug("PHASE START {}", id);
		updateWorldBorder(server);
        getGameData().resetForfeiters();
	}
	
	public void onStop(MinecraftServer server) {
        LOGGER.debug("PHASE STOP {}", id);
	}
	
	public void checkExitConditions(MinecraftServer server) {
		if (exitConditions == null) return;
		for (PhaseExitCondition<T> con : exitConditions) {
			if (con.shouldExit(server, this)) {
				con.onExit(server, this);
				gameData.changePhase(server, con.getNextPhaseId());
				return;
			}
		}
	}
	
	public void updateWorldBorder(MinecraftServer server) {
		WorldBorder border = server.overworld().getWorldBorder();
		if (hasWorldBorder()) {
			double size = getWorldBorderSize();
			double endSize = getWorldBorderEndSize();
			long time = getWorldBorderChangeTime();
			if (size != -1 && time != -1 && endSize != -1) {
				border.setSize(size);
				border.lerpSizeBetween(endSize, size, time);
			} else if (size != -1 && time != -1) {
				border.lerpSizeBetween(border.getSize(), size, time);
			} else if (size != -1) {
				border.setSize(size);
			}
			Vec3 center = getGameData().getGameCenter();
			border.setCenter(center.x, center.z);
		} else border.applySettings(WorldBorder.DEFAULT_SETTINGS);
	}
	
	public String getId() {
		return id;
	}
	
	public T getGameData() {
		return gameData;
	}
	
	public PhaseExitCondition<T>[] getExitConditions() {
		return exitConditions;
	}
	
	public int getAge() {
		return age;
	}
	
	public boolean isSetupPhase() {
		return false;
	}

	public boolean isForceAdventureMode() {
		return getGameData().forceAdventureDuringGame();
	}
	
	public boolean isForceSurvivalMode() {
		return false;
	}
	
	public boolean hasWorldBorder() {
		return false;
	}
	
	public double getWorldBorderEndSize() {
		return -1;
	}
	
	public double getWorldBorderSize() {
		return -1;
	}
	
	public long getWorldBorderChangeTime() {
		return -1;
	}
	
	@Override
	public String toString() {
		return "id:"+getId()+",age:"+getAge();
	}
	
	public boolean shouldEndGame() {
		return false;
	}

	public void onPlayerDeath(PlayerAgent player, MinecraftServer server, @Nullable DamageSource source) {

	}

	public void onPlayerRespawn(PlayerAgent player, MinecraftServer server) {
		player.refillPlayerKit(server);
	}

	public void onLogIn(PlayerAgent player, MinecraftServer server) {
	}

	public void onLogOut(PlayerAgent player, MinecraftServer server) {
	}

	public boolean canAgentUseKit(GameAgent agent, String kit) {
		return getGameData().hasKit(kit);
	}

	public boolean canAgentOpenShop(MinecraftServer server, GameAgent agent, String shop) {
		return getGameData().hasShop(shop);
	}

	public boolean allowPVP() {
		return true;
	}

	protected void setCountDown(Function<T,Integer> maxPhaseAgeGetter) {
		announceTimeLeft = true;
		maxTime = maxPhaseAgeGetter;
	}

	public boolean looseLiveOnDeath(GameAgent gameAgent, MinecraftServer server) {
		return true;
	}

	public boolean allowBlockPlace(PlayerAgent agent, MinecraftServer server, BlockPos pos, Block placedBlock) {
		return getGameData().allowBlockPlace(agent, server, pos, placedBlock);
	}

	public boolean hungerPlayersInWater() {
		return true;
	}

    public boolean isBuyPhase() {
		return false;
    }

	public boolean isAttackPhase() {
		return false;
	}

    public boolean canPlayerForfeit(PlayerAgent agent) {
        return isAttackPhase();
    }

    public void onPlayerForfeit(MinecraftServer server, PlayerAgent agent) {
        if (!canPlayerForfeit(agent)) {
            agent.sendMessage(server, "You can only forfeit during an attack phase.");
            return;
        }
        if (getGameData().isPlayerForfeit(agent)) {
            agent.sendMessage(server, "You already forfeited! Please wait for your team to agree.");
            return;
        }
        getGameData().setPlayerForfeit(agent);
        TeamAgent team = agent.getTeamAgent();
        if (team == null) {
            Component message = UtilMCText.empty().append(agent.getDisplayName(server))
                    .append(" has voted to forfeit this round.").setStyle(RED);
            getGameData().chatToAllPlayers(server, message, SoundEvents.VILLAGER_HURT);
            agent.onForfeit(server);
            return;
        }
        if (getGameData().isTeamForfeit(team)) {
            Component message = UtilMCText.literal("All members of team ").append(team.getDisplayName(server))
                            .append(" have voted to forfeit this round.").setStyle(RED);
            getGameData().chatToAllPlayers(server, message, SoundEvents.VILLAGER_HURT);
            team.onForfeit(server);
            return;
        }
        Component message = UtilMCText.empty().append(agent.getDisplayName(server))
                .append(" has voted to forfeit this round.").setStyle(RED);
        team.sendMessage(server, message, SoundEvents.VILLAGER_NO);
    }

    public void forEachPlayer(MinecraftServer server, BiPredicate<T,PlayerAgent> predicate,
                              TriConsumer<T,PlayerAgent,ServerPlayer> consumer) {
        List<PlayerAgent> players = getGameData().getAllPlayerAgents();
        for (PlayerAgent agent : players) {
            ServerPlayer player = agent.getPlayer(server);
            if (player == null) continue;
            if (!predicate.test(getGameData(), agent)) continue;
            consumer.accept(getGameData(), agent, player);
        }
    }
}
