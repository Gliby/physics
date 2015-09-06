package net.gliby.physics.common.physics.jbullet;

import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.bulletphysics.dynamics.constraintsolver.SliderConstraint;

import net.gliby.physics.common.physics.IConstraintSlider;

public class JBulletConstraintSlider implements IConstraintSlider {

	private SliderConstraint constraint;

	JBulletConstraintSlider(SliderConstraint sliderConstraint) {
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

}
