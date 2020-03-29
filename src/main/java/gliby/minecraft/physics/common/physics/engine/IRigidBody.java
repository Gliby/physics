package gliby.minecraft.physics.common.physics.engine;

import com.bulletphysicsx.linearmath.Transform;

import javax.vecmath.Matrix4f;
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

    /**
     * @param angularVelocity
     */
    void setAngularVelocity(Vector3f angularVelocity);

    Vector3f getLinearVelocity();

    /**
     * @param linearVelocity
     */
    void setLinearVelocity(Vector3f linearVelocity);

    Vector3f getCenterOfMassPosition();

    Transform getWorldTransform();

    Matrix4f getWorldMatrix();

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
     * @return
     */
    Transform getCenterOfMassTransform();

    /**
     * @return
     */
    Map<String, Object> getProperties();

    /**
     * @param force
     */
    void applyCentralForce(Vector3f force);

    /**
     *
     * @param force
     * @param relativePosition
     */
    void applyForce(Vector3f force, Vector3f relativePosition);

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
