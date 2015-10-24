/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.engine;

import com.bulletphysics.linearmath.Transform;

import net.minecraft.entity.Entity;

/**
 *
 */
public interface ICollisionObject extends IDisposable {

	public Entity getOwner();
	
	
	public Object getCollisionObject();

	/**
	 * @param entityTransform
	 */
	public void setWorldTransform(Transform entityTransform);

	/**
	 * @param iCollisionShape
	 */
	public void setCollisionShape(ICollisionShape iCollisionShape);

	/**
	 * @param characterObject
	 */
	public void setCollisionFlags(int characterObject);

	/**
	 * @param entityTransform
	 */
	public void setInterpolationWorldTransform(Transform entityTransform);
	
}
