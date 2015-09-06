package net.gliby.physics.common.physics;

import java.util.List;

import javax.vecmath.Vector3f;

/**
 * Ropes are pretty much just inertial-less spheres, attached to each other.
 *
 */
public interface IRope {

	public List<Vector3f> getSpherePositions();

	void create(PhysicsWorld physicsWorld);
	
	public int getDetail();
	public Vector3f getStartPosition();
	public Vector3f getEndPosition();

	void dispose(PhysicsWorld physicsWorld);
}
