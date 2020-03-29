package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btVoxelContentProvider;
import com.badlogic.gdx.physics.bullet.collision.btVoxelInfo;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.blocks.BlockManager;
import gliby.minecraft.physics.common.blocks.PhysicsBlockMetadata;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

class NativeVoxelProvider extends btVoxelContentProvider {


    private World world;
    private PhysicsWorld physicsWorld;

    NativeVoxelProvider(World world, PhysicsWorld physicsWorld) {
        this.world = world;
        this.physicsWorld = physicsWorld;
    }

    @Override
    public void getVoxel(int x, int y, int z, btVoxelInfo info) {
        BlockPos.PooledMutableBlockPos blockPosition = BlockPos.PooledMutableBlockPos.retain(x, y, z);
        if(world.isAreaLoaded(blockPosition, 1)) {

            final IBlockState state = world.getBlockState(blockPosition);
            // Get Block Metadata

            float friction = 1 - state.getBlock().slipperiness;
            Physics physics = Physics.getInstance();
            BlockManager blockManager = physics.getBlockManager();
            final PhysicsBlockMetadata metadata = blockManager.getPhysicsBlockMetadata().get(blockManager.getBlockIdentity(state.getBlock()));

            if (metadata != null) {
                friction = metadata.friction;
            }

            info.setBlocking(state.getMaterial().isSolid());
            if (info.getBlocking()) {
//            info.setFriction(MathHelper.clamp(friction, 0.01f, 100));
                // when friction is 0, all rigidbodies pass through the terrain.
                info.setFriction(MathHelper.clamp(friction, 0.3f, 1.0f));
                info.setCollisionShape((btCollisionShape) physicsWorld.getBlockCache()
                        .getShape(world, blockPosition, state).getCollisionShape());
            }

            blockPosition.release();
        }
    }

    //    @Override
//    public btVoxelInfo getVoxel(int x, int y, int z) {
//        BlockPos.PooledMutableBlockPos blockPosition = BlockPos.PooledMutableBlockPos.retain(x, y, z);
//        final IBlockState state = world.getBlockState(blockPosition);
//        // Get Block Metadata
//
//        float friction = 1 - state.getBlock().slipperiness;
//        Physics physics = Physics.getInstance();
//        BlockManager blockManager = physics.getBlockManager();
//        final PhysicsBlockMetadata metadata = blockManager.getPhysicsBlockMetadata().get(blockManager.getBlockIdentity(state.getBlock()));
//
//        if (metadata != null) {
//            friction = metadata.friction;
//        }
//
//        info.setBlocking(state.getMaterial().isSolid());
//        if (info.getBlocking()) {
////            info.setFriction(MathHelper.clamp(friction, 0.01f, 100));
//            // when friction is 0, all rigidbodies pass through the terrain.
//            info.setFriction(MathHelper.clamp(friction, 0.3f, 1.0f));
//            info.setCollisionShape((btCollisionShape) physicsWorld.getBlockCache()
//                    .getShape(world, blockPosition, state).getCollisionShape());
//        }
//
//        blockPosition.release();
//        return info;
//
//    }


//    @Override
//    public void dispose() {
//        super.dispose();
//    }
}
