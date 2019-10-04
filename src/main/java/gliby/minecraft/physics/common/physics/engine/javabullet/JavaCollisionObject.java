package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.dispatch.CollisionObject;
import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionObject;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import net.minecraft.entity.Entity;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 *
 */
public class JavaCollisionObject implements ICollisionObject {

    protected SoftReference<PhysicsWorld> physicsWorld;
    protected SoftReference<Entity> owner;
    private SoftReference<CollisionObject> collisionObject;

    JavaCollisionObject(PhysicsWorld physicsWorld, CollisionObject object) {
        this.physicsWorld = new SoftReference<PhysicsWorld>(physicsWorld);
        this.collisionObject = new SoftReference<CollisionObject>(object);
    }

    JavaCollisionObject(PhysicsWorld physicsWorld, Entity owner, CollisionObject object) {
        this(physicsWorld, object);
        this.owner = new SoftReference<Entity>(owner);
    }

    @Override
    public Object getCollisionObject() {
        return collisionObject.get();
    }

    @Override
    public void setWorldTransform(final Transform transform) {
        collisionObject.get().setWorldTransform(transform);

    }

    @Override
    public void setCollisionShape(final ICollisionShape iCollisionShape) {
        collisionObject.get().setCollisionShape((CollisionShape) iCollisionShape.getCollisionShape());
    }

    @Override
    public void setCollisionFlags(final int characterObject) {
        collisionObject.get().setCollisionFlags(characterObject);
    }

    @Override
    public void setInterpolationWorldTransform(final Transform transform) {
        collisionObject.get().setInterpolationWorldTransform(transform);
    }

    @Override
    public boolean isValid() {
        return collisionObject != null && getCollisionObject() != null;
    }

    @Override
    public Entity getOwner() {
        return owner.get();
    }

    @Override
    public PhysicsWorld getPhysicsWorld() {
        return physicsWorld.get();
    }
}
