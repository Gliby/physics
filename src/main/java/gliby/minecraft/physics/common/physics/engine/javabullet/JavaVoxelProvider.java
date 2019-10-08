package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.shapes.voxel.VoxelInfo;
import com.bulletphysicsx.collision.shapes.voxel.VoxelPhysicsWorld;
import com.bulletphysicsx.linearmath.VectorUtil;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.blocks.PhysicsBlockMetadata;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
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
        final IBlockState state = world.getBlockState(blockPosition);
        // first we check if the block is loaded.
//        if (world.isBlockLoaded(blockPos)) {
        // final PhysicsBlockMetadata metadata =
        // physicsOverworld.getPhysicsBlockMetadata().get(state.getBlock().getUnlocalizedName());
        final ICollisionShape collisionShape = physicsWorld.getBlockCache()
                .getShape(world, blockPosition, state);
        blockPosition.release();

        Physics physics = Physics.getInstance();
        final PhysicsBlockMetadata metadata = physics.getBlockManager().getPhysicsBlockMetadata().get(physics.getBlockManager().getBlockIdentity(state.getBlock()));

        float friction = (1 - state.getBlock().slipperiness) * 5;

        if (metadata != null) {
            friction = metadata.friction;
        }


        float finalFriction = friction;

        VoxelInfo info = new VoxelInfo() {

            @Override
            public boolean isColliding() {
                return state.getBlock().getMaterial(state).isLiquid();
            }

            @Override
            public float getRestitution() {
                // if (metadata != null) return metadata.restitution;
                return 0;
            }

            public float getFriction() {
                return finalFriction;
            }

            @Override
            public boolean isBlocking() {
                return state.getBlock().getMaterial(state).isSolid();
            }

            @Override
            public Object getUserData() {
                return new Vector3f(x, y, z);
            }

            @Override
            public Object getCollisionShape() {
                return collisionShape.getCollisionShape();
            }

            @Override
            public Object getCollisionOffset() {
                return VectorUtil.IDENTITY;
            }

        };
        return info;
    }

}
