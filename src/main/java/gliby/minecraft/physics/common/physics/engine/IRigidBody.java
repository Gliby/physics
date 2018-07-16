package gliby.minecraft.physics.common.physics.engine;

import java.util.Map;


import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;

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
	public Vector3 getAngularVelocity(Vector3 vector3f);

	/**
	 * @param vector3f
	 * @return
	 */
	public Vector3 getLinearVelocity(Vector3 vector3f);

	/**
	 * @param centerOfMass
	 * @return
	 */
	public Vector3 getCenterOfMassPosition(Vector3 centerOfMass);

	/**
	 * @param transform
	 * @return
	 */
	public Matrix4 getWorldTransform(Matrix4 transform);

	/**
	 * @param transform
	 */
	public void setWorldTransform(Matrix4 transform);

	/**
	 * @param vector3f
	 */
	public void setGravity(Vector3 vector3f);

	/**
	 * @param friction
	 */
	public void setFriction(float friction);

	/**
	 * @param linearVelocity
	 */
	public void setLinearVelocity(Vector3 linearVelocity);

	/**
	 * @param angularVelocity
	 */
	public void setAngularVelocity(Vector3 angularVelocity);

	/**
	 * @param direction
	 */
	public void applyCentralImpulse(Vector3 direction);

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
	public void getAabb(Vector3 min, Vector3 max);

	/**
	 * @param transform
	 * @return
	 */
	public Matrix4 getCenterOfMassTransform(Matrix4 transform);

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
	public void applyCentralForce(Vector3 force);

	/**
	 * 
	 * @param gravity
	 * @return
	 */
	public Vector3 getGravity(Vector3 gravity);

	public void applyTorque(Vector3 vector);

	public void applyTorqueImpulse(Vector3 vector);
}
