package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.client.render.VecUtility;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.Entity;
import org.terasology.math.geom.Matrix4f;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
class NativeRigidBody extends NativeCollisionObject implements IRigidBody {

    private SoftReference<btRigidBody> rigidBody;
    private ICollisionShape collisionShape;
    private Map<String, Object> properties;

    public NativeRigidBody(PhysicsWorld physicsWorld, btRigidBody rigidBody, ICollisionShape collisionShape, Entity entity) {
        super(physicsWorld, entity, rigidBody);
        this.physicsWorld = new SoftReference<NativePhysicsWorld>((NativePhysicsWorld) physicsWorld);
        this.rigidBody = new SoftReference<btRigidBody>(rigidBody);
        this.collisionShape = collisionShape;
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
        return VecUtility.toVector3f(rigidBody.get().getAngularVelocity());
    }

    @Override
    public void setAngularVelocity(Vector3f angularVelocity) {
        rigidBody.get().setAngularVelocity(VecUtility.toVector3fTera(angularVelocity));

    }

    @Override
    public Vector3f getLinearVelocity() {
        return VecUtility.toVector3f(rigidBody.get().getLinearVelocity());

    }

    @Override
    public void setLinearVelocity(final Vector3f linearVelocity) {
        rigidBody.get().setLinearVelocity(VecUtility.toVector3fTera(linearVelocity));

    }

    @Override
    public Transform getWorldTransform() {
        return VecUtility.toTransform(rigidBody.get().getWorldTransform());
    }

    @Override
    public Transform getWorldTransformRef() {
        return VecUtility.toTransformRef(rigidBody.get().getWorldTransform());
    }

    @Override
    public javax.vecmath.Matrix4f getWorldMatrixRef() {
        return VecUtility.toMatrix4fRef(rigidBody.get().getWorldTransform());
    }

    @Override
    public void setWorldTransform(final Transform transform) {
        Matrix4f mat4 = VecUtility.toMatrix4fTera(transform);
        rigidBody.get().setWorldTransform(mat4);

    }

    @Override
    public boolean isValid() {
        return rigidBody != null && rigidBody.get() != null && !rigidBody.get().isDisposed();
    }

    @Override
    public void setGravity(final Vector3f acceleration) {
        rigidBody.get().setGravity(VecUtility.toVector3fTera(acceleration));

    }

    @Override
    public void setFriction(final float friction) {
        rigidBody.get().setFriction(friction);

    }

    @Override
    public void applyCentralImpulse(final Vector3f direction) {
        rigidBody.get().applyCentralImpulse(VecUtility.toVector3fTera(direction));

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
        org.terasology.math.geom.Vector3f min = new org.terasology.math.geom.Vector3f (), max = new org.terasology.math.geom.Vector3f ();
        rigidBody.get().getAabb(min, max);
        aabbMin.set(VecUtility.toVector3f(min));
        aabbMin.set(VecUtility.toVector3f(max));

    }

    @Override
    public Transform getCenterOfMassTransform() {
        return VecUtility.toTransform(rigidBody.get().getCenterOfMassTransform());
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void applyCentralForce(final Vector3f force) {
        rigidBody.get().applyCentralForce(VecUtility.toVector3fTera(force));

    }

    @Override
    public void applyForce(Vector3f force, Vector3f relativePosition) {
        rigidBody.get().applyForce(VecUtility.toVector3fTera(force), VecUtility.toVector3fTera(relativePosition));
    }

    @Override
    public Vector3f getGravity(Vector3f vector3f) {
        vector3f.set(VecUtility.toVector3f(rigidBody.get().getGravity()));
        return vector3f;
    }

    @Override
    public Vector3f getCenterOfMassPosition() {
        return VecUtility.toVector3f(rigidBody.get().getCenterOfMassPosition());
    }

    @Override
    public Quat4f getRotation() {
        return VecUtility.toQuat4f(rigidBody.get().getOrientation());
    }

    @Override
    public Vector3f getPosition() {
        return VecUtility.toVector3f(rigidBody.get().getWorldTransform().getTranslation());
    }

    @Override
    public void applyTorque(final Vector3f vector) {
        rigidBody.get().applyTorque(VecUtility.toVector3fTera(vector));

    }

    @Override
    public void applyTorqueImpulse(final Vector3f vector) {
        rigidBody.get().applyTorqueImpulse(VecUtility.toVector3fTera(vector));
    }

    @Override
    public void dispose() {
        if (isValid()) {
            rigidBody.get().getMotionState().dispose();
            rigidBody.get().getCollisionShape().dispose();
            rigidBody.get().dispose();
        }
        rigidBody.clear();
        collisionShape.dispose();
        properties.clear();
    }
}