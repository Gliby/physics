/**
 * Copyright (c) 2015, Mine Fortress.
 */
package gliby.minecraft.physics.common.physics.mechanics.physicsgun;

import javax.vecmath.Vector3f;

import gliby.minecraft.physics.common.physics.engine.IRayResult;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.Entity;

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
