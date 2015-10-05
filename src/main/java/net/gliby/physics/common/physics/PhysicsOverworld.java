package net.gliby.physics.common.physics;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.gliby.physics.Physics;
import net.gliby.physics.common.entity.mechanics.ActivateRedstoneMechanic;
import net.gliby.physics.common.entity.mechanics.BlockInheritanceMechanic;
import net.gliby.physics.common.entity.mechanics.BounceMechanic;
import net.gliby.physics.common.entity.mechanics.ClientBlockInheritanceMechanic;
import net.gliby.physics.common.entity.mechanics.EnvironmentGravityMechanic;
import net.gliby.physics.common.entity.mechanics.EnvironmentResponseMechanic;
import net.gliby.physics.common.entity.mechanics.RigidBodyMechanic;
import net.gliby.physics.common.physics.PhysicsOverworld.BlockShapeCache;
import net.gliby.physics.common.physics.engine.ICollisionShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PhysicsOverworld {

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

	public BlockShapeCache getBlockCache() {
		return blockShapeCache;
	}

	/**
	 * @return
	 */
	public PhysicsWorld getPhysicsByWorld(World access) {
		return physicsWorlds.get(access);
	}

	private BiMap<String, RigidBodyMechanic> mechanicsMap = HashBiMap.create();

	/**
	 * @return the mechanicsMap
	 */
	public BiMap<String, RigidBodyMechanic> getMechanicsMap() {
		return mechanicsMap;
	}

	/**
	 * @param string
	 * @return
	 */
	public RigidBodyMechanic getMechanicFromName(String string) {
		RigidBodyMechanic mechanic = mechanicsMap.get(string);
		if (mechanic == null)
			Physics.getLogger().error("Mechanic: " + string + " doesn't exists, or is misspelled.");
		return mechanic;
	}

	protected Map<World, PhysicsWorld> physicsWorlds = new HashMap<World, PhysicsWorld>();

	public Map<World, PhysicsWorld> getPhysicsWorldMap() {
		return physicsWorlds;
	}

	public PhysicsOverworld() {
		blockShapeCache = new BlockShapeCache();
		getMechanicsMap().put("EnvironmentGravity", new EnvironmentGravityMechanic());
		getMechanicsMap().put("EnvironmentResponse", new EnvironmentResponseMechanic());
		getMechanicsMap().put("Bounce", new BounceMechanic());
		getMechanicsMap().put("ActivateRedstone", new ActivateRedstoneMechanic());
		getMechanicsMap().put("BlockInheritance", new BlockInheritanceMechanic());
		getMechanicsMap().put("ClientBlockInheritance", new ClientBlockInheritanceMechanic().setCommon(true));
	}
}
