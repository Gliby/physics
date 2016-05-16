/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.physics.common.physics.engine;

import com.bulletphysics.linearmath.Transform;

/**
 *
 */
public interface ICollisionShapeChildren {

	public Transform getTransform();

	public ICollisionShape getCollisionShape();
	
	public void dispose();
}
