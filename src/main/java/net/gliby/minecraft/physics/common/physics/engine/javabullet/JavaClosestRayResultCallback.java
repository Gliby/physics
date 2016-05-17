/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.physics.common.physics.engine.javabullet;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionWorld;

import net.gliby.minecraft.physics.common.physics.engine.IRayResult;

/**
 *
 */
class JavaClosestRayResultCallback implements IRayResult {

	private CollisionWorld.ClosestRayResultCallback rayCallback;

	/**
	 * 
	 */
	JavaClosestRayResultCallback(CollisionWorld.ClosestRayResultCallback rayCallback) {
		this.rayCallback = rayCallback;
	}

	@Override
	public Object getRayResultCallback() {
		return rayCallback;
	}

	@Override
	public boolean hasHit() {
		return rayCallback.hasHit();
	}

	@Override
	public Object getCollisionObject() {
		return rayCallback.collisionObject;
	}

	@Override
	public Vector3f getHitPointWorld() {
		return rayCallback.hitPointWorld;
	}

	@Override
	public Vector3f getHitPointNormal() {
		return rayCallback.hitNormalWorld;
	}

}
