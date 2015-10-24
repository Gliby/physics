package net.gliby.physics.common.physics.engine.javabullet;

import com.bulletphysics.dynamics.constraintsolver.SliderConstraint;

import net.gliby.physics.common.physics.engine.IConstraintSlider;

public class JavaConstraintSlider implements IConstraintSlider {

	private SliderConstraint constraint;

	JavaConstraintSlider(SliderConstraint sliderConstraint) {
		this.constraint = sliderConstraint;
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
		return false;
	}

	@Override
	public boolean isSlider() {
		return true;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
