package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btVoxelContentProvider;
import com.badlogic.gdx.physics.bullet.collision.btVoxelInfo;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;

class NativeVoxelProvider extends btVoxelContentProvider {

    private Physics physics;

    private WeakReference<World> worldRef;
    private WeakReference<PhysicsWorld> physicsWorld;
    private WeakReference<btVoxelInfo> info;

    NativeVoxelProvider(btVoxelInfo info, World world, PhysicsWorld physicsWorld, Physics physics) {
        this.worldRef = new WeakReference<World>(world);
        this.physicsWorld = new WeakReference<PhysicsWorld>(physicsWorld);
        this.info = new WeakReference<btVoxelInfo>(info);
        this.physics = physics;
    }

    @Override
    public btVoxelInfo getVoxel(int x, int y, int z) {
        btVoxelInfo voxelInfo = info.get();
        World world = worldRef.get();
        if (world != null && !world.playerEntities.isEmpty()) {
            final BlockPos blockPosition = new BlockPos(x, y, z);
            final IBlockState state = world.getBlockState(blockPosition);
            voxelInfo.setTracable(false);
            voxelInfo.setBlocking(state.getBlock().getMaterial(state).isSolid());
            voxelInfo.setCollisionShape((btCollisionShape) physicsWorld.get().getBlockCache()
                    .getShape(world, blockPosition, state).getCollisionShape());
            voxelInfo.setFriction((1 - state.getBlock().slipperiness) * 5);
        }
        return voxelInfo;

    }

}
