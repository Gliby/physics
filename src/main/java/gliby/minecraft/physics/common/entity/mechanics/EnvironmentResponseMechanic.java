package gliby.minecraft.physics.common.entity.mechanics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Vector3f;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

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
			List<BlockStateAndLocation> blocks = getLiquidsInBB(rigidBody.getOwner().worldObj, blockBB);
			for (int i = 0; i < blocks.size(); i++) {
				BlockStateAndLocation block = blocks.get(i);
				Material liquidMaterial = block.getBlockState().getBlock().getMaterial();
				BlockDynamicLiquid liquidBlock = BlockLiquid.getFlowingBlock(liquidMaterial);
				Vec3 vec3 = getFlowVector(rigidBody.getOwner().worldObj, block.getBlockPosition(),
						block.getBlockState().getBlock(), liquidMaterial);
				Vector3f impulse = new Vector3f((float) vec3.xCoord, (float) vec3.yCoord, (float) vec3.zCoord);
				// TODO improvement: add scalar 
				impulse.scale(1.25f);
				rigidBody.applyCentralImpulse(impulse);
				rigidBody.activate();
			}
		}
	}

	/**
	 * Used to getLevel of block.
	 * 
	 * @param worldIn
	 * @param pos
	 * @param blockMaterial
	 * @return
	 */
	protected int getLevel(IBlockAccess worldIn, BlockPos pos, Material blockMaterial) {
		return worldIn.getBlockState(pos).getBlock().getMaterial() == blockMaterial
				? ((Integer) worldIn.getBlockState(pos).getValue(LEVEL)).intValue() : -1;
	}

	protected int getEffectiveFlowDecay(IBlockAccess worldIn, BlockPos pos, Material blockMaterial) {
		int i = getLevel(worldIn, pos, blockMaterial);
		return i >= 8 ? 0 : i;
	}

	private static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 15);

	/**
	 * Returns block flow direction vector.
	 * 
	 * @param worldIn
	 * @param pos
	 * @param block
	 * @param blockMaterial
	 * @return
	 */
	protected Vec3 getFlowVector(IBlockAccess worldIn, BlockPos pos, Block block, Material blockMaterial) {
		Vec3 vec3 = new Vec3(0.0D, 0.0D, 0.0D);
		int i = getEffectiveFlowDecay(worldIn, pos, blockMaterial);
		Iterator iterator = EnumFacing.Plane.HORIZONTAL.iterator();
		EnumFacing enumfacing;
		BlockPos blockpos1;

		while (iterator.hasNext()) {
			enumfacing = (EnumFacing) iterator.next();
			blockpos1 = pos.offset(enumfacing);
			int j = getEffectiveFlowDecay(worldIn, blockpos1, blockMaterial);
			int k;

			if (j < 0) {
				if (!worldIn.getBlockState(blockpos1).getBlock().getMaterial().blocksMovement()) {
					j = getEffectiveFlowDecay(worldIn, blockpos1.down(), blockMaterial);

					if (j >= 0) {
						k = j - (i - 8);
						vec3 = vec3.addVector((double) ((blockpos1.getX() - pos.getX()) * k),
								(double) ((blockpos1.getY() - pos.getY()) * k),
								(double) ((blockpos1.getZ() - pos.getZ()) * k));
					}
				}
			} else if (j >= 0) {
				k = j - i;
				vec3 = vec3.addVector((double) ((blockpos1.getX() - pos.getX()) * k),
						(double) ((blockpos1.getY() - pos.getY()) * k), (double) ((blockpos1.getZ() - pos.getZ()) * k));
			}
		}

		if (((Integer) worldIn.getBlockState(pos).getValue(LEVEL)).intValue() >= 8) {
			iterator = EnumFacing.Plane.HORIZONTAL.iterator();

			while (iterator.hasNext()) {
				enumfacing = (EnumFacing) iterator.next();
				blockpos1 = pos.offset(enumfacing);

				if (block.isBlockSolid(worldIn, blockpos1, enumfacing)
						|| block.isBlockSolid(worldIn, blockpos1.up(), enumfacing)) {
					vec3 = vec3.normalize().addVector(0.0D, -6.0D, 0.0D);
					break;
				}
			}
		}
		return vec3.normalize();
	}

	public List<BlockStateAndLocation> getLiquidsInBB(World world, AxisAlignedBB bb) {
		List<BlockStateAndLocation> blockInformations = new ArrayList<BlockStateAndLocation>();
		int maxX = MathHelper.floor_double(bb.maxX);
		int maxY = MathHelper.floor_double(bb.maxY);
		int maxZ = MathHelper.floor_double(bb.maxZ);
		int minX = MathHelper.floor_double(bb.minX);
		int minY = MathHelper.floor_double(bb.minY);
		int minZ = MathHelper.floor_double(bb.minZ);

		for (int x = minX; x <= maxX; ++x) {
			for (int y = minY; y <= maxY; ++y) {
				for (int z = minZ; z <= maxZ; ++z) {
					BlockPos blockPosition = new BlockPos(x, y, z);
					IBlockState blockState = world.getBlockState(blockPosition);

					if (blockState.getBlock().getMaterial().isLiquid()) {
						blockInformations.add(new BlockStateAndLocation(blockState, blockPosition));
					}
				}
			}
		}
		return blockInformations;
	}
}
