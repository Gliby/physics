package gliby.minecraft.physics.client;

import com.badlogic.gdx.math.Vector3;
import gliby.minecraft.physics.Physics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.Sound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
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

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        final IForgeRegistry<SoundEvent> registry = event.getRegistry();
        for (SoundEvent soundEvent : soundRegistry.values()) {
            soundEvent.setRegistryName(soundEvent.getSoundName());
            registry.register(soundEvent);
        }

    }

    public static SoundEvent getSoundByIdentifier(String soundName) {
        SoundEvent event = soundRegistry.get(soundName);
        return event;
    }

    public static void playSound(Minecraft mc, SoundEvent soundIn, SoundCategory categoryIn, float volumeIn, float pitchIn, Vector3f pos)
    {
        PositionedSoundRecord sound = new PositionedSoundRecord(soundIn, categoryIn, volumeIn, pitchIn, (float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
        mc.getSoundHandler().playSound(sound);
    }


    public static void playLocalSound(Minecraft mc, String soundName) {
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(getSoundByIdentifier(soundName), 1.0f));
    }
}
