package gliby.minecraft.physics.common.physics.engine;

import com.badlogic.gdx.math.Matrix4;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.entity.Entity;

/**
 *
 */
public interface ICollisionObject {
	
	PhysicsWorld getPhysicsWorld();

	public Entity getOwner();
	
	
	public Object getCollisionObject();

	/**
	 * @param entityTransform
	 */
	public void setWorldTransform(Matrix4 entityTransform);

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
	public void setInterpolationWorldTransform(Matrix4 entityTransform);
	
}
