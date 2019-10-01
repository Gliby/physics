package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.dispatch.CollisionObject;
import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionObject;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import net.minecraft.entity.Entity;

/**
 *
 */
public class JavaCollisionObject implements ICollisionObject {

    protected PhysicsWorld physicsWorld;
    Entity owner;
    private CollisionObject object;

    JavaCollisionObject(PhysicsWorld physicsWorld, CollisionObject object) {
        this.physicsWorld = physicsWorld;
        this.object = object;
    }

    JavaCollisionObject(PhysicsWorld physicsWorld, Entity owner, CollisionObject object) {
        this(physicsWorld, object);
        this.owner = owner;
    }

    @Override
    public Object getCollisionObject() {
        return object;
    }

    @Override
    public void setWorldTransform(final Transform transform) {

        object.setWorldTransform(transform);

    }

    @Override
    public void setCollisionShape(final ICollisionShape iCollisionShape) {

        object.setCollisionShape((CollisionShape) iCollisionShape.getCollisionShape());

    }

    @Override
    public void setCollisionFlags(final int characterObject) {

        object.setCollisionFlags(characterObject);
    }

    @Override
    public void setInterpolationWorldTransform(final Transform transform) {

        object.setInterpolationWorldTransform(transform);

    }

    @Override
    public boolean isValid() {
        return getCollisionObject() != null;
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
