package gliby.minecraft.physics.common.physics.engine;

import java.util.List;

import javax.vecmath.Vector3f;

import gliby.minecraft.physics.common.physics.PhysicsWorld;

/**
 *
 */
public interface ICollisionShape {

	PhysicsWorld getPhysicsWorld();
	
	
	/**
	 * @return
	 */
	Object getCollisionShape();

	/**
	 * @return
	 */
	int getShapeType();

	/**
	 * @return
	 */
	boolean isBoxShape();

	/**
	 * @return
	 */
	boolean isCompoundShape();

	void setLocalScaling(Vector3f localScaling);
	
	/**
	 * @param mass
	 * @param localInertia
	 */
	void calculateLocalInertia(float mass, Object localInertia);

	/**
	 * Only applies to box shapes.
	 * 
	 * @param halfExtent
	 */
	void getHalfExtentsWithMargin(Vector3f halfExtent);

	/**
	 * @return
	 */
	List<ICollisionShapeChildren> getChildren();

}
