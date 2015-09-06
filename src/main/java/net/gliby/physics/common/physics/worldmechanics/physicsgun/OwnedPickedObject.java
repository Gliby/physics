/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.worldmechanics.physicsgun;

import javax.vecmath.Vector3f;

import net.gliby.physics.common.physics.IRayResult;
import net.gliby.physics.common.physics.IRigidBody;
import net.minecraft.entity.Entity;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.dynamics.RigidBody;

/**
 *
 */
public class OwnedPickedObject extends PickedObject {

	private final Entity owner;

	/**
	 * @return the owner
	 */
	public Entity getOwner() {
		return owner;
	}

	/**
	 * @param rayCallback
	 * @param rayFromWorld
	 * @param rayToWorld
	 */
	public OwnedPickedObject(IRigidBody body, Entity owner, IRayResult rayCallback, Vector3f rayFromWorld, Vector3f rayToWorld) {
		super(body, rayCallback, rayFromWorld, rayToWorld);
		this.owner = owner;
	}

}
