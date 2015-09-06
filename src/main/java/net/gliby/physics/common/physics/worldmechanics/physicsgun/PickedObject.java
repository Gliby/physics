/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.worldmechanics.physicsgun;

import javax.vecmath.Vector3f;

import net.gliby.physics.common.physics.IConstraint;
import net.gliby.physics.common.physics.IRayResult;
import net.gliby.physics.common.physics.IRigidBody;

import com.bulletphysics.dynamics.constraintsolver.Point2PointConstraint;
import com.bulletphysics.linearmath.Transform;

/**
 *
 */
public class PickedObject {

	private Transform centerOfMassTransform;

	/**
	 * @return the angularVelocity
	 */
	public Transform getOriginalCenterOfMassTransform() {
		return centerOfMassTransform;
	}

	/**
	 * @param angularVelocity
	 *            the angularVelocity to set
	 */
	public void setOriginalCenterOfMassTransform(Transform angularVelocity) {
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
	private final Vector3f rayFromWorld, rayToWorld;
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
	PickedObject(IRigidBody body, IRayResult rayCallback2, Vector3f rayFromWorld, Vector3f rayToWorld) {
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
	public Vector3f getRayFromWorld() {
		return rayFromWorld;
	}

	/**
	 * @return the rayToWorld
	 */
	public Vector3f getRayToWorld() {
		return rayToWorld;
	}
}
