package gliby.minecraft.physics.common.physics.engine.nativebullet;

import javax.vecmath.Vector3f;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;

import gliby.minecraft.physics.common.physics.engine.IRayResult;

/**
 *
 */
class NativeClosestRayResultCallback implements IRayResult {

	private ClosestRayResultCallback callback;

	/**
	 * @param callback
	 */
	NativeClosestRayResultCallback(ClosestRayResultCallback callback) {
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
	public Vector3 getHitPointWorld() {
		Vector3 vec = new Vector3();
		callback.getHitPointWorld(vec);
		return vec;
	}

	@Override
	public Vector3 getHitPointNormal() {
		Vector3 vec = new Vector3();
		callback.getHitNormalWorld(vec);
		return vec;
	}
}
