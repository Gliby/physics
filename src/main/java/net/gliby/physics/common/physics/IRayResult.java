/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics;

import javax.vecmath.Vector3f;

/**
 *
 */
public interface IRayResult {

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
