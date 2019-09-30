package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.shapes.voxel.VoxelInfo;
import com.bulletphysicsx.collision.shapes.voxel.VoxelPhysicsWorld;
import com.bulletphysicsx.linearmath.VectorUtil;
import gliby.minecraft.physics.Physics;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;


public class JavaVoxelProvider implements VoxelPhysicsWorld {

    BlockPos blockPos;
    IBlockState blockState;
    private World world;
    private JavaPhysicsWorld physicsWorld;
    private Physics physics;

    public JavaVoxelProvider(World world, Physics physics, JavaPhysicsWorld physicsWorld) {
        this.world = world;
        this.physics = physics;
        this.physicsWorld = physicsWorld;
    }

    @Override
    public VoxelInfo getCollisionShapeAt(final int x, final int y, final int z) {
        blockPos = new BlockPos(x, y, z);
        final IBlockState blockState = world.getBlockState(blockPos);
        // final PhysicsBlockMetadata metadata =
        // physicsOverworld.getPhysicsBlockMetadata().get(state.getBlock().getUnlocalizedName());
        return new VoxelInfo() {

            @Override
            public boolean isColliding() {
                return blockState.getBlock().getMaterial().isLiquid();
            }

            @Override
            public float getRestitution() {
                // if (metadata != null) return metadata.restitution;
                return 0;
            }

            public float getFriction() {
                float friction = (1 - blockState.getBlock().slipperiness) * 5;
                return friction;
            }

            @Override
            public boolean isBlocking() {
                return blockState.getBlock().getMaterial().isSolid();
            }

            @Override
            public Object getUserData() {
                return new Vector3f(x, y, z);
            }

            @Override
            public Object getCollisionShape() {
                return physics.getBlockManager().getBlockCache()
                        .getShape(physicsWorld, world, blockPos, blockState).getCollisionShape();
            }

            @Override
            public Object getCollisionOffset() {
                return VectorUtil.IDENTITY;
            }

        };
    }

}
