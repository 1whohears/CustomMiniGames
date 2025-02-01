package com.onewhohears.minigames.data.shops;

import com.onewhohears.onewholibs.data.jsonpreset.JsonPresetReloadListener;

public class MiniGameShopsManager extends JsonPresetReloadListener<GameShop> {

	public static final String KIND = "minigameshops";
	private static MiniGameShopsManager instance;

	public static MiniGameShopsManager get() {
		if (instance == null) instance = new MiniGameShopsManager();
		return instance;
	}

	public MiniGameShopsManager() {
		super(KIND);
	}

	@Override
	public GameShop[] getNewArray(int i) {
		return new GameShop[i];
	}

	@Override
	protected void resetCache() {

	}

	@Override
	public void registerDefaultPresetTypes() {
		addPresetType(GameShop.GAMESHOP);
	}

	// TODO 3.4.1 shop system

}
