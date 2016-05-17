/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.physics.common.physics.engine;

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
