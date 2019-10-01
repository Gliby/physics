package gliby.minecraft.physics.common.physics.engine;

import com.bulletphysicsx.linearmath.Transform;

import javax.vecmath.Vector3f;
import java.util.Map;

/**
 *
 */
public interface IRigidBody extends ICollisionObject {

    /**
     * @return
     */
    Object getBody();

    ICollisionShape getCollisionShape();

    /**
     * @return
     */
    boolean isActive();

    /**
     * @param vector3f
     * @return
     */
    Vector3f getAngularVelocity(Vector3f vector3f);

    /**
     * @param vector3f
     * @return
     */
    Vector3f getLinearVelocity(Vector3f vector3f);

    /**
     * @param centerOfMass
     * @return
     */
    Vector3f getCenterOfMassPosition(Vector3f centerOfMass);

    /**
     * @param transform
     * @return
     */
    Transform getWorldTransform(Transform transform);

    /**
     * @param transform
     */
    void setWorldTransform(Transform transform);

    /**
     * @param vector3f
     */
    void setGravity(Vector3f vector3f);

    /**
     * @param friction
     */
    void setFriction(float friction);

    /**
     * @param linearVelocity
     */
    void setLinearVelocity(Vector3f linearVelocity);

    /**
     * @param angularVelocity
     */
    void setAngularVelocity(Vector3f angularVelocity);

    /**
     * @param direction
     */
    void applyCentralImpulse(Vector3f direction);

    /**
     * @return
     */
    boolean hasContactResponse();

    /**
     * @return
     */
    float getInvMass();

    /**
     *
     */
    void activate();

    /**
     * @param vector3f
     * @param vector3f2
     */
    void getAabb(Vector3f vector3f, Vector3f vector3f2);

    /**
     * @param transform
     * @return
     */
    Transform getCenterOfMassTransform(Transform transform);

    /**
     * @return
     */

    /**
     * @return
     */
    Map<String, Object> getProperties();

    /**
     * @param force
     */
    void applyCentralForce(Vector3f force);

    /**
     * @param gravity
     * @return
     */
    Vector3f getGravity(Vector3f gravity);

    Vector3f getCenterOfMassPosition();

    IQuaternion getRotation();

    IVector3 getPosition();

    void applyTorque(Vector3f vector);

    void applyTorqueImpulse(Vector3f vector);

    void dispose();
}
