/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics;

import javax.vecmath.Vector3f;

import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;

/**
 *
 */
public interface IConstraint {

	/**
	 * @return
	 */
	Object getConstraint();

	/**
	 * @return
	 */
	boolean isPoint2Point();

	/**
	 * @return
	 */
	boolean isGeneric6Dof();
	
	boolean isSlider();
}
