package gliby.minecraft.physics.common.physics.engine.concurrent.javabullet;

import com.bulletphysicsx.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintGeneric6Dof;
import gliby.minecraft.physics.common.physics.engine.concurrent.ConcurrentPhysicsWorld;

/**
 *
 */
public class JavaConstraintGeneric6Dof implements IConstraintGeneric6Dof {

	protected ConcurrentPhysicsWorld physicsWorld;
	private Generic6DofConstraint constraint;

	JavaConstraintGeneric6Dof(ConcurrentPhysicsWorld physicsWorld, Generic6DofConstraint constraint) {
		this.physicsWorld = physicsWorld;
		this.constraint = constraint;
	}

	@Override
	public Object getConstraint() {
		return constraint;
	}

	@Override
	public boolean isPoint2Point() {
		return false;
	}

	@Override
	public boolean isGeneric6Dof() {
		return true;
	}

	@Override
	public Transform getGlobalFrameOffsetA(Transform transform) {
		return constraint.getCalculatedTransformA(transform);
	}

	@Override
	public Transform getGlobalFrameOffsetB(Transform transform) {
		return constraint.getCalculatedTransformB(transform);
	}

	@Override
	public boolean isSlider() {
		return false;
	}

	@Override
	public ConcurrentPhysicsWorld getPhysicsWorld() {
		return physicsWorld;
	}


}
