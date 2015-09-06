/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.block;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import com.google.gson.annotations.SerializedName;

import net.gliby.physics.common.physics.entitymechanics.RigidBodyMechanic;

/**
 *
 */
public class PhysicsBlockMetadata {

	public boolean shouldSpawnInExplosion = true;
	public float mass;
	public float friction;
	public boolean overrideCollisionShape;
	// TODO Re-implement
	public float restitution;
	// Cannot be serialized without guide!
	public List<RigidBodyMechanic> mechanics = new ArrayList<RigidBodyMechanic>();

	public boolean collisionEnabled = true;

}