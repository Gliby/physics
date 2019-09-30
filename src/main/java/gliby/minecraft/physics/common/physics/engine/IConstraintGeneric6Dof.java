package gliby.minecraft.physics.common.physics.engine;

import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.common.physics.PhysicsWorld;

/**
 *
 */
public interface IConstraintGeneric6Dof extends IConstraint {

    PhysicsWorld getPhysicsWorld();

    /**
     * @param transform
     * @return
     */
    Transform getGlobalFrameOffsetA(Transform transform);

    /**
     * @param transform
     * @return
     */
    Transform getGlobalFrameOffsetB(Transform transform);

}
