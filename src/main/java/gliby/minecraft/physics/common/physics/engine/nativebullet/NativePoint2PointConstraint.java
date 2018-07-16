package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintPoint2Point;

/**
 *
 */
class NativePoint2PointConstraint implements IConstraintPoint2Point {

	private btPoint2PointConstraint constraint;
	protected PhysicsWorld physicsWorld;

	NativePoint2PointConstraint(PhysicsWorld physicsWorld, btPoint2PointConstraint constraint) {
		this.physicsWorld = physicsWorld;
		this.constraint = constraint;
	}

	@Override
	public void setImpulseClamp(final float f) {
		constraint.getSetting().setImpulseClamp(f);
	}

	@Override
	public void setTau(final float f) {
		constraint.getSetting().setTau(f);
	}

	@Override
	public void setPivotB(final Vector3 newPos) {
		constraint.setPivotB(newPos);
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
	public Vector3 getPivotInA(Vector3 out) {
		out.set(constraint.getPivotInA());
		return out;
	}

	@Override
	public Vector3 getPivotInB(Vector3 out) {
		out.set(constraint.getPivotInB());
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

	@Override
	public PhysicsWorld getPhysicsWorld() {
		return physicsWorld;
	}
}
