package gliby.minecraft.physics;

import gliby.minecraft.physics.common.entity.EntityPhysicsBase;
import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import gliby.minecraft.physics.common.entity.EntityToolGunBeam;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

@Config(modid = Physics.ID, type = Config.Type.INSTANCE)
@Config.LangKey("gphysics.config.title")
public class PhysicsConfig {

    @Mod.EventBusSubscriber(modid = Physics.ID)
    private static class EventHandler {

        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Physics.ID)) {
                ConfigManager.sync(Physics.ID, Config.Type.INSTANCE);
            }
        }
    }

    static File modDir;

    public static File getModDirectory() {
        return modDir;
    }

    public static void setModDirectory(File inModDir) {
        modDir = inModDir;
    }

    public static PhysicsEngine PHYSICS_ENGINE = new PhysicsEngine();

    public static PhysicsEntities PHYSICS_ENTITIES = new PhysicsEntities();

    public static Game GAME = new Game();

    public static Tools TOOLS = new Tools();

    public static Miscellaneous MISCELLANEOUS = new Miscellaneous();

    public static Render RENDER = new Render();

    public File getDirectory() {
        return modDir;
    }

    public static class PhysicsEngine {
        @Config.RequiresWorldRestart
        @Config.Comment("Use slower, less buggy Java physics implementation.")
        public boolean useJavaPhysics = false;

        @Config.RequiresWorldRestart
        @Config.Comment("How many ticks per second should the physics engine update?.")
        public int ticksPerSecond = 20;

        @Config.RequiresWorldRestart
        @Config.Comment("Strength of default gravity in the Y axis.")
        public float gravityForce = -9.8f;
    }

    public static class PhysicsEntities {

        @Config.Comment("Should entities have collision colliders in the world? Enables entity physics interaction.")
        public boolean entityColliders = false;

        @Config.Comment("How long (seconds) dirty entity colliders last.")
        public float entityColliderCleanupTime = 1.0f;

        @Config.Comment("How long (seconds) do player spawned physics entities last before re-aligning.")
        public float playerSpawnedExpiryTime = 30.0f;

        @Config.Comment("How long (seconds) do game spawned (falling blocks) physics entities last before re-aligning.")
        public float gameSpawnedExpiryTime = 15.0f;

        @Config.Comment("Entities classes that are blacklisted from collider creation.")
        public String[] entityColliderBlacklist = {EntityPhysicsBlock.class.getName(), EntityPhysicsBase.class.getName(), EntityToolGunBeam.class.getName(), EntityItem.class.getName()};

    }

    public static class Game {
        @Config.Comment("Water flow force multiplier.")
        public float waterForceMultiplier = 1.0f;

        @Config.Comment("Projectile (e.g., arrows) impact force.")
        public float projectileImpulseForce = 30.0f;

        @Config.Comment("How far (in blocks) do explosions affect physics.")
        public float explosionImpulseRadius = 32.0f;

        @Config.Comment("Explosion physics force.")
        public float explosionImpulseForce = 500.0f;

        @Config.Comment("Should replace all nearby falling blocks with physics blocks?")
        public boolean replaceFallingBlocks = true;

        @Config.Comment("How close (in blocks) do players have to be for falling physics blocks.")
        public float fallingBlockSpawnDistance = 32.0f;

        @Config.Comment("Limits physics blocks, old blocks get re-aligned. -1 disables limit.")
        public float maxPhysicsBlocks = 512;
    }

    public static class Tools {
        @Config.Comment("Attract Tool radius (in blocks).")
        public float attractRadius = 16.0f;

        @Config.Comment("Attract Tool attract force.")
        public float attractForce = 10.0f;

        @Config.Comment("Gravitizer Tool radius (in blocks).")
        public float gravitizerRadius = 16.0f;

        @Config.Comment("Gravitizer Tool gravity force.")
        public float gravitizerForce = 10.0f;

    }


    public static class Miscellaneous {

        @Config.Comment("Disables minecraft anti-flight kick.")
        public boolean disableAllowFlight = true;

        public String lastVersion = "" + Physics.VERSION;
    }

    public static class Render {

        @Config.Comment("How much to interpolate render transform to block transform per render tick.")
        public float blockInterpolation = 0.15f;
    }

}
