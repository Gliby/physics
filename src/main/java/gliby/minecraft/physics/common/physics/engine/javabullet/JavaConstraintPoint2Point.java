package gliby.minecraft.physics.common.physics.engine.javabullet;

import javax.vecmath.Vector3f;

import com.bulletphysicsx.dynamics.constraintsolver.Point2PointConstraint;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintPoint2Point;

/**
 *
 */
public class JavaConstraintPoint2Point implements IConstraintPoint2Point {

	private Point2PointConstraint constraint;

	protected PhysicsWorld physicsWorld;

	JavaConstraintPoint2Point(PhysicsWorld physicsWorld, Point2PointConstraint constraint) {
		this.physicsWorld = physicsWorld;
		this.constraint = constraint;
	}

	@Override
	public void setImpulseClamp(final float f) {
		this.getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				constraint.setting.impulseClamp = f;
			}
		});
	}

	@Override
	public void setTau(final float f) {
		this.getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				constraint.setting.tau = f;
			}
		});
	}

	@Override
	public void setPivotB(final Vector3f newPos) {
		this.getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				constraint.setPivotB(newPos);
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
		return constraint.getPivotInA(out);
	}

	@Override
	public Vector3f getPivotInB(Vector3f out) {
		return constraint.getPivotInB(out);
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
