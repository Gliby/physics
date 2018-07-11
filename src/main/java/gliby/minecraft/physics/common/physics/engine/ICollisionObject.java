package gliby.minecraft.physics.common.physics.engine;

import com.bulletphysicsx.linearmath.Transform;

import net.minecraft.entity.Entity;

/**
 *
 */
public interface ICollisionObject {

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
