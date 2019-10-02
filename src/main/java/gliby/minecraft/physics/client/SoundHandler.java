package gliby.minecraft.physics.client;

import gliby.minecraft.physics.Physics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SoundHandler {

    private static SoundHandler instance = new SoundHandler();
    private static Map<String, String> soundRegistry = new HashMap<String, String>();

    static {
        soundRegistry.put("ToolGun.Scroll", "item.toolgun.scroll");
        soundRegistry.put("ToolGun.Beam", "item.toolgun.beam");
    }

    static SoundHandler getInstance() {
        return instance;
    }

    public static String getSoundByIdentifer(String soundName) {
        return Physics.ID + ":" + soundRegistry.get(soundName);
    }

    public static void playLocalSound(Minecraft mc, String sound) {
        // todo 1.12.2 port
//        mc.getSoundHandler().playSound(PositionedSoundRecord.(new ResourceLocation(getSoundByIdentifer(sound)), 1.0F));
    }
}
