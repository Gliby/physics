package gliby.minecraft.physics.common.physics.engine.nativebullet;

import javax.vecmath.Vector3f;

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
		getPhysicsWorld().scheduledTasks.add(new Runnable() {

			@Override
			public void run() {
				constraint.getSetting().setImpulseClamp(f);
			}
		});
	}

	@Override
	public void setTau(final float f) {
		getPhysicsWorld().scheduledTasks.add(new Runnable() {

			@Override
			public void run() {
				constraint.getSetting().setTau(f);
			}
		});
	}

	@Override
	public void setPivotB(final Vector3f newPos) {
		getPhysicsWorld().scheduledTasks.add(new Runnable() {

			@Override
			public void run() {
				constraint.setPivotB(NativePhysicsWorld.toVector3(newPos));
			}
		});
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
		out.set(NativePhysicsWorld.toVector3f(constraint.getPivotInA()));
		return out;
	}

	@Override
	public Vector3f getPivotInB(Vector3f out) {
		out.set(NativePhysicsWorld.toVector3f(constraint.getPivotInB()));
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
