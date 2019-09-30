package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.dispatch.PairCachingGhostObject;
import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.IGhostObject;
import net.minecraft.entity.Entity;

/**
 *
 */
public class JavaPairCachingGhostObject implements IGhostObject {

    protected PhysicsWorld physicsWorld;
    Entity owner;
    private PairCachingGhostObject object;

    JavaPairCachingGhostObject(PhysicsWorld physicsWorld, PairCachingGhostObject object) {
        this.physicsWorld = physicsWorld;
        this.object = object;
    }

    JavaPairCachingGhostObject(PhysicsWorld physicsWorld, Entity entity, PairCachingGhostObject object) {
        this(physicsWorld, object);
        this.owner = entity;
    }


    @Override
    public Object getGhostObject() {
        return object;
    }

    @Override
    public Object getCollisionObject() {
        return object;
    }

    @Override
    public void setWorldTransform(final Transform entityTransform) {

        object.setWorldTransform(entityTransform);

    }

    @Override
    public void setCollisionShape(final ICollisionShape colllisionShape) {

        object.setCollisionShape((CollisionShape) colllisionShape.getCollisionShape());

    }

    @Override
    public void setCollisionFlags(final int characterObject) {

        object.setCollisionFlags(characterObject);

    }

    @Override
    public void setInterpolationWorldTransform(final Transform entityTransform) {

        object.setInterpolationWorldTransform(entityTransform);

    }

    @Override
    public Entity getOwner() {
        return owner;
    }

    @Override
    public PhysicsWorld getPhysicsWorld() {
        return physicsWorld;
    }

}
