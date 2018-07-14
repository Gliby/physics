package gliby.minecraft.physics.common.physics.engine.concurrent.javabullet;

import javax.vecmath.Vector3f;

import com.bulletphysicsx.collision.dispatch.CollisionWorld;

import gliby.minecraft.physics.common.physics.engine.IRayResult;

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
