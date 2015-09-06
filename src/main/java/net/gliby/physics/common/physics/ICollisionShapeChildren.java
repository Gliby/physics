/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics;

import com.bulletphysics.linearmath.Transform;

/**
 *
 */
public interface ICollisionShapeChildren {

	public Transform getTransform();

	public ICollisionShape getCollisionShape();
}
