package net.gliby.physics.common.blocks;

import java.util.HashMap;
import java.util.Map;

import net.gliby.physics.Physics;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.ICollisionShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BlockManager {

	private Physics physics;

	public BlockManager(Physics physics) {
		this.physics = physics;
		blockShapeCache = new BlockShapeCache();
		blockGenerators = new HashMap<String, IBlockGenerator>();
		defaultGenerator = new DefaultBlockGenerator();
	}

	private Map<String, IBlockGenerator> blockGenerators;

	private IBlockGenerator defaultGenerator;

	/**
	 * If modID is equal to null, the default block generator will be set.
	 * 
	 * @param modID
	 * @param blockGenerator
	 */
	public void registerBlockGenerator(String modID, IBlockGenerator blockGenerator) {
		if (modID != null)
			blockGenerators.put(modID, blockGenerator);
		else
			defaultGenerator = blockGenerator;
	}

	public IBlockGenerator getDefaultBlockGenerator() {
		return defaultGenerator;
	}

	public Map<String, IBlockGenerator> getBlockGenerators() {
		return blockGenerators;
	}

	public BlockShapeCache getBlockCache() {
		return blockShapeCache;
	}

	private BlockShapeCache blockShapeCache;

	public class BlockShapeCache {

		private Map<IBlockState, ICollisionShape> cache;

		public BlockShapeCache() {
			cache = new HashMap<IBlockState, ICollisionShape>();
		}

		public ICollisionShape getShape(PhysicsWorld physicsWorld, World world, BlockPos pos, IBlockState state) {
			ICollisionShape shape;
			if ((shape = cache.get(state)) == null) {
				shape = physicsWorld.createBlockShape(world, pos, state);
				cache.put(state, shape);
			}
			return shape;
		}
	}

}
