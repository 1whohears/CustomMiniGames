package com.onewhohears.minigames.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class UtilParse {
	
	public static final Gson GSON = new Gson();
	
	public static CompoundTag getComoundFromResource(String path) {
		CompoundTag compound;
        DataInputStream dis;
        try {
            dis = new DataInputStream(new GZIPInputStream(getResourceAsStream(path)));
            compound = NbtIo.read(dis);
            dis.close();
        }
        catch (Exception e) {
        	System.out.println("ERROR: COULD NOT PARSE COMPOUNDTAG "+path);
            e.printStackTrace();
        	return new CompoundTag();
        }
        return compound;
	}
	
	public static CompoundTag getCompoundFromJson(JsonObject json) {
		return JsonToNBTUtil.getTagFromJson(json);
	}
	
	public static CompoundTag getCompoundFromJsonResource(String path) {
		return getCompoundFromJson(getJsonFromResource(path));
	}
	
	public static JsonObject getJsonFromResource(Resource resource) {
		JsonObject json;
		try {
			BufferedReader br = resource.openAsReader();
			json = GSON.fromJson(br, JsonObject.class);
			br.close();
		} catch (Exception e) {
			System.out.println("ERROR: COULD NOT PARSE JSON "+resource.sourcePackId());
			e.printStackTrace();
			return new JsonObject();
		}
		return json;
	}
	
	public static JsonObject getJsonFromResource(String path) {
		JsonObject json;
        InputStreamReader isr;
        try {
        	isr = new InputStreamReader(getResourceAsStream(path));
            json = GSON.fromJson(isr, JsonObject.class);
            isr.close();
        } catch (Exception e) {
        	System.out.println("ERROR: COULD NOT PARSE JSON "+path);
            e.printStackTrace();
        	return new JsonObject();
        }
        return json;
	}

	private static InputStream getResourceAsStream(String resource) {
	    return UtilParse.class.getResourceAsStream(resource);
	}
	
	public static void writeVec3(CompoundTag tag, Vec3 v, String name) {
		if (v == null) return;
		tag.putDouble(name+"x", v.x);
		tag.putDouble(name+"y", v.y);
		tag.putDouble(name+"z", v.z);
	}
	
	public static Vec3 readVec3(CompoundTag tag, String name) {
		if (!tag.contains(name+"x") || !tag.contains(name+"y") || !tag.contains(name+"z")) return null;
		double x, y, z;
		x = tag.getDouble(name+"x");
		y = tag.getDouble(name+"y");
		z = tag.getDouble(name+"z");
		return new Vec3(x, y, z);
	}

	public static float fixFloatNbt(CompoundTag nbt, String tag, CompoundTag presetNbt, float min) {
		if (nbt.contains(tag)) {
			float f = nbt.getFloat(tag);
			if (f > min) return f;
		} 
		float nbtf = presetNbt.getFloat(tag);
		nbt.putFloat(tag, nbtf);
		return nbtf;
	}

	public static float fixFloatNbt(CompoundTag nbt, String tag, float alt) {
		if (!nbt.contains(tag)) {
			nbt.putFloat(tag, alt);
			return alt;
		}
		return nbt.getFloat(tag);
	}
	
	public static String prettyVec3(Vec3 v) {
		return String.format("[%3.1f,%3.1f,%3.1f]", v.x, v.y, v.z);
	}
	
	public static String prettyVec3(Vec3 v, int decimals) {
		String f = "%3."+decimals+"f";
		return String.format("["+f+","+f+","+f+"]", v.x, v.y, v.z);
	}
	
	public static String getRandomString(String[]... arrays) {
		int size = 0;
		for (int i = 0; i < arrays.length; ++i) size += arrays[i].length;
		int k = 0, r = (int)(Math.random()*size);
		for (int i = 0; i < arrays.length; ++i) 
			for (int j = 0; j < arrays[i].length; ++j) 
				if (k++ == r) return arrays[i][j];
		return "";
	}
	
	/**
	 * @param weights this array must be the same size as arrays
	 * @param arrays
	 * @return a random string in arrays
	 */
	public static String getRandomString(int[] weights, String[]... arrays) {
		if (weights.length != arrays.length) return "";
		int size = 0;
		for (int i = 0; i < arrays.length; ++i) size += arrays[i].length * weights[i];
		int k = 0, r = (int)(Math.random()*size);
		for (int i = 0; i < arrays.length; ++i) 
			for (int w = 0; w < weights[i]; ++w)
				for (int j = 0; j < arrays[i].length; ++j) 
					if (k++ == r) return arrays[i][j];
		return "";
	}
	
	@Nullable
	public static Class<? extends Entity> getEntityClass(String className) {
		try {
			return Class.forName(className, false, UtilParse.class.getClassLoader()).asSubclass(Entity.class);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	public static boolean getBooleanSafe(JsonObject json, String name, boolean alt) {
		if (!json.has(name)) return alt;
		return json.get(name).getAsBoolean();
	}
	
	public static int getIntSafe(JsonObject json, String name, int alt) {
		if (!json.has(name)) return alt;
		return json.get(name).getAsInt();
	}
	
	public static float getFloatSafe(JsonObject json, String name, float alt) {
		if (!json.has(name)) return alt;
		return json.get(name).getAsFloat();
	}
	
	public static String getStringSafe(JsonObject json, String name, String alt) {
		if (!json.has(name)) return alt;
		return json.get(name).getAsString();
	}
	
}
