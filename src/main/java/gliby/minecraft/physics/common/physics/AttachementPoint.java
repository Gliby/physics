package gliby.minecraft.physics.common.physics;

import javax.vecmath.Vector3f;

/**
 *
 */
public class AttachementPoint {

    private final Vector3f position;
    public ModelPart bodyA, bodyB;

    /**
     * @param point
     */
    public AttachementPoint(Vector3f point) {
        this.position = point;
    }

    public Vector3f getPosition() {
        return position;
    }

    public ModelPart getBodyA() {
        return bodyA;
    }

    /**
     * @param bodyA the bodyA to set
     */
    public void setBodyA(ModelPart bodyA) {
        this.bodyA = bodyA;
    }

    public ModelPart getBodyB() {
        return bodyB;
    }

    /**
     * @param bodyB the bodyB to set
     */
    public void setBodyB(ModelPart bodyB) {
        this.bodyB = bodyB;
    }
}
