/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.jbullet;

import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysics.linearmath.Transform;

import net.gliby.physics.common.physics.IConstraintGeneric6Dof;

/**
 *
 */
public class JavaConstraintGeneric6Dof implements IConstraintGeneric6Dof {

	private Generic6DofConstraint constraint;

	JavaConstraintGeneric6Dof(Generic6DofConstraint constraint) {
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

}
