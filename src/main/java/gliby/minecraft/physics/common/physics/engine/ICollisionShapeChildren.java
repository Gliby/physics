package gliby.minecraft.physics.common.physics.engine;

import com.bulletphysicsx.linearmath.Transform;

/**
 *
 */
public interface ICollisionShapeChildren {

    Transform getTransform();

    ICollisionShape getCollisionShape();

}
