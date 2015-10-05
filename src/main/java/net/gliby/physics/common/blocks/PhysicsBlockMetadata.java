/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.blocks;

import java.util.ArrayList;
import java.util.List;

import net.gliby.physics.common.entity.mechanics.RigidBodyMechanic;

/**
 *
 */
public class PhysicsBlockMetadata {

	public boolean shouldSpawnInExplosion = true;
	public float mass;
	public float friction;
	public boolean defaultCollisionShape;
	// TODO Re-implement
	public float restitution;
	// Cannot be serialized without guide!
	public List<RigidBodyMechanic> mechanics = new ArrayList<RigidBodyMechanic>();

	public boolean collisionEnabled = true;

}