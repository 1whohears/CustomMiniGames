package com.onewhohears.minigames.minigame.param;

import com.mojang.brigadier.context.CommandContext;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.data.AttackDefendData;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriFunction;

public final class MiniGameParamTypes {

    // All Mini Games
    public static final BoolParamType CAN_ADD_PLAYERS = new BoolParamType("canAddIndividualPlayers", false);
    public static final BoolParamType CAN_ADD_TEAMS = new BoolParamType("canAddTeams", false);
    public static final BoolParamType CLEAR_ON_START = new BoolParamType("clearOnStart", false);
    public static final BoolParamType ALLOW_ALWAYS_SHOP = new BoolParamType("allowAlwaysShop", false);
    public static final BoolParamType FORCE_NON_MEMBER_SPEC = new BoolParamType("forceNonMemberSpectator", false);
    public static final BoolParamType REQUIRE_SET_SPAWN = new BoolParamType("requiresSetRespawnPos", false);
    public static final BoolParamType USE_WORLD_BORDER = new BoolParamType("worldBorderDuringGame", false);
    public static final IntParamType DEFAULT_LIVES = new IntParamType("defaultInitialLives", 3, 1, 1000000) {
        @Override
        protected TriFunction<CommandContext<CommandSourceStack>, MiniGameData, Integer, Boolean> getSetterApplier() {
            return (context, gameData, value) -> {
                if (!gameData.setParam(this, value)) return false;
                gameData.setAllAgentInitialLives(value);
                return true;
            };
        }
    };
    public static final IntParamType MONEY_PER_ROUND = new IntParamType("moneyPerRound", 10, 0, 640);
    public static final DoubleParamType WORLD_BORDER_SIZE = new DoubleParamType("gameBorderSize", 1000d, 1, 9999999);
    public static final FloatParamType WATER_FOOD_EXHAUSTION_RATE = new FloatParamType("waterFoodExhaustionRate", 0f, 0, 40f);
    public static final Vec3ParamType GAME_CENTER = new Vec3ParamType("gameCenter", Vec3.ZERO);
    // Buy Attack Phase Games
    public static final BoolParamType ALLOW_BUY_PHASE_RESPAWN = new BoolParamType("allowRespawnInBuyPhase", true);
    public static final BoolParamType ALLOW_PVP_BUY_PHASE = new BoolParamType("allowPvpInBuyPhase", false);
    public static final IntParamType BUY_TIME = new IntParamType("buyTime", 900, 0, 1000000);
    public static final IntParamType ATTACK_TIME = new IntParamType("attackTime", 6000, 0, 1000000);
    public static final IntParamType ATTACK_END_TIME = new IntParamType("attackEndTime", 200, 0, 1000000);
    public static final IntParamType ROUNDS_TO_WIN = new IntParamType("roundsToWin", 3, 1, 1000000);
    public static final IntParamType BUY_RADIUS = new IntParamType("buyRadius", 24, 1, 1000000);
    // Attack Defend Data
    public static final BoolParamType ATTACKERS_SHARE_LIVES = new BoolParamType("attackersShareLives", false);
    public static final StringSetParamType DEFENDERS = new StringSetParamType("defenders") {
        @Override
        protected ListParamModifier getAdderApplier() {
            return (context, gameData, list, value) -> {
                if (!(gameData instanceof AttackDefendData data)) return false;
                return data.addDefender(value);
            };
        }
    };
    public static final StringSetParamType ATTACKERS = new StringSetParamType("attackers") {
        @Override
        protected ListParamModifier getAdderApplier() {
            return (context, gameData, list, value) -> {
                if (!(gameData instanceof AttackDefendData data)) return false;
                return data.addAttacker(value);
            };
        }
    };
    public static final StringSetParamType DEFENDER_SHOPS = new StringSetParamType("defenderShops") {
        @Override
        protected ListParamModifier getAdderApplier() {
            return (context, gameData, list, value) -> {
                gameData.addShops(value);
                list.add(value);
                return true;
            };
        }
    };
    public static final StringSetParamType ATTACKER_SHOPS = new StringSetParamType("attackerShops") {
        @Override
        protected ListParamModifier getAdderApplier() {
            return (context, gameData, list, value) -> {
                gameData.addShops(value);
                list.add(value);
                return true;
            };
        }
    };

    /**
     * called in {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}
     * register all built in games param types here
     */
    public static void registerGameParamTypes() {
        MiniGameManager.registerGameParamType(CAN_ADD_PLAYERS);
        MiniGameManager.registerGameParamType(CAN_ADD_TEAMS);
        MiniGameManager.registerGameParamType(CLEAR_ON_START);
        MiniGameManager.registerGameParamType(ALLOW_ALWAYS_SHOP);
        MiniGameManager.registerGameParamType(FORCE_NON_MEMBER_SPEC);
        MiniGameManager.registerGameParamType(REQUIRE_SET_SPAWN);
        MiniGameManager.registerGameParamType(USE_WORLD_BORDER);
        MiniGameManager.registerGameParamType(DEFAULT_LIVES);
        MiniGameManager.registerGameParamType(MONEY_PER_ROUND);
        MiniGameManager.registerGameParamType(WORLD_BORDER_SIZE);
        MiniGameManager.registerGameParamType(WATER_FOOD_EXHAUSTION_RATE);
        MiniGameManager.registerGameParamType(GAME_CENTER);
        MiniGameManager.registerGameParamType(ALLOW_BUY_PHASE_RESPAWN);
        MiniGameManager.registerGameParamType(ALLOW_PVP_BUY_PHASE);
        MiniGameManager.registerGameParamType(BUY_TIME);
        MiniGameManager.registerGameParamType(ATTACK_TIME);
        MiniGameManager.registerGameParamType(ATTACK_END_TIME);
        MiniGameManager.registerGameParamType(ROUNDS_TO_WIN);
        MiniGameManager.registerGameParamType(BUY_RADIUS);
        MiniGameManager.registerGameParamType(ATTACKERS_SHARE_LIVES);
        MiniGameManager.registerGameParamType(DEFENDERS);
        MiniGameManager.registerGameParamType(ATTACKERS);
        MiniGameManager.registerGameParamType(DEFENDER_SHOPS);
        MiniGameManager.registerGameParamType(ATTACKER_SHOPS);
    }
}
