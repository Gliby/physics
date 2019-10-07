package gliby.minecraft.physics.common.entity.mechanics;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Used for Redstone related Blocks, like Redstone Block or Redstone Torch.
 */
// TODO (0.6.0) reimplement with isBlockIndirectlyGettingPowered
public class ActivateRedstoneMechanic extends RigidBodyMechanic {

    @Override
    public void update(IRigidBody rigidBody, PhysicsWorld physicsWorld, Entity entity, Side side) {
        /*
         * World world = rigidBody.getOwner().worldObj; AxisAlignedBB bb =
         * this.rigidBody.getOwner().getEntityBoundingBox();
         * List<BlockStateAndLocation> blocks =
         * this.getBlockLocationWithingAxisAlignedBB(world, bb);
         *
         * for (int i = 0; i < blocks.size(); i++) { BlockStateAndLocation block
         * = blocks.get(i); if (block.getBlockState().getBlock() instanceof
         * BlockRedstoneWire) { if ((Integer)
         * block.getBlockState().getValue(BlockRedstoneWire.POWER) != 15) {
         * world.setBlockState(block.blockPosition,
         * block.getBlockState().withProperty(BlockRedstoneWire.POWER, 15));
         * activeBlocks.add(block.blockPosition); } } } for (int i = 0; i <
         * activeBlocks.size(); i++) { BlockPos location = activeBlocks.get(i);
         * if (!containsPos(blocks, location)) { IBlockState state =
         * world.getBlockState(location); BlockRedstoneWire wire =
         * ((BlockRedstoneWire) state.getBlock());
         * wire.onNeighborBlockChange(world, location, state, wire);
         * activeBlocks.remove(i); } }
         *
         * this.rigidBody.activate(); this.rigidBody.applyCentralImpulse(new
         * Vector3f(0, 9.8f, 0));
         */
    }

    /*
     * @Override public void dispose() { World world =
     * rigidBody.getOwner().worldObj; AxisAlignedBB bb =
     * this.rigidBody.getOwner().getEntityBoundingBox();
     * List<BlockStateAndLocation> blocks =
     * this.getBlockLocationWithingAxisAlignedBB(world, bb); for (int i = 0; i <
     * blocks.size(); i++) { BlockStateAndLocation block = blocks.get(i); if
     * (block.getBlockState().getBlock() instanceof BlockRedstoneWire) { } }
     */
}
