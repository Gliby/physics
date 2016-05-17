/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.physics.common.physics.engine.javabullet;

import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.constraintsolver.Point2PointConstraint;

import net.gliby.minecraft.physics.common.physics.engine.IConstraintPoint2Point;

/**
 *
 */
public class JavaConstraintPoint2Point implements IConstraintPoint2Point {

	private Point2PointConstraint constraint;

	/**
	 * @param point2PointConstraint
	 */
	JavaConstraintPoint2Point(Point2PointConstraint constraint) {
		this.constraint = constraint;
	}

	@Override
	public void setImpulseClamp(float f) {
		constraint.setting.impulseClamp = f;
	}

	@Override
	public void setTau(float f) {
		constraint.setting.tau = f;
	}

	@Override
	public void setPivotB(Vector3f newPos) {
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

}
