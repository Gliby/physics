package gliby.minecraft.physics.common.entity.mechanics;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * Block Mechanic responsible for water flow, lava death, etc.
 */
public class EnvironmentResponseMechanic extends RigidBodyMechanic {


    @Override
    public void update(IRigidBody rigidBody, PhysicsWorld physicsWorld, Entity entity, Side side) {
        if (entity.isInLava() || entity.isBurning()) {
            rigidBody.getOwner().setDead();
        }

        if (entity.isInWater()) {
            Vector3f centerOfMass = rigidBody.getCenterOfMassPosition();
            float size = 0.5f;
            Vector3f bbPos = new Vector3f((centerOfMass.x + (size / 2)), (centerOfMass.y + (size / 2)),
                    (centerOfMass.z + (size / 2)));
            AxisAlignedBB blockBB = new AxisAlignedBB(bbPos.x, bbPos.y, bbPos.z, bbPos.x + size, bbPos.y + size,
                    bbPos.z + size);
            List<BlockStateAndLocation> blocks = getLiquidsInBB(rigidBody.getOwner().world, blockBB);
            for (int i = 0; i < blocks.size(); i++) {
                BlockStateAndLocation block = blocks.get(i);
                Material liquidMaterial = block.getBlockState().getBlock().getMaterial(block.getBlockState());
                BlockDynamicLiquid liquidBlock = BlockLiquid.getFlowingBlock(liquidMaterial);
                // TODO (0.7.0) Fix flow direction
//                Vec3d vec3 = getFlowVector(rigidBody.getOwner().world, block.getBlockPosition(),
//                        block.getBlockState().getBlock(), liquidMaterial);
                Vec3d flow = Vec3d.ZERO;
                Vector3f impulse = new Vector3f((float) flow.x, (float) flow.y, (float) flow.z);

                impulse.scale(1.25f);
                rigidBody.applyCentralImpulse(impulse);
                rigidBody.activate();
            }
        }
    }


    public List<BlockStateAndLocation> getLiquidsInBB(World world, AxisAlignedBB bb) {
        List<BlockStateAndLocation> blockImportations = new ArrayList<BlockStateAndLocation>();
        int maxX = MathHelper.floor(bb.maxX);
        int maxY = MathHelper.floor(bb.maxY);
        int maxZ = MathHelper.floor(bb.maxZ);
        int minX = MathHelper.floor(bb.minX);
        int minY = MathHelper.floor(bb.minY);
        int minZ = MathHelper.floor(bb.minZ);

        BlockPos.PooledMutableBlockPos blockPosition = BlockPos.PooledMutableBlockPos.retain();
		try {
			for (int x = minX; x <= maxX; ++x) {
				for (int y = minY; y <= maxY; ++y) {
					for (int z = minZ; z <= maxZ; ++z) {
						blockPosition.setPos(x, y, z);
						IBlockState blockState = world.getBlockState(blockPosition);
						if (blockState.getBlock().getMaterial(blockState).isLiquid())
							blockImportations.add(new BlockStateAndLocation(blockState, blockPosition));
					}
				}
			}
		} finally {
			blockPosition.release();
		}
        return blockImportations;
    }
}
