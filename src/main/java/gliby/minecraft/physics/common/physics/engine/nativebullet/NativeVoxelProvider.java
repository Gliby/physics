package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btVoxelContentProvider;
import com.badlogic.gdx.physics.bullet.collision.btVoxelInfo;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class NativeVoxelProvider extends btVoxelContentProvider {


    private World world;
    private PhysicsWorld physicsWorld;
    private btVoxelInfo info;

    NativeVoxelProvider(btVoxelInfo info, World world, PhysicsWorld physicsWorld) {
        this.world = world;
        this.physicsWorld = physicsWorld;
        this.info = info;
    }

    // TODO (0.6.0) add is block loaded check to avoid loading distant chunks.
    @Override
    public btVoxelInfo getVoxel(int x, int y, int z) {
        final BlockPos.PooledMutableBlockPos blockPosition = BlockPos.PooledMutableBlockPos.retain(x, y, z);
        final IBlockState state = world.getBlockState(blockPosition);
        info.setBlocking(state.getBlock().getMaterial(state).isSolid());
        info.setCollisionShape((btCollisionShape) physicsWorld.getBlockCache()
                .getShape(world, blockPosition, state).getCollisionShape());
        info.setFriction((1 - state.getBlock().slipperiness) * 5);
        blockPosition.release();
        return info;

    }

}
