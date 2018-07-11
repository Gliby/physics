package gliby.minecraft.physics.common.physics.mechanics;

import java.util.ConcurrentModificationException;
import java.util.concurrent.Callable;

import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;

/**
 *
 */
public abstract class PhysicsMechanic implements Callable, Runnable {

	private boolean enabled = true;
	private int ticksPerSecond;

	/**
	 * @return the ticksPerSecond
	 */
	public int getTicksPerSecond() {
		return ticksPerSecond;
	}

	/**
	 * @param ticksPerSecond
	 *            the ticksPerSecond to set
	 */
	public synchronized void setTicksPerSecond(int ticksPerSecond) {
		this.ticksPerSecond = ticksPerSecond;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 */
	public synchronized void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	protected PhysicsWorld physicsWorld;

	protected final boolean threaded;

	public boolean isThreaded() {
		return threaded;
	}

	/**
	 * @param physicsWorld
	 * @param threaded
	 * @param ticksPerSecond
	 */
	public PhysicsMechanic(PhysicsWorld physicsWorld, boolean threaded, int ticksPerSecond) {
		this.physicsWorld = physicsWorld;
		this.threaded = threaded;
		this.ticksPerSecond = ticksPerSecond;
	}
	

	@Override
	public Object call() {
		try {
			this.update();
		} catch (Exception e) {
			if (!(e instanceof ConcurrentModificationException)) {
				e.printStackTrace();
				Physics.getLogger().error("# # # # # # # # # # # # # # # # # # # # # # #");
				Physics.getLogger().error(this + " crashed because of " + e + ". Restarting now.");
				Physics.getLogger().error("# # # # # # # # # # # # # # # # # # # # # # #");
			}
			this.physicsWorld.restartMechanic(this);
		}
		return null;
	}

	@Override
	public void run() {
		while (enabled) {
			synchronized (this) {
				try {
					wait(1000 / ticksPerSecond);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				this.update();
			} catch (Exception e) {
				if (!(e instanceof ConcurrentModificationException)) {
					e.printStackTrace();
					Physics.getLogger().error("# # # # # # # # # # # # # # # # # # # # # # #");
					Physics.getLogger().error(this + " crashed because of " + e + ". Restarting now.");
					Physics.getLogger().error("# # # # # # # # # # # # # # # # # # # # # # #");
				}
				this.physicsWorld.restartMechanic(this);
				break;
			}
		}
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
