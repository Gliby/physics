package gliby.minecraft.physics.common.physics.mechanics;

import gliby.minecraft.physics.common.physics.PhysicsWorld;

/**
 * Global extensions to PhysicsWorld.
 */
public abstract class PhysicsMechanic {


    public PhysicsWorld physicsWorld;
    private boolean enabled = true;
    private int ticksPerSecond;

    /**
     * @param physicsWorld
     * @param ticksPerSecond
     */
    public PhysicsMechanic(final PhysicsWorld physicsWorld, final int ticksPerSecond) {
        this.physicsWorld = physicsWorld;
        this.ticksPerSecond = ticksPerSecond;
    }

    /**
     * @return the ticksPerSecond
     */
    public int getTicksPerSecond() {
        return ticksPerSecond;
    }

    /**
     * @param ticksPerSecond the ticksPerSecond to set
     */
    public void setTicksPerSecond(final int ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Updates!
     */
    public abstract void update();

    public abstract String getName();

    /**
     *
     */
    public abstract void init();

    public abstract void dispose();
}
