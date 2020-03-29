package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import gliby.minecraft.physics.client.render.VecUtility;
import gliby.minecraft.physics.common.physics.engine.IRayResult;

import javax.vecmath.Vector3f;
import java.lang.ref.SoftReference;

/**
 *
 */
class NativeClosestRayResultCallback implements IRayResult {

    private SoftReference<ClosestRayResultCallback> callback;

    /**
     * @param callback
     */
    NativeClosestRayResultCallback(ClosestRayResultCallback callback) {
        this.callback = new SoftReference<ClosestRayResultCallback>(callback);
    }

    @Override
    public Object getRayResultCallback() {
        return callback.get();
    }

    @Override
    public boolean hasHit() {
        return callback.get().hasHit();
    }

    @Override
    public Object getCollisionObject() {
        return callback.get().getCollisionObject();
    }

    @Override
    public Vector3f getHitPointWorld() {
        org.terasology.math.geom.Vector3f vec = new org.terasology.math.geom.Vector3f();
        callback.get().getHitPointWorld(vec);
        return VecUtility.toVector3f(vec);
    }

    @Override
    public Vector3f getHitPointNormal() {
        org.terasology.math.geom.Vector3f vector = new org.terasology.math.geom.Vector3f();
        callback.get().getHitNormalWorld(vector);
        return VecUtility.toVector3f(vector);
    }
}
