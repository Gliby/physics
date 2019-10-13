package gliby.minecraft.physics.common.entity.actions;

import gliby.minecraft.physics.Physics;
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
// TODO (0.7.0) Add buoyancy https://pybullet.org/Bullet/phpBB3/viewtopic.php?t=11905 (might have to convert global mechanics into Bullet Action Interface, to apply under substep)
public class EnvironmentResponseAction extends RigidBodyAction {


    @Override
    public void update(IRigidBody rigidBody, PhysicsWorld physicsWorld, Entity entity, Side side) {
        if (entity.isInLava() || entity.isBurning()) {
            rigidBody.getOwner().setDead();
        }



        if (entity.isInWater()) {

//            // TODO (0.7.0) Buoyancy (bugged: spins like crazy)
//
//            // Get RigidBody AABB
//            Vector3f min = new Vector3f(), max = new Vector3f();
//            rigidBody.getAabb(min, max);
//
//            // Convert AABB to Minecraft's ABBB
//            AxisAlignedBB bb = new AxisAlignedBB(VecUtility.toVec3(min), VecUtility.toVec3(max));
//            // Offset by physics space
//            bb.offset(0.5f, 0.5f, 0.5f);
//            // get corners of bb
//            Vec3d[] corners = VecUtility.getCorners(bb);
//
//            // Calculate submerged corners.
//
//            float totalDepth = 0;
//            List<DepthPoint> submergedPoints = new ArrayList<DepthPoint>();
//            BlockPos.PooledMutableBlockPos cornerBlock = BlockPos.PooledMutableBlockPos.retain();
//            for (Vec3d corner : corners) {
//                cornerBlock.setPos(corner.x, corner.y, corner.z);
//                final BlockPos highestWaterBlock = entity.getEntityWorld().getTopSolidOrLiquidBlock(cornerBlock);
//                final int waterHeight = highestWaterBlock.getY();
//                if (corner.y <= waterHeight)  {
//                    float depth = (float) (waterHeight - corner.y);
//                    totalDepth += depth;
//                    submergedPoints.add(new DepthPoint(corner, depth));
//                }
//            }
//            cornerBlock.release();
//
//            // Caulculate displacement
//            float avgDepth = totalDepth / submergedPoints.size();
//            float bottomMostAreaOfBox = VecUtility.getAreaOfBoundingBoxBottomFace(bb);
//
//            float volumeOfBox = VecUtility.getVolumeOfBoundingBox(bb);
//
//
//            float displacedVolumeOfBox = FastMath.min(bottomMostAreaOfBox * avgDepth, volumeOfBox);
//            float displacedVolume = displacedVolumeOfBox * (rigidBody.getCollisionShape().getVolume() / volumeOfBox);
//
//
//            float waterDensity = 1.0f;
//            float accelerationOfGravity = physicsWorld.getPhysicsConfiguration().getRegularGravity().y;
//            float totalForce = displacedVolume * waterDensity * accelerationOfGravity;
//
//            Vec3d position = VecUtility.toVec3(rigidBody.getPosition());
//
//            for (DepthPoint depthPoint : submergedPoints) {
//                Vec3d relativePosition = depthPoint.point.subtract(position);
////                System.out.println("depth: "+ depthPoint.depth);
////                System.out.println("totalDepth: "+ totalDepth);
////                System.out.println("totalForce: "+ totalForce);
//
//                float force = (totalForce * depthPoint.depth / totalDepth);
//                Vector3f upForce = new Vector3f(0, 1, 0);
//                upForce.scale(force);
////                System.out.println("buoyancy: " + force);
//                rigidBody.applyForce(upForce, VecUtility.toVector3f(relativePosition));
//            }

            List<BlockStateAndLocation> blocks = getLiquidsInBB(entity.world, entity.getEntityBoundingBox());
            for (int i = 0; i < blocks.size(); i++) {
                BlockStateAndLocation block = blocks.get(i);
                Material liquidMaterial = block.getBlockState().getBlock().getMaterial(block.getBlockState());
                BlockDynamicLiquid liquidBlock = BlockLiquid.getFlowingBlock(liquidMaterial);
                Vec3d flow = liquidBlock.getFlow(entity.world, block.getBlockPosition(), block.getBlockState());
                Vector3f impulse = new Vector3f((float) flow.x, (float) flow.y, (float) flow.z);
                float waterForceMultiplier = Physics.getInstance().getSettings().getFloatSetting("Game.WaterForceMultiplier").getFloatValue();
                if (impulse.lengthSquared() > 0) {
                    impulse.scale(waterForceMultiplier);
                    rigidBody.applyCentralImpulse(impulse);
                    rigidBody.activate();
                }

            }
            System.out.println("tick");
        }
    }

    public class DepthPoint {
        public DepthPoint(Vec3d point, float depth) {
            this.point = point;
            this.depth = depth;
        }

        Vec3d point;
        float depth;
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
							blockImportations.add(new BlockStateAndLocation(blockState, new BlockPos(blockPosition)));
					}
				}
			}
		} finally {
			blockPosition.release();
		}
        return blockImportations;
    }
}
