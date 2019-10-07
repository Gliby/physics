package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.dispatch.CollisionWorld;
import gliby.minecraft.physics.common.physics.engine.IRayResult;

import javax.vecmath.Vector3f;
import java.lang.ref.SoftReference;

/**
 *
 */
class JavaClosestRayResultCallback implements IRayResult {

    private SoftReference<CollisionWorld.ClosestRayResultCallback> rayCallback;

    /**
     *
     */
    JavaClosestRayResultCallback(CollisionWorld.ClosestRayResultCallback rayCallback) {
        this.rayCallback = new SoftReference<CollisionWorld.ClosestRayResultCallback>(rayCallback);
    }

    @Override
    public Object getRayResultCallback() {
        return rayCallback.get();
    }

    @Override
    public boolean hasHit() {
        return rayCallback.get().hasHit();
    }

    @Override
    public Object getCollisionObject() {
        return rayCallback.get().collisionObject;
    }

    @Override
    public Vector3f getHitPointWorld() {
        return rayCallback.get().hitPointWorld;
    }

    @Override
    public Vector3f getHitPointNormal() {
        return rayCallback.get().hitNormalWorld;
    }

}
