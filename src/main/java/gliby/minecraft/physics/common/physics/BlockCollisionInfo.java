package gliby.minecraft.physics.common.physics;

import com.bulletphysicsx.collision.broadphase.BroadphaseNativeType;

import javax.vecmath.Point3i;
import javax.vecmath.Tuple3i;

/**
 *
 */
public class BlockCollisionInfo {
    public final Tuple3i position;
    public int blockShape = BroadphaseNativeType.INVALID_SHAPE_PROXYTYPE.ordinal();
    public Object algorithm;

    public BlockCollisionInfo(int x, int y, int z) {
        this.position = new Point3i(x, y, z);
    }
}
