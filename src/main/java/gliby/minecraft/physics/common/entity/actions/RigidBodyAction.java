package gliby.minecraft.physics.common.entity.actions;

import com.google.gson.annotations.Expose;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Per RigidBody mechanic, acts like an extension.
 */
public abstract class RigidBodyAction {

    @Expose
    private boolean enabled = true;

    @Expose
    private boolean common;

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled
     */
    public RigidBodyAction setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public abstract void update(IRigidBody rigidBody, @Nullable  PhysicsWorld physicsWorld, Entity entity, Side side);

    public List<BlockStateAndLocation> getBlockLocationWithingAxisAlignedBB(IBlockAccess world, AxisAlignedBB BB) {
        List<BlockStateAndLocation> bb = new ArrayList<BlockStateAndLocation>();
        int i = MathHelper.floor(BB.minX);
        int j = MathHelper.floor(BB.maxX);
        int k = MathHelper.floor(BB.minY);
        int l = MathHelper.floor(BB.maxY);
        int i1 = MathHelper.floor(BB.minZ);
        int j1 = MathHelper.floor(BB.maxZ);

        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
		try {
			for (int k1 = i; k1 <= j; ++k1) {
				for (int l1 = k; l1 <= l; ++l1) {
					for (int i2 = i1; i2 <= j1; ++i2) {
						pos.setPos(k1, l1, i2);
						IBlockState state = world.getBlockState(pos);
						if (!state.getBlock().isAir(state, world, pos))
							bb.add(new BlockStateAndLocation(state, new BlockPos(pos)));
					}
				}
			}
		} finally {
			pos.release();
		}
        return bb;
    }

    public void dispose() {
        enabled = false;
    }

    public void onCreatePhysics(IRigidBody body) {
    }

    public boolean isCommon() {
        return common;
    }

    public RigidBodyAction setCommon(boolean common) {
        this.common = common;
        return this;
    }

    protected class BlockStateAndLocation {
        private final IBlockState blockState;
        private final BlockPos blockPosition;

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


}
