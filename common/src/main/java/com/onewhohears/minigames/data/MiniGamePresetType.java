package com.onewhohears.minigames.data;

import com.onewhohears.onewholibs.data.jsonpreset.JsonPresetStats;
import com.onewhohears.onewholibs.data.jsonpreset.JsonPresetType;

public class MiniGamePresetType extends JsonPresetType {
    public MiniGamePresetType(String id, JsonPresetStatsFactory<? extends JsonPresetStats> statsFactory) {
        super(id, statsFactory);
    }
}
