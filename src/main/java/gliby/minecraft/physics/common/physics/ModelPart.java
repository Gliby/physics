package gliby.minecraft.physics.common.physics;


import com.badlogic.gdx.math.Vector3;

import net.minecraft.client.model.ModelBox;

public class ModelPart {

	private final Vector3 position;
	private final ModelBox modelBox;
	/**
	 * @param position
	 * @param modelBox
	 */
	public ModelPart(Vector3 position, ModelBox modelBox) {
		this.position = position;
		this.modelBox = modelBox;
	}
	/**
	 * @return the position
	 */
	public Vector3 getPosition() {
		return position;
	}
	/**
	 * @return the modelBox
	 */
	public ModelBox getModelBox() {
		return modelBox;
	}
	
}
