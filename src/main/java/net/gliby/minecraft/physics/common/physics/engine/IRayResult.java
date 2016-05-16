/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.physics.common.physics.engine;

import javax.vecmath.Vector3f;

/**
 *
 */
public interface IRayResult extends IDisposable {

	public Object getRayResultCallback();

	/**
	 * @return
	 */
	public boolean hasHit();

	/**
	 * @return
	 */
	public Object getCollisionObject();

	/**
	 * @return
	 */
	public Vector3f getHitPointWorld();

	/**
	 * @return
	 */
	public Vector3f getHitPointNormal();
	
}
