package gliby.minecraft.physics.common.physics.mechanics.physicsgun;

import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.common.physics.engine.IConstraint;
import gliby.minecraft.physics.common.physics.engine.IRayResult;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;

import javax.vecmath.Vector3f;

/**
 *
 */
public class PickedObject {

    private final IRayResult rayCallback;
    /**
     * Raw rays, for the actual pick result use rayCallback.
     */
    private final Vector3f rayFromWorld, rayToWorld;
    private final IRigidBody rigidBody;
    private Transform centerOfMassTransform;
    private float pickDistance;
    private IConstraint constraint;

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
     * @return the angularVelocity
     */
    public Transform getOriginalCenterOfMassTransform() {
        return centerOfMassTransform;
    }

    /**
     * @param angularVelocity the angularVelocity to set
     */
    public void setOriginalCenterOfMassTransform(Transform angularVelocity) {
        this.centerOfMassTransform = angularVelocity;
    }

    /**
     * @return the pickDistance
     */
    public float getPickDistance() {
        return pickDistance;
    }

    /**
     * @param distance the distance to set
     */
    public void setPickDistance(float distance) {
        this.pickDistance = distance;
    }

    /**
     * @return the constraint
     */
    public IConstraint getConstraint() {
        return constraint;
    }

    /**
     * @param p2p the constraint to set
     */
    public void setConstraint(IConstraint p2p) {
        this.constraint = p2p;
    }

    /**
     * @return the rigidBody
     */
    public IRigidBody getRigidBody() {
        return rigidBody;
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
