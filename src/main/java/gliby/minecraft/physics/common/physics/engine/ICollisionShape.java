package gliby.minecraft.physics.common.physics.engine;

import java.util.List;

import javax.vecmath.Vector3f;

/**
 *
 */
public interface ICollisionShape {

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
