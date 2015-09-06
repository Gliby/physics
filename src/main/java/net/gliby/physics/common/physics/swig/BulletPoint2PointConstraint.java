/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.swig;

import javax.vecmath.Vector3f;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

import net.gliby.physics.common.physics.IConstraint;
import net.gliby.physics.common.physics.IConstraintPoint2Point;

/**
 *
 */
class BulletPoint2PointConstraint implements IConstraintPoint2Point {

	private btPoint2PointConstraint constraint;

	BulletPoint2PointConstraint(btPoint2PointConstraint constraint) {
		this.constraint = constraint;
	}

	@Override
	public void setImpulseClamp(float f) {
		constraint.getSetting().setImpulseClamp(f);
	}

	@Override
	public void setTau(float f) {
		constraint.getSetting().setTau(f);
	}

	@Override
	public void setPivotB(Vector3f newPos) {
		constraint.setPivotB(BulletPhysicsWorld.toVector3(newPos));
	}

	@Override
	public Object getConstraint() {
		return constraint;
	}

	@Override
	public boolean isPoint2Point() {
		return true;
	}

	@Override
	public Vector3f getPivotInA(Vector3f out) {
		out.set(BulletPhysicsWorld.toVector3f(constraint.getPivotInA()));
		return out;
	}

	@Override
	public Vector3f getPivotInB(Vector3f out) {
		out.set(BulletPhysicsWorld.toVector3f(constraint.getPivotInB()));
		return out;
	}

	@Override
	public boolean isGeneric6Dof() {
		return false;
	}

	@Override
	public boolean isSlider() {
		return false;
	}

}
