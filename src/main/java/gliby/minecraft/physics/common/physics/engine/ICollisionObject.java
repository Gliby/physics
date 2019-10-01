package gliby.minecraft.physics.common.physics.engine;

import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.entity.Entity;

/**
 *
 */
public interface ICollisionObject {

    PhysicsWorld getPhysicsWorld();

    Entity getOwner();


    Object getCollisionObject();

    /**
     * @param entityTransform
     */
    void setWorldTransform(Transform entityTransform);

    /**
     * @param iCollisionShape
     */
    void setCollisionShape(ICollisionShape iCollisionShape);

    /**
     * @param characterObject
     */
    void setCollisionFlags(int characterObject);

    /**
     * @param entityTransform
     */
    void setInterpolationWorldTransform(Transform entityTransform);

    // Returns whether the RigidBody is valid.
    boolean isValid();

}
