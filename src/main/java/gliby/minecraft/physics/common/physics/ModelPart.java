package gliby.minecraft.physics.common.physics;

import net.minecraft.client.model.ModelBox;

import javax.vecmath.Vector3f;

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
