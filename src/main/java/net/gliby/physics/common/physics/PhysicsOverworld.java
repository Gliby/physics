package net.gliby.physics.common.physics;

import java.util.HashMap;
import java.util.Map;

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
import net.minecraft.world.World;

public class PhysicsOverworld {

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
		getMechanicsMap().put("EnvironmentGravity", new EnvironmentGravityMechanic());
		getMechanicsMap().put("EnvironmentResponse", new EnvironmentResponseMechanic());
		getMechanicsMap().put("Bounce", new BounceMechanic());
		getMechanicsMap().put("ActivateRedstone", new ActivateRedstoneMechanic());
		getMechanicsMap().put("BlockInheritance", new BlockInheritanceMechanic());
		getMechanicsMap().put("ClientBlockInheritance", new ClientBlockInheritanceMechanic().setCommon(true));
	}
}
