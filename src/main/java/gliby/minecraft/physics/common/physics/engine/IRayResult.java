package gliby.minecraft.physics.common.physics.engine;

import javax.vecmath.Vector3f;

/**
 *
 */
public interface IRayResult {

    Object getRayResultCallback();

    /**
     * @return
     */
    boolean hasHit();

    /**
     * @return
     */
    Object getCollisionObject();

    /**
     * @return
     */
    Vector3f getHitPointWorld();

    /**
     * @return
     */
    Vector3f getHitPointNormal();

}
