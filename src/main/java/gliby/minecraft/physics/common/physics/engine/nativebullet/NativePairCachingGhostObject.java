package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.client.render.VecUtility;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.IGhostObject;
import net.minecraft.entity.Entity;

import java.lang.ref.SoftReference;

/**
 *
 */
class NativePairCachingGhostObject implements IGhostObject {

    protected SoftReference<NativePhysicsWorld> physicsWorld;
    protected SoftReference<Entity> owner;
    private SoftReference<btPairCachingGhostObject> ghostObject;

    NativePairCachingGhostObject(PhysicsWorld physicsWorld, btPairCachingGhostObject object) {
        this.physicsWorld = new SoftReference<NativePhysicsWorld>((NativePhysicsWorld) physicsWorld);
        this.ghostObject = new SoftReference<btPairCachingGhostObject>(object);
    }

    NativePairCachingGhostObject(PhysicsWorld physicsWorld, Entity owner, btPairCachingGhostObject object) {
        this(physicsWorld, object);
        this.owner = new SoftReference<Entity>(owner);
    }

    @Override
    public Object getGhostObject() {
        return ghostObject.get();
    }

    @Override
    public Object getCollisionObject() {
        return ghostObject.get();
    }

    @Override
    public void setWorldTransform(final Transform entityTransform) {
        ghostObject.get().setWorldTransform(VecUtility.toMatrix4(entityTransform));

    }

    @Override
    public void setCollisionShape(final ICollisionShape collisionShape) {
        ghostObject.get().setCollisionShape((btCollisionShape) collisionShape.getCollisionShape());
    }

    @Override
    public void setCollisionFlags(final int characterObject) {
        ghostObject.get().setCollisionFlags(characterObject);
    }

    @Override
    public void setInterpolationWorldTransform(final Transform entityTransform) {
        ghostObject.get().setInterpolationWorldTransform(VecUtility.toMatrix4(entityTransform));
    }

    @Override
    public boolean isValid() {
        return ghostObject != null && ghostObject.get() != null && !ghostObject.get().isDisposed();
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
