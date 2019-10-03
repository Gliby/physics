package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import gliby.minecraft.physics.client.render.ConversionUtility;
import gliby.minecraft.physics.common.physics.engine.IRayResult;

import javax.vecmath.Vector3f;

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
    public Vector3f getHitPointWorld() {
        Vector3 vec = new Vector3();
        callback.getHitPointWorld(vec);
        return ConversionUtility.toVector3f(vec);
    }

    @Override
    public Vector3f getHitPointNormal() {
        Vector3 vector = new Vector3();
        callback.getHitNormalWorld(vector);
        return ConversionUtility.toVector3f(vector);
    }
}
