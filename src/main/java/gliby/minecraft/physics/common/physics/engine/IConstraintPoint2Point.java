/**
 * Copyright (c) 2015, Mine Fortress.
 */
package gliby.minecraft.physics.common.physics.engine;

import javax.vecmath.Vector3f;

/**
 *
 */
public interface IConstraintPoint2Point extends IConstraint {

	public void setImpulseClamp(float f);

	public void setTau(float f);

	public void setPivotB(Vector3f newPos);

	public Vector3f getPivotInA(Vector3f vector3f);

	public Vector3f getPivotInB(Vector3f vector3f);
}
