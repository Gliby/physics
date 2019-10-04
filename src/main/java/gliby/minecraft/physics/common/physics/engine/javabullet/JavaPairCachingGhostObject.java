package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.dispatch.PairCachingGhostObject;
import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.IGhostObject;
import net.minecraft.entity.Entity;

import java.lang.ref.WeakReference;

/**
 *
 */
public class JavaPairCachingGhostObject implements IGhostObject {

    protected WeakReference<PhysicsWorld> physicsWorld;
    protected WeakReference<Entity> owner;
    private WeakReference<PairCachingGhostObject> pairCache;

    JavaPairCachingGhostObject(PhysicsWorld physicsWorld, PairCachingGhostObject object) {
        this.physicsWorld = new WeakReference<PhysicsWorld>(physicsWorld);
        this.pairCache = new WeakReference<PairCachingGhostObject>(object);
    }

    JavaPairCachingGhostObject(PhysicsWorld physicsWorld, Entity entity, PairCachingGhostObject object) {
        this(physicsWorld, object);
        this.owner = new WeakReference<Entity>(entity);
    }


    @Override
    public Object getGhostObject() {
        return pairCache.get();
    }

    @Override
    public Object getCollisionObject() {
        return pairCache.get();
    }

    @Override
    public void setWorldTransform(final Transform entityTransform) {
        pairCache.get().setWorldTransform(entityTransform);
    }

    @Override
    public void setCollisionShape(final ICollisionShape colllisionShape) {
        pairCache.get().setCollisionShape((CollisionShape) colllisionShape.getCollisionShape());
    }

    @Override
    public void setCollisionFlags(final int characterObject) {
        pairCache.get().setCollisionFlags(characterObject);
    }

    @Override
    public void setInterpolationWorldTransform(final Transform entityTransform) {
        pairCache.get().setInterpolationWorldTransform(entityTransform);
    }

    @Override
    public boolean isValid() {
        return pairCache != null && getCollisionObject() != null;
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
