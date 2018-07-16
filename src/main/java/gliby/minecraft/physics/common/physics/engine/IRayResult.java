package gliby.minecraft.physics.common.physics.engine;

import com.badlogic.gdx.math.Vector3;

/**
 *
 */
public interface IRayResult {

	public Object getRayResultCallback();

	/**
	 * @return
	 */
	public boolean hasHit();

	/**
	 * @return
	 */
	public Object getCollisionObject();

	/**
	 * @return
	 */
	public Vector3 getHitPointWorld();

	/**
	 * @return
	 */
	public Vector3 getHitPointNormal();
	
}
