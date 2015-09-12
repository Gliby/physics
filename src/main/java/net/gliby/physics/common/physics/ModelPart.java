/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics;

import javax.vecmath.Vector3f;

import net.minecraft.client.model.ModelBox;

/**
 *
 */
//TODO Remove
public class ModelPart {

	private final Vector3f position;
	private final ModelBox modelBox;
	/**
	 * @param position
	 * @param modelBox
	 */
	public ModelPart(Vector3f position, ModelBox modelBox) {
		this.position = position;
		this.modelBox = modelBox;
	}
	/**
	 * @return the position
	 */
	public Vector3f getPosition() {
		return position;
	}
	/**
	 * @return the modelBox
	 */
	public ModelBox getModelBox() {
		return modelBox;
	}
	
}
