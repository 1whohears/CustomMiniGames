package com.onewhohears.minigames.data.kits;

import com.onewhohears.onewholibs.data.jsonpreset.JsonPresetReloadListener;

public class MiniGameKitsManager extends JsonPresetReloadListener<GameKit> {

	public static final String KIND = "minigamekits";
	private static MiniGameKitsManager instance;

	public MiniGameKitsManager() {
		super(KIND);
	}

	public static MiniGameKitsManager get() {
		if (instance == null) instance = new MiniGameKitsManager();
		return instance;
	}

	@Override
	public GameKit[] getNewArray(int i) {
		return new GameKit[i];
	}

	@Override
	protected void resetCache() {

	}

	@Override
	public void registerDefaultPresetTypes() {
		addPresetType(GameKit.GAMEKIT);
	}

	// TODO 3.5.1 kit system

}
