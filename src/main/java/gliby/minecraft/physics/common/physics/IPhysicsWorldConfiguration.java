package gliby.minecraft.physics.common.physics;

import net.minecraft.world.World;

import javax.vecmath.Vector3f;

public interface IPhysicsWorldConfiguration {

    boolean shouldSimulate(World world, PhysicsWorld physicsWorld);

    int getTicksPerSecond();

    World getWorld();

    Vector3f getRegularGravity();
}
