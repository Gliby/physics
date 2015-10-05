package net.gliby.physics.common.physics.engine.nativebullet;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btVoxelContentProvider;
import com.badlogic.gdx.physics.bullet.collision.btVoxelInfo;

import net.gliby.physics.common.physics.PhysicsOverworld;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

class NativeVoxelProvider extends btVoxelContentProvider {

	private World world;
	private PhysicsOverworld physicsOverworld;
	private PhysicsWorld physicsWorld;

	NativeVoxelProvider(World world, PhysicsWorld physicsWorld, PhysicsOverworld physicsOverworld) {
		this.world = world;
		this.physicsWorld = physicsWorld;
		this.physicsOverworld = physicsOverworld;
	}

	/*
	 * public btVoxelInfo getVoxelAt(int x, int y, int z) { final BlockPos
	 * blockPosition = new BlockPos(x, y, z); final IBlockState state =
	 * world.getBlockState(blockPosition); return new btVoxelInfo(false,
	 * state.getBlock().getMaterial().isSolid(), 0, 0, (btCollisionShape)
	 * physicsWorld.createBlockShape(world, blockPosition,
	 * state).getCollisionShape(), new Vector3(0, 0, 0), (1 -
	 * state.getBlock().slipperiness) * 5, 0, 0); }
	 */


	private btVoxelInfo info = new btVoxelInfo(false, false, 0, 0, null, new Vector3(0, 0, 0), 0, 0, 0);

	@Override
	public btVoxelInfo getVoxel(int x, int y, int z) {
		final BlockPos blockPosition = new BlockPos(x, y, z);
		final IBlockState state = world.getBlockState(blockPosition);
		info.setTracable(false);
		info.setBlocking(state.getBlock().getMaterial().isSolid());
		info.setCollisionShape((btCollisionShape) physicsOverworld.getBlockCache().getShape(physicsWorld, world, blockPosition, state)
				.getCollisionShape());
		info.setFriction((1 - state.getBlock().slipperiness) * 5);
		return info;

	}
}
