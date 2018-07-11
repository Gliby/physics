package gliby.minecraft.physics.common.entity.mechanics;

import java.util.ArrayList;
import java.util.List;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;

/**
 *
 */
public abstract class RigidBodyMechanic {

	private boolean enabled = true;
	private boolean common;

	/**
	 * @param enabled
	 */
	public RigidBodyMechanic setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public abstract void update(IRigidBody rigidBody, PhysicsWorld physicsWorld, Entity entity, Side side);

	public List<BlockStateAndLocation> getBlockLocationWithingAxisAlignedBB(IBlockAccess world, AxisAlignedBB BB) {
		List<BlockStateAndLocation> bb = new ArrayList<BlockStateAndLocation>();
		int i = MathHelper.floor_double(BB.minX);
		int j = MathHelper.floor_double(BB.maxX);
		int k = MathHelper.floor_double(BB.minY);
		int l = MathHelper.floor_double(BB.maxY);
		int i1 = MathHelper.floor_double(BB.minZ);
		int j1 = MathHelper.floor_double(BB.maxZ);

		for (int k1 = i; k1 <= j; ++k1) {
			for (int l1 = k; l1 <= l; ++l1) {
				for (int i2 = i1; i2 <= j1; ++i2) {
					BlockPos pos = new BlockPos(k1, l1, i2);
					IBlockState state = world.getBlockState(pos);
					if (!state.getBlock().isAir(world, pos))
						bb.add(new BlockStateAndLocation(state, pos));
				}
			}
		}
		return bb;
	}

	protected class BlockStateAndLocation {
		private IBlockState blockState;
		private BlockPos blockPosition;

		/**
		 * @param blockState
		 * @param blockPosition
		 */
		BlockStateAndLocation(IBlockState blockState, BlockPos blockPosition) {
			this.blockState = blockState;
			this.blockPosition = blockPosition;
		}

		/**
		 * @return
		 */
		public IBlockState getBlockState() {
			return blockState;
		}

		/**
		 * @return
		 */
		public BlockPos getBlockPosition() {
			return blockPosition;
		}

	}

	public void dispose() {
		enabled = false;
	}

	public void onCreatePhysics(IRigidBody body) {
	}

	public RigidBodyMechanic setCommon(boolean common) {
		this.common = common;
		return this;
	}

	public boolean isCommon() {
		return common;
	}
}
