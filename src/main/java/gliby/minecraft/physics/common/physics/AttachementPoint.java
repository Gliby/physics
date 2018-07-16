package gliby.minecraft.physics.common.physics;


import com.badlogic.gdx.math.Vector3;

/**
 *
 */
public class AttachementPoint {

	private final Vector3 position;
	public ModelPart bodyA, bodyB;

	/**
	 * @param point
	 * @param bodyA
	 * @param bodyB
	 */
	public AttachementPoint(Vector3 point) {
		this.position = point;
	}

	public Vector3 getPosition() {
		return position;
	}

	/**
	 * @param bodyA the bodyA to set
	 */
	public void setBodyA(ModelPart bodyA) {
		this.bodyA = bodyA;
	}

	/**
	 * @param bodyB the bodyB to set
	 */
	public void setBodyB(ModelPart bodyB) {
		this.bodyB = bodyB;
	}

	public ModelPart getBodyA() {
		return bodyA;
	}

	public ModelPart getBodyB() {
		return bodyB;
	}
}
