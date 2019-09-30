package gliby.minecraft.physics.common.physics.engine;

import gliby.minecraft.physics.common.physics.PhysicsWorld;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * Ropes are pretty much just inertial-less spheres, attached to each other.
 */
public interface IRope {

    List<Vector3f> getSpherePositions();

    void create(PhysicsWorld physicsWorld);

    int getDetail();

    Vector3f getStartPosition();

    Vector3f getEndPosition();

    void dispose(PhysicsWorld physicsWorld);

}
