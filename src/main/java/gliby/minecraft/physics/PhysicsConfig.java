package gliby.minecraft.physics;

import gliby.minecraft.physics.common.entity.EntityPhysicsBase;
import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import gliby.minecraft.physics.common.entity.EntityToolGunBeam;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.common.config.Config;

import java.io.File;

@Config(modid = Physics.ID)
@Config.LangKey("gphysics.config.title")
public class PhysicsConfig {

    File modDir;

    PhysicsConfig(File modDir) {
        this.modDir = modDir;
    }

    // @todo: lombok this
    public PhysicsEngineConfig getPhysicsEngineConfig() {
        return physicsEngineConfig;
    }

    public void setPhysicsEngineConfig(PhysicsEngineConfig physicsEngineConfig) {
        this.physicsEngineConfig = physicsEngineConfig;
    }

    public PhysicsEntities getPhysicsEntities() {
        return physicsEntities;
    }

    public void setPhysicsEntities(PhysicsEntities physicsEntities) {
        this.physicsEntities = physicsEntities;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Tools getTools() {
        return tools;
    }

    public void setTools(Tools tools) {
        this.tools = tools;
    }

    public Miscellaneous getMiscellaneous() {
        return miscellaneous;
    }

    public void setMiscellaneous(Miscellaneous miscellaneous) {
        this.miscellaneous = miscellaneous;
    }

    protected PhysicsEngineConfig physicsEngineConfig = new PhysicsEngineConfig();

    protected PhysicsEntities physicsEntities = new PhysicsEntities();

    protected Game game = new Game();

    protected Tools tools = new Tools();

    protected Miscellaneous miscellaneous = new Miscellaneous();

    public Render getRender() {
        return render;
    }

    public void setRender(Render render) {
        this.render = render;
    }

    protected Render render = new Render();

    public File getDirectory() {
        return modDir;
    }

    public static class PhysicsEngineConfig {
        @Config.Comment("Use slower, less buggy Java physics implementation.")
        protected boolean useJavaPhysics = false;

        @Config.Comment("How many ticks per second should the physics engine update?.")
        protected int ticksPerSecond = 20;

        @Config.Comment("Strength of default gravity in the Y axis.")
        protected float gravityForce = -9.8f;

        public boolean isUseJavaPhysics() {
            return useJavaPhysics;
        }

        public void setUseJavaPhysics(boolean useJavaPhysics) {
            this.useJavaPhysics = useJavaPhysics;
        }

        public int getTicksPerSecond() {
            return ticksPerSecond;
        }

        public void setTicksPerSecond(int ticksPerSecond) {
            this.ticksPerSecond = ticksPerSecond;
        }

        public float getGravityForce() {
            return gravityForce;
        }

        public void setGravityForce(float gravityForce) {
            this.gravityForce = gravityForce;
        }
    }

    public static class PhysicsEntities {
        @Config.Comment("Should entities have collision colliders in the world? Enables entity physics interaction.")
        protected boolean entityCollider = false;

        @Config.Comment("How long (seconds) dirty entity colliders last.")
        protected float entityColliderCleanupTime = 1.0f;

        @Config.Comment("How long (seconds) do player spawned physics entities last before re-aligning.")
        protected float playerSpawnedExpiryTime = 30.0f;

        @Config.Comment("How long (seconds) do game spawned (falling blocks) physics entities last before re-aligning.")
        protected float gameSpawnedExpiryTime = 15.0f;

        @Config.Comment("Entities classes that are blacklisted from collider creation.")
        protected String[] entityColliderBlacklist = {EntityPhysicsBlock.class.getName(), EntityPhysicsBase.class.getName(), EntityToolGunBeam.class.getName(), EntityItem.class.getName()};

        public boolean isEntityCollider() {
            return entityCollider;
        }

        public void setEntityCollider(boolean entityCollider) {
            this.entityCollider = entityCollider;
        }

        public float getEntityColliderCleanupTime() {
            return entityColliderCleanupTime;
        }

        public void setEntityColliderCleanupTime(float entityColliderCleanupTime) {
            this.entityColliderCleanupTime = entityColliderCleanupTime;
        }

        public float getPlayerSpawnedExpiryTime() {
            return playerSpawnedExpiryTime;
        }

        public void setPlayerSpawnedExpiryTime(float playerSpawnedExpiryTime) {
            this.playerSpawnedExpiryTime = playerSpawnedExpiryTime;
        }

        public float getGameSpawnedExpiryTime() {
            return gameSpawnedExpiryTime;
        }

        public void setGameSpawnedExpiryTime(float gameSpawnedExpiryTime) {
            this.gameSpawnedExpiryTime = gameSpawnedExpiryTime;
        }

        public String[] getEntityColliderBlacklist() {
            return entityColliderBlacklist;
        }

        public void setEntityColliderBlacklist(String[] entityColliderBlacklist) {
            this.entityColliderBlacklist = entityColliderBlacklist;
        }
    }

    public static class Game {
        @Config.Comment("Water flow force multiplier.")
        protected float waterForceMultiplier = 1.0f;

        @Config.Comment("Projectile (e.g., arrows) impact force.")
        protected float projectileImpulseForce = 30.0f;

        @Config.Comment("How far (in blocks) do explosions affect physics.")
        protected float explosionImpulseRadius = 32.0f;

        @Config.Comment("Explosion physics force.")
        protected float explosionImpulseForce = 500.0f;

        @Config.Comment("Should replace all nearby falling blocks with physics blocks?")
        protected boolean replaceFallingBlocks = true;

        @Config.Comment("How close (in blocks) do players have to be for falling physics blocks.")
        protected float fallingBlockSpawnDistance = 32.0f;

        public float getWaterForceMultiplier() {
            return waterForceMultiplier;
        }

        public void setWaterForceMultiplier(float waterForceMultiplier) {
            this.waterForceMultiplier = waterForceMultiplier;
        }

        public float getProjectileImpulseForce() {
            return projectileImpulseForce;
        }

        public void setProjectileImpulseForce(float projectileImpulseForce) {
            this.projectileImpulseForce = projectileImpulseForce;
        }

        public float getExplosionImpulseRadius() {
            return explosionImpulseRadius;
        }

        public void setExplosionImpulseRadius(float explosionImpulseRadius) {
            this.explosionImpulseRadius = explosionImpulseRadius;
        }

        public float getExplosionImpulseForce() {
            return explosionImpulseForce;
        }

        public void setExplosionImpulseForce(float explosionImpulseForce) {
            this.explosionImpulseForce = explosionImpulseForce;
        }

        public boolean isReplaceFallingBlocks() {
            return replaceFallingBlocks;
        }

        public void setReplaceFallingBlocks(boolean replaceFallingBlocks) {
            this.replaceFallingBlocks = replaceFallingBlocks;
        }

        public float getFallingBlockSpawnDistance() {
            return fallingBlockSpawnDistance;
        }

        public void setFallingBlockSpawnDistance(float fallingBlockSpawnDistance) {
            this.fallingBlockSpawnDistance = fallingBlockSpawnDistance;
        }
    }

    public static class Tools {
        @Config.Comment("Attract Tool radius (in blocks).")
        protected float attractRadius = 16.0f;

        @Config.Comment("Attract Tool attract force.")
        protected float attractForce = 10.0f;

        @Config.Comment("Gravitizer Tool radius (in blocks).")
        protected float gravitizerRadius = 16.0f;

        @Config.Comment("Gravitizer Tool gravity force.")
        protected float gravitizerForce = 10.0f;

        public float getAttractRadius() {
            return attractRadius;
        }

        public void setAttractRadius(float attractRadius) {
            this.attractRadius = attractRadius;
        }

        public float getAttractForce() {
            return attractForce;
        }

        public void setAttractForce(float attractForce) {
            this.attractForce = attractForce;
        }

        public float getGravitizerRadius() {
            return gravitizerRadius;
        }

        public void setGravitizerRadius(float gravitizerRadius) {
            this.gravitizerRadius = gravitizerRadius;
        }

        public float getGravitizerForce() {
            return gravitizerForce;
        }

        public void setGravitizerForce(float gravitizerForce) {
            this.gravitizerForce = gravitizerForce;
        }
    }


    public static class Miscellaneous {

        @Config.Comment("Disables minecraft anti-flight kick.")
        protected boolean disableAllowFlight = true;

        @Config.Comment("Gravitizer Tool gravity force.")
        protected String LastVersion = "" + Physics.VERSION;

        public boolean isDisableAllowFlight() {
            return disableAllowFlight;
        }

        public void setDisableAllowFlight(boolean disableAllowFlight) {
            this.disableAllowFlight = disableAllowFlight;
        }

        public String getLastVersion() {
            return LastVersion;
        }

        public void setLastVersion(String lastVersion) {
            LastVersion = lastVersion;
        }
    }

    public static class Render {

        public float getBlockInterpolation() {
            return blockInterpolation;
        }

        public void setBlockInterpolation(float blockInterpolation) {
            this.blockInterpolation = blockInterpolation;
        }

        @Config.Comment("How much to interpolate render transform to block transform per render tick.")
        protected float blockInterpolation = 0.15f;
    }

}
