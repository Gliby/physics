/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.swig;

import javax.vecmath.Vector3f;

import net.gliby.physics.common.physics.IRayResult;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;

/**
 *
 */
class BulletClosestRayResultCallback implements IRayResult {

	private ClosestRayResultCallback callback;

	/**
	 * @param callback
	 */
	BulletClosestRayResultCallback(ClosestRayResultCallback callback) {
		this.callback = callback;
	}

	@Override
	public Object getRayResultCallback() {
		return callback;
	}

	@Override
	public boolean hasHit() {
		return callback.hasHit();
	}

	@Override
	public Object getCollisionObject() {
		return callback.getCollisionObject();
	}

	@Override
	public Vector3f getHitPointWorld() {
		Vector3 vec = new Vector3();
		callback.getHitPointWorld(vec);
		return BulletPhysicsWorld.toVector3f(vec);
	}

	@Override
	public Vector3f getHitPointNormal() {
		Vector3 vector = new Vector3();
		callback.getHitNormalWorld(vector);
		return BulletPhysicsWorld.toVector3f(vector);
	}

}
