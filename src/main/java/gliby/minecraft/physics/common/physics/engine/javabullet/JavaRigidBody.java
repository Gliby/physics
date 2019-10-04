package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.dynamics.RigidBody;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.Entity;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
class JavaRigidBody extends JavaCollisionObject implements IRigidBody {

    private WeakReference<RigidBody> rigidBody;

    private ICollisionShape collisionShape;

    private Map<String, Object> properties;

    public JavaRigidBody(PhysicsWorld physicsWorld, RigidBody body, Entity owner) {
        super(physicsWorld, owner, body);
        this.rigidBody = new WeakReference<RigidBody>(body);
        this.owner = new WeakReference<Entity>(owner);
        this.collisionShape = new JavaCollisionShape(physicsWorld, body.getCollisionShape());
        this.properties = new HashMap<String, Object>();
    }

    @Override
    public Object getBody() {
        return rigidBody.get();
    }

    @Override
    public ICollisionShape getCollisionShape() {
        return collisionShape;
    }

    @Override
    public boolean isActive() {
        return rigidBody.get().isActive();
    }

    @Override
    public Vector3f getAngularVelocity() {
        return rigidBody.get().getAngularVelocity(new Vector3f());
    }

    @Override
    public Vector3f getLinearVelocity() {
        return rigidBody.get().getLinearVelocity(new Vector3f());
    }

    @Override
    public Vector3f getCenterOfMassPosition() {
        return rigidBody.get().getCenterOfMassPosition(new Vector3f());
    }

    @Override
    public Transform getWorldTransform() {
        return rigidBody.get().getWorldTransform(new Transform());
    }

    @Override
    public void setWorldTransform(final Transform transform) {
        rigidBody.get().setWorldTransform(transform);
    }

    @Override
    public void setGravity(final Vector3f acceleration) {
        rigidBody.get().setGravity(acceleration);
    }

    @Override
    public void setFriction(final float friction) {
        rigidBody.get().setFriction(friction);
    }

    @Override
    public void setLinearVelocity(final Vector3f linearVelocity) {
        rigidBody.get().setLinearVelocity(linearVelocity);
    }

    @Override
    public void setAngularVelocity(final Vector3f angularVelocity) {
        rigidBody.get().setAngularVelocity(angularVelocity);
    }

    @Override
    public void applyCentralImpulse(final Vector3f direction) {
        rigidBody.get().applyCentralImpulse(direction);
    }

    @Override
    public boolean hasContactResponse() {
        return rigidBody.get().hasContactResponse();
    }

    @Override
    public float getInvMass() {
        return rigidBody.get().getInvMass();
    }

    @Override
    public void activate() {
        rigidBody.get().activate();
    }

    @Override
    public void getAabb(Vector3f aabbMin, Vector3f aabbMax) {
        rigidBody.get().getAabb(aabbMin, aabbMax);
    }

    @Override
    public Transform getCenterOfMassTransform() {
        return rigidBody.get().getCenterOfMassTransform(new Transform());

    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void applyCentralForce(final Vector3f force) {
        rigidBody.get().applyCentralForce(force);
    }

    @Override
    public Vector3f getGravity(Vector3f vector3f) {
        return rigidBody.get().getGravity(vector3f);

    }

    @Override
    public Quat4f getRotation() {
        return rigidBody.get().getOrientation(new Quat4f());

    }

    @Override
    public Vector3f getPosition() {
        return rigidBody.get().getWorldTransform(new Transform()).origin;
    }

    @Override
    public void applyTorque(final Vector3f vector) {
        rigidBody.get().applyTorque(vector);

    }

    @Override
    public void applyTorqueImpulse(final Vector3f vector) {
        rigidBody.get().applyTorqueImpulse(vector);

    }

    @Override
    public void dispose() {
        rigidBody.get().destroy();
        rigidBody = null;
        properties.clear();
    }

}
