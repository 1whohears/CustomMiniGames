package com.onewhohears.minigames.init;

import com.onewhohears.minigames.MiniGamesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class CMGTags {

    public static void init() {
        Blocks.init();
    }

    public static class Blocks {
        public static final TagKey<Block> FLAG_PLACE_WHITE_LIST = tag("place_flag_whitelist");
        public static final TagKey<Block> FLAG_PLACE_BLACK_LIST = tag("place_flag_blacklist");
        private static void init() {}
        public static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(MiniGamesMod.MODID, name));
        }
    }

}
