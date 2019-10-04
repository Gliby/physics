package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.client.render.VecUtility;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionObject;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import net.minecraft.entity.Entity;

import java.lang.annotation.Native;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 *
 */
class NativeCollisionObject implements ICollisionObject {

    protected SoftReference<NativePhysicsWorld> physicsWorld;
    protected SoftReference<Entity> owner;
    private SoftReference<btCollisionObject> collisionObject;

    NativeCollisionObject(PhysicsWorld physicsWorld, btCollisionObject object) {
        this.physicsWorld = new SoftReference<NativePhysicsWorld> ((NativePhysicsWorld) physicsWorld);
        this.collisionObject = new SoftReference<btCollisionObject>(object);
    }

    NativeCollisionObject(PhysicsWorld physicsWorld, Entity owner, btCollisionObject object) {
        this(physicsWorld, object);
        this.owner = new SoftReference<Entity>(owner);
    }

    @Override
    public Object getCollisionObject() {
        return collisionObject.get();
    }

    @Override
    public void setWorldTransform(final Transform transform) {
        collisionObject.get().setWorldTransform(VecUtility.toMatrix4(transform));

    }

    @Override
    public void setCollisionShape(final ICollisionShape shape) {
        collisionObject.get().setCollisionShape((btCollisionShape) shape.getCollisionShape());

    }

    @Override
    public void setCollisionFlags(final int characterObject) {
        collisionObject.get().setCollisionFlags(characterObject);

    }

    @Override
    public void setInterpolationWorldTransform(final Transform transform) {
        collisionObject.get().setInterpolationWorldTransform(VecUtility.toMatrix4(transform));

    }

    @Override
    public boolean isValid() {
        return collisionObject != null && getCollisionObject() != null && !collisionObject.get().isDisposed();
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
