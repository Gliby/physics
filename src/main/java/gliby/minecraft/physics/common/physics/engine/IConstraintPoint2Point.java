package gliby.minecraft.physics.common.physics.engine;


import com.badlogic.gdx.math.Vector3;

import gliby.minecraft.physics.common.physics.PhysicsWorld;

/**
 *
 */
public interface IConstraintPoint2Point extends IConstraint {

	PhysicsWorld getPhysicsWorld();
	
	public void setImpulseClamp(float f);

	public void setTau(float f);

	public void setPivotB(Vector3 newPos);

	public Vector3 getPivotInA(Vector3 vector3);

	public Vector3 getPivotInB(Vector3 vector3);
}
