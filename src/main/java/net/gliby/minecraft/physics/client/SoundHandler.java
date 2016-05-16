/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.physics.client;

import java.util.HashMap;
import java.util.Map;

import net.gliby.minecraft.physics.Physics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

/**
 *
 */
public class SoundHandler {

	private static SoundHandler instance = new SoundHandler();

	static SoundHandler getInstance() {
		return instance;
	}

	private static Map<String, String> soundRegistry = new HashMap<String, String>();
	static {
		soundRegistry.put("ToolGun.Scroll", "item.toolgun.scroll");
		soundRegistry.put("ToolGun.Beam", "item.toolgun.beam");
	}

	public static String getSoundByIdentifer(String soundName) {
		return Physics.MOD_ID + ":" + soundRegistry.get(soundName);
	}

	public static void playLocalSound(Minecraft mc, String sound) {
		mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation(getSoundByIdentifer(sound)), 1.0F));
	}
}
