/**
 * Copyright (c) 2015, Mine Fortress.
 */
package gliby.minecraft.physics.common.physics.engine;

import java.util.Map;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

import net.minecraft.entity.Entity;

/**
 *
 */
public interface IRigidBody extends ICollisionObject {

	/**
	 * @return
	 */
	public Object getBody();

	public ICollisionShape getCollisionShape();

	/**
	 * @return
	 */
	public boolean isActive();

	/**
	 * @param vector3f
	 * @return
	 */
	public Vector3f getAngularVelocity(Vector3f vector3f);

	/**
	 * @param vector3f
	 * @return
	 */
	public Vector3f getLinearVelocity(Vector3f vector3f);

	/**
	 * @param centerOfMass
	 * @return
	 */
	public Vector3f getCenterOfMassPosition(Vector3f centerOfMass);

	/**
	 * @param transform
	 * @return
	 */
	public Transform getWorldTransform(Transform transform);

	/**
	 * @param transform
	 */
	public void setWorldTransform(Transform transform);

	/**
	 * @param vector3f
	 */
	public void setGravity(Vector3f vector3f);

	/**
	 * @param friction
	 */
	public void setFriction(float friction);

	/**
	 * @param linearVelocity
	 */
	public void setLinearVelocity(Vector3f linearVelocity);

	/**
	 * @param angularVelocity
	 */
	public void setAngularVelocity(Vector3f angularVelocity);

	/**
	 * @param direction
	 */
	public void applyCentralImpulse(Vector3f direction);

	/**
	 * @return
	 */
	public boolean hasContactResponse();

	/**
	 * @return
	 */
	public float getInvMass();

	/**
	 * 
	 */
	public void activate();

	/**
	 * @param vector3f
	 * @param vector3f2
	 */
	public void getAabb(Vector3f vector3f, Vector3f vector3f2);

	/**
	 * @param transform
	 * @return
	 */
	public Transform getCenterOfMassTransform(Transform transform);

	/**
	 * @return
	 */

	/**
	 * 
	 * @return
	 */
	public Map<String, Object> getProperties();

	/**
	 * 
	 * @param force
	 */
	public void applyCentralForce(Vector3f force);

	/**
	 * 
	 * @param gravity
	 * @return
	 */
	public Vector3f getGravity(Vector3f gravity);

	public Vector3f getCenterOfMassPosition();

	public IQuaternion getRotation();

	public IVector3 getPosition();

	public void applyTorque(Vector3f vector);

	public void applyTorqueImpulse(Vector3f vector);
}
