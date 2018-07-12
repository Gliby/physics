package gliby.minecraft.physics.common.physics.engine;

import javax.vecmath.Vector3f;

import gliby.minecraft.physics.common.physics.PhysicsWorld;

/**
 *
 */
public interface IConstraintPoint2Point extends IConstraint {

	PhysicsWorld getPhysicsWorld();
	
	public void setImpulseClamp(float f);

	public void setTau(float f);

	public void setPivotB(Vector3f newPos);

	public Vector3f getPivotInA(Vector3f vector3f);

	public Vector3f getPivotInB(Vector3f vector3f);
}
