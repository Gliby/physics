package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.shapes.voxel.VoxelInfo;
import com.bulletphysicsx.collision.shapes.voxel.VoxelPhysicsWorld;
import com.bulletphysicsx.linearmath.VectorUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;


public class JavaVoxelProvider implements VoxelPhysicsWorld {

    private World world;
    private JavaPhysicsWorld physicsWorld;

    public JavaVoxelProvider(World world, JavaPhysicsWorld physicsWorld) {
        this.world = world;
        this.physicsWorld = physicsWorld;
    }

    // TODO (0.6.0) use block metadata if key exists.
    @Override
    public VoxelInfo getCollisionShapeAt(final int x, final int y, final int z) {
        final BlockPos.PooledMutableBlockPos blockPosition = BlockPos.PooledMutableBlockPos.retain(x, y, z);
        final IBlockState blockState = world.getBlockState(blockPosition);
        // first we check if the block is loaded.
//        if (world.isBlockLoaded(blockPos)) {
        // final PhysicsBlockMetadata metadata =
        // physicsOverworld.getPhysicsBlockMetadata().get(state.getBlock().getUnlocalizedName());
        VoxelInfo info = new VoxelInfo() {

            @Override
            public boolean isColliding() {
                return blockState.getBlock().getMaterial(blockState).isLiquid();
            }

            @Override
            public float getRestitution() {
                // if (metadata != null) return metadata.restitution;
                return 0;
            }

            public float getFriction() {
                return (1 - blockState.getBlock().slipperiness);
            }

            @Override
            public boolean isBlocking() {
                return blockState.getBlock().getMaterial(blockState).isSolid();
            }

            @Override
            public Object getUserData() {
                return new Vector3f(x, y, z);
            }

            @Override
            public Object getCollisionShape() {
                return physicsWorld.getBlockCache()
                        .getShape(world, blockPosition, blockState).getCollisionShape();
            }

            @Override
            public Object getCollisionOffset() {
                return VectorUtil.IDENTITY;
            }

        };
        blockPosition.release();
        return info;
    }

}
