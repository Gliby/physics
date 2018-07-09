/**
 * Copyright (c) 2015, Mine Fortress.
 */
package gliby.minecraft.physics.common.physics.engine;

import com.bulletphysics.linearmath.Transform;

/**
 *
 */
public interface IConstraintGeneric6Dof extends IConstraint {

	/**
	 * @param transform
	 * @return
	 */
	Transform getGlobalFrameOffsetA(Transform transform);

	/**
	 * @param transform
	 * @return
	 */
	Transform getGlobalFrameOffsetB(Transform transform);

}
