package com.onewhohears.minigames.minigame.param;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.onewhohears.minigames.command.GameComArgs;
import com.onewhohears.minigames.data.kits.MiniGameKitsManager;
import com.onewhohears.minigames.data.shops.MiniGameShopsManager;
import com.onewhohears.minigames.minigame.MiniGameManager;
import com.onewhohears.minigames.minigame.data.AttackDefendData;
import com.onewhohears.minigames.minigame.data.MiniGameData;
import com.onewhohears.minigames.util.CommandUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Set;

import static net.minecraft.server.commands.FunctionCommand.SUGGEST_FUNCTION;

public final class MiniGameParamTypes {

    // All Mini Games
    public static final BoolParamType JOIN_SETUP_ONLY = new BoolParamType("onlyJoinDuringSetup", true);
    public static final BoolParamType OPEN_JOINING = new BoolParamType("allPlayersCanJoin", true);
    public static final BoolParamType OPEN_TEAMS = new BoolParamType("allowTeamSelection", true);
    public static final BoolParamType CAN_ADD_PLAYERS = new BoolParamType("canAddIndividualPlayers", false);
    public static final BoolParamType CAN_ADD_TEAMS = new BoolParamType("canAddTeams", false);
    public static final BoolParamType CLEAR_ON_START = new BoolParamType("clearOnStart", false);
    public static final BoolParamType CLEAR_ON_ROUND_CHANGE = new BoolParamType("clearOnRoundChange", false);
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
    public static final IntParamType RESPAWN_TICKS = new IntParamType("respawnTicks", 0, 0, 1000000, "ticks");
    public static final IntParamType MONEY_PER_ROUND = new IntParamType("moneyPerRound", 20, 0, 640);
    public static final DoubleParamType WORLD_BORDER_SIZE = new DoubleParamType("gameBorderSize", 1000d, 1, 9999999);
    public static final FloatParamType WATER_FOOD_EXHAUSTION_RATE = new FloatParamType("waterFoodExhaustionRate", 0f, 0, 40f);
    public static final Vec3ParamType GAME_CENTER = new Vec3ParamType("gameCenter", Vec3.ZERO) {
        @Override
        protected TriFunction<CommandContext<CommandSourceStack>, MiniGameData, Vec3, Boolean> getSetterApplier() {
            return (context, gameData, value) -> {
                if (!gameData.setParam(this, value)) return false;
                gameData.setGameCenter(value, context.getSource().getServer());
                return true;
            };
        }
    };
    public static final StringSetParamType KITS = new StringSetParamType("kits",
            CommandUtil.suggestStrings(() -> MiniGameKitsManager.get().getAllIds()),
            GameComArgs.suggestEnabledKits());
    public static final StringSetParamType SHOPS = new StringSetParamType("shops",
            CommandUtil.suggestStrings(() -> MiniGameShopsManager.get().getAllIds()),
            GameComArgs.suggestEnabledShops());
    public static final StringSetParamType EVENTS = new StringSetParamType("events",
            CommandUtil.suggestStrings(MiniGameManager::getAllEventIds),
            GameComArgs.suggestHandleableEvents());
    public static final StringSetParamType POI_TYPES = new StringSetParamType("poiTypes",
            CommandUtil.suggestStrings(MiniGameManager::getAllPoiTypeIds),
            GameComArgs.suggestHandleablePoiTypes());
    public static final FunctionSetParamType FUNCTION_ON_GAME_START = new FunctionSetParamType("functionsOnGameStart",
            SUGGEST_FUNCTION, GameComArgs.suggestNothing()) {
        @Override
        protected SuggestionProvider<CommandSourceStack> getRemoveSuggestions() {
            return GameComArgs.suggestFromSet(this);
        }
    };
    public static final FunctionSetParamType FUNCTION_ON_ROUND_START = new FunctionSetParamType("functionsOnRoundStart",
            SUGGEST_FUNCTION, GameComArgs.suggestNothing()) {
        @Override
        protected SuggestionProvider<CommandSourceStack> getRemoveSuggestions() {
            return GameComArgs.suggestFromSet(this);
        }
    };
    public static final FunctionSetParamType FUNCTION_ON_ROUND_END = new FunctionSetParamType("functionsOnRoundEnd",
            SUGGEST_FUNCTION, GameComArgs.suggestNothing()) {
        @Override
        protected SuggestionProvider<CommandSourceStack> getRemoveSuggestions() {
            return GameComArgs.suggestFromSet(this);
        }
    };
    // Buy Attack Phase Games
    public static final BoolParamType ALLOW_BUY_PHASE_RESPAWN = new BoolParamType("allowRespawnInBuyPhase", true);
    public static final BoolParamType ALLOW_PVP_BUY_PHASE = new BoolParamType("allowPvpInBuyPhase", false);
    public static final IntParamType BUY_TIME = new IntParamType("buyTime", 900, 0, 2000000, "ticks");
    public static final IntParamType ATTACK_TIME = new IntParamType("attackTime", 6000, 0, 2000000, "ticks");
    public static final IntParamType ATTACK_END_TIME = new IntParamType("attackEndTime", 200, 0, 2000000, "ticks");
    public static final IntParamType ROUNDS_TO_WIN = new IntParamType("roundsToWin", 3, 1, 1000000);
    public static final IntParamType BUY_RADIUS = new IntParamType("buyRadius", 24, -1, 1000000, "blocks");
    public static final BoolParamType SHOP_OUTSIDE_BUY_RADIUS = new BoolParamType("allowShopOutsideBuyRadius", false);
    // Attack Defend Data
    public static final BoolParamType ATTACKERS_SHARE_LIVES = new BoolParamType("attackersShareLives", false);
    public static final StringSetParamType DEFENDERS = new StringSetParamType("defenders", GameComArgs.suggestAgentNames(), GameComArgs.suggestAgentNames()) {
        @Override
        protected ListParamModifier<Set<String>, String> getAdderApplier() {
            return (context, gameData, list, value) -> {
                if (!(gameData instanceof AttackDefendData data)) return false;
                return data.addDefender(value);
            };
        }
    };
    public static final StringSetParamType ATTACKERS = new StringSetParamType("attackers", GameComArgs.suggestAgentNames(), GameComArgs.suggestAgentNames()) {
        @Override
        protected ListParamModifier<Set<String>, String> getAdderApplier() {
            return (context, gameData, list, value) -> {
                if (!(gameData instanceof AttackDefendData data)) return false;
                return data.addAttacker(value);
            };
        }
    };
    public static final StringSetParamType DEFENDER_SHOPS = new StringSetParamType("defenderShops",
            CommandUtil.suggestStrings(() -> MiniGameShopsManager.get().getAllIds()),
            GameComArgs.suggestEnabledShops()) {
        @Override
        protected ListParamModifier<Set<String>, String> getAdderApplier() {
            return (context, gameData, list, value) -> {
                gameData.addShops(value);
                list.add(value);
                return true;
            };
        }
    };
    public static final StringSetParamType ATTACKER_SHOPS = new StringSetParamType("attackerShops",
            CommandUtil.suggestStrings(() -> MiniGameShopsManager.get().getAllIds()),
            GameComArgs.suggestEnabledShops()) {
        @Override
        protected ListParamModifier<Set<String>, String> getAdderApplier() {
            return (context, gameData, list, value) -> {
                gameData.addShops(value);
                list.add(value);
                return true;
            };
        }
    };
    // Kill the flag data
    public static final IntParamType BAN_ALL_BLOCKS_RADIUS = new IntParamType("banAllBlocksRadius", 2, 0, 1000000, "blocks");
    public static final IntParamType BLACK_LIST_BLOCKS_RADIUS = new IntParamType("blockBlackListRadius", 0, 0, 1000000, "blocks");
    public static final IntParamType WHITE_LIST_BLOCKS_RADIUS = new IntParamType("blockWhiteListRadius", 0, 0, 1000000, "blocks");
    // Last Stand Data
    public static final IntParamType INIT_ATTACKER_LIVES = new IntParamType("initialAttackerLives", 50, 1, 1000000, "lives");
    // Area Control Data
    public static final FloatParamType AREA_CONTROL_POINTS_MAX = new FloatParamType("areaControlPointsMax", 100f, 1, 1000000);
    public static final IntParamType AREA_RADIUS = new IntParamType("areaRadius", 10, 1, 1000000, "blocks");
    public static final FloatParamType POINTS_PER_PLAYER_PER_SECOND = new FloatParamType("areaControlPointsPerPlayerPerSecond", 4f, 0, 1000000);
    /**
     * called in {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}
     * register all built in games param types here
     */
    public static void registerGameParamTypes() {
        MiniGameManager.registerGameParamType(FUNCTION_ON_GAME_START);
        MiniGameManager.registerGameParamType(FUNCTION_ON_ROUND_START);
        MiniGameManager.registerGameParamType(FUNCTION_ON_ROUND_END);
        MiniGameManager.registerGameParamType(JOIN_SETUP_ONLY);
        MiniGameManager.registerGameParamType(OPEN_JOINING);
        MiniGameManager.registerGameParamType(OPEN_TEAMS);
        MiniGameManager.registerGameParamType(KITS);
        MiniGameManager.registerGameParamType(SHOPS);
        MiniGameManager.registerGameParamType(EVENTS);
        MiniGameManager.registerGameParamType(CAN_ADD_PLAYERS);
        MiniGameManager.registerGameParamType(CAN_ADD_TEAMS);
        MiniGameManager.registerGameParamType(CLEAR_ON_START);
        MiniGameManager.registerGameParamType(CLEAR_ON_ROUND_CHANGE);
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
        MiniGameManager.registerGameParamType(SHOP_OUTSIDE_BUY_RADIUS);
        MiniGameManager.registerGameParamType(ATTACKERS_SHARE_LIVES);
        MiniGameManager.registerGameParamType(DEFENDERS);
        MiniGameManager.registerGameParamType(ATTACKERS);
        MiniGameManager.registerGameParamType(DEFENDER_SHOPS);
        MiniGameManager.registerGameParamType(ATTACKER_SHOPS);
        MiniGameManager.registerGameParamType(BAN_ALL_BLOCKS_RADIUS);
        MiniGameManager.registerGameParamType(BLACK_LIST_BLOCKS_RADIUS);
        MiniGameManager.registerGameParamType(WHITE_LIST_BLOCKS_RADIUS);
        MiniGameManager.registerGameParamType(INIT_ATTACKER_LIVES);
        MiniGameManager.registerGameParamType(AREA_CONTROL_POINTS_MAX);
        MiniGameManager.registerGameParamType(RESPAWN_TICKS);
        MiniGameManager.registerGameParamType(AREA_RADIUS);
        MiniGameManager.registerGameParamType(POI_TYPES);
        MiniGameManager.registerGameParamType(POINTS_PER_PLAYER_PER_SECOND);
    }
}
