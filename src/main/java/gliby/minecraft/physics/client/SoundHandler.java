package gliby.minecraft.physics.client;

import gliby.minecraft.physics.Physics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SoundHandler {

    private static Map<String, SoundEvent> soundRegistry = new HashMap<String, SoundEvent>();

    static {
        soundRegistry.put("ToolGun.Scroll", new SoundEvent(new ResourceLocation(Physics.ID, "toolgun_scroll")));
        soundRegistry.put("ToolGun.Beam", new SoundEvent(new ResourceLocation(Physics.ID, "toolgun_beam")));
    }

    public static SoundEvent getSoundByIdentifier(String soundName) {
        return soundRegistry.get(soundName);
    }

    public static void playSound(Minecraft mc, SoundEvent soundIn, SoundCategory categoryIn, float volumeIn, float pitchIn, Vector3f pos) {
        PositionedSoundRecord sound = new PositionedSoundRecord(soundIn, categoryIn, volumeIn, pitchIn, pos.getX(), pos.getY(), pos.getZ());
        mc.getSoundHandler().playSound(sound);
    }

    public static void playLocalSound(Minecraft mc, String soundName) {
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(getSoundByIdentifier(soundName), 1.0f));
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        final IForgeRegistry<SoundEvent> registry = event.getRegistry();
        for (SoundEvent soundEvent : soundRegistry.values()) {
            soundEvent.setRegistryName(soundEvent.getSoundName());
            registry.register(soundEvent);
        }

    }
}
