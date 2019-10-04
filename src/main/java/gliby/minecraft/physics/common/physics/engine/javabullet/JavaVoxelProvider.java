package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.shapes.voxel.VoxelInfo;
import com.bulletphysicsx.collision.shapes.voxel.VoxelPhysicsWorld;
import com.bulletphysicsx.linearmath.VectorUtil;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.vecmath.Vector3f;
import java.lang.ref.SoftReference;

public class JavaVoxelProvider implements VoxelPhysicsWorld {

    private SoftReference<World> worldRef;
    private SoftReference<JavaPhysicsWorld> physicsWorld;

    public JavaVoxelProvider(World world, JavaPhysicsWorld physicsWorld) {
        this.worldRef = new SoftReference<World>(world);
        this.physicsWorld = new SoftReference<JavaPhysicsWorld>(physicsWorld);
    }

    @Override
    public VoxelInfo getCollisionShapeAt(final int x, final int y, final int z) {
        World world = worldRef.get();
        final BlockPos blockPos = new BlockPos(x, y, z);
        final IBlockState blockState = world.getBlockState(blockPos);
        // final PhysicsBlockMetadata metadata =
        // physicsOverworld.getPhysicsBlockMetadata().get(state.getBlock().getUnlocalizedName());
        return new VoxelInfo() {

            @Override
            public boolean isColliding() {
                return blockState.getMaterial().isLiquid();
            }

            @Override
            public float getRestitution() {
                // if (metadata != null) return metadata.restitution;
                return 0;
            }

            public float getFriction() {
                float friction = (1 - blockState.getBlock().slipperiness);
                return friction;
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
                return physicsWorld.get().getBlockCache()
                        .getShape(world, blockPos, blockState).getCollisionShape();
            }

            @Override
            public Object getCollisionOffset() {
                return VectorUtil.IDENTITY;
            }

        };
    }

}
