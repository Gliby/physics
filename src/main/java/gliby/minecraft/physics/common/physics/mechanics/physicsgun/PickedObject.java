package gliby.minecraft.physics.common.physics.mechanics.physicsgun;

import org.lwjgl.util.vector.Matrix;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import gliby.minecraft.physics.common.physics.engine.IConstraint;
import gliby.minecraft.physics.common.physics.engine.IRayResult;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;

/**
 *
 */
public class PickedObject {

	private Matrix4 centerOfMassTransform;

	/**
	 * @return the angularVelocity
	 */
	public Matrix4 getOriginalCenterOfMassTransform() {
		return centerOfMassTransform;
	}

	/**
	 * @param angularVelocity
	 *            the angularVelocity to set
	 */
	public void setOriginalCenterOfMassTransform(Matrix4 angularVelocity) {
		this.centerOfMassTransform = angularVelocity;
	}

	private float pickDistance;

	/**
	 * @return the pickDistance
	 */
	public float getPickDistance() {
		return pickDistance;
	}

	/**
	 * @param distance
	 *            the distance to set
	 */
	public void setPickDistance(float distance) {
		this.pickDistance = distance;
	}

	private IConstraint constraint;

	/**
	 * @return the constraint
	 */
	public IConstraint getConstraint() {
		return constraint;
	}

	/**
	 * @param p2p
	 *            the constraint to set
	 */
	public void setConstraint(IConstraint p2p) {
		this.constraint = p2p;
	}

	private final IRayResult rayCallback;
	/**
	 * Raw rays, for the actual pick result use rayCallback.
	 */
	private final Vector3 rayFromWorld, rayToWorld;
	private final IRigidBody rigidBody;

	/**
	 * @return the rigidBody
	 */
	public IRigidBody getRigidBody() {
		return rigidBody;
	}

	/**
	 * @param rayCallback2
	 * @param rayFromWorld
	 * @param rayToWorld
	 */
	PickedObject(IRigidBody body, IRayResult rayCallback2, Vector3 rayFromWorld, Vector3 rayToWorld) {
		this.rigidBody = body;
		this.rayCallback = rayCallback2;
		this.rayFromWorld = rayFromWorld;
		this.rayToWorld = rayToWorld;
	}

	/**
	 * @return the rayCallback
	 */
	public IRayResult getRayCallback() {
		return rayCallback;
	}

	/**
	 * @return the rayFromWorld
	 */
	public Vector3 getRayFromWorld() {
		return rayFromWorld;
	}

	/**
	 * @return the rayToWorld
	 */
	public Vector3 getRayToWorld() {
		return rayToWorld;
	}
}
