package gliby.minecraft.physics.common.physics.mechanics;

import gliby.minecraft.physics.common.physics.engine.IGhostObject;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;

//TODO (0.7.0) feature: finish

/**
 * Physics Mechanic event.
 */
public class PhysicsMechanicEvent {

    public static class TouchedEntity {
        IRigidBody bodyA;
        IRigidBody bodyB;

        public TouchedEntity(IRigidBody bodyA, IGhostObject ghostObject) {

        }
    }

}
