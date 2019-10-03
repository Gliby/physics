package gliby.minecraft.physics.common.physics.engine;

import com.bulletphysicsx.linearmath.Transform;

import javax.vecmath.Quat4f;
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


    Vector3f getAngularVelocity();


    Vector3f getLinearVelocity();


    Vector3f getCenterOfMassPosition();

    Transform getWorldTransform();

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
    Transform getCenterOfMassTransform();

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

    Quat4f getRotation();

    Vector3f getPosition();

    void applyTorque(Vector3f vector);

    void applyTorqueImpulse(Vector3f vector);

    void dispose();
}
