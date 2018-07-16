package gliby.minecraft.physics.common.physics.engine;

import com.badlogic.gdx.math.Matrix4;

import gliby.minecraft.physics.common.physics.PhysicsWorld;

/**
 *
 */
public interface IConstraintGeneric6Dof extends IConstraint {

	PhysicsWorld getPhysicsWorld();

	/**
	 * @param transform
	 * @return
	 */
	Matrix4 getGlobalFrameOffsetA(Matrix4 transform);

	/**
	 * @param transform
	 * @return
	 */
	Matrix4 getGlobalFrameOffsetB(Matrix4 transform);

}
