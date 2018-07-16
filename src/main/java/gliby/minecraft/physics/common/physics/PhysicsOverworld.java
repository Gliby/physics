package gliby.minecraft.physics.common.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.entity.mechanics.BounceMechanic;
import gliby.minecraft.physics.common.entity.mechanics.EnvironmentGravityMechanic;
import gliby.minecraft.physics.common.entity.mechanics.EnvironmentResponseMechanic;
import gliby.minecraft.physics.common.entity.mechanics.RigidBodyMechanic;
import gliby.minecraft.physics.common.physics.engine.nativebullet.NativePhysicsWorld;
import gliby.minecraft.physics.common.physics.mechanics.PhysicsMechanic;
import gliby.minecraft.physics.common.physics.mechanics.ToolMechanics;
import gliby.minecraft.physics.common.physics.mechanics.gravitymagnets.GravityModifierMechanic;
import gliby.minecraft.physics.common.physics.mechanics.physicsgun.PickUpMechanic;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class PhysicsOverworld {

	List<PhysicsWorld> tickableWorlds;

	/**
	 * @return
	 */
	public PhysicsWorld getPhysicsByWorld(final World access) {
		PhysicsWorld physicsWorld = getPhysicsWorldMap().get(access);
		if (physicsWorld == null) {
			boolean multiThread = physics.getSettings().getBooleanSetting("PhysicsEngine.MultiThread")
					.getBooleanValue();

			final Vector3 gravity = new Vector3(0,
					physics.getSettings().getFloatSetting("PhysicsEngine.GravityForce").getFloatValue(), 0);
			final PhysicsWorld createdPhysicsWorld = getCorrectPhysicsWorld(
					!physics.getSettings().getBooleanSetting("PhysicsEngine.UseJavaPhysics").getBooleanValue(),
					multiThread, new IPhysicsWorldConfiguration() {

						@Override
						public boolean shouldSimulate(World world, PhysicsWorld physicsWorld) {
							return !world.playerEntities.isEmpty();
						}

						@Override
						public World getWorld() {
							return access;
						}

						@Override
						public final int getTicksPerSecond() {
							return physics.getSettings().getIntegerSetting("PhysicsEngine.TickRate").getIntValue();
						}

						@Override
						public Vector3 getRegularGravity() {
							return gravity;
						}
					});
			physicsWorld = createdPhysicsWorld;
			// TODO concurrency bug might happen here, because of threaded mechanics.
			physicsWorld.getMechanics().put("PickUp", new PickUpMechanic(physicsWorld, true, 20));
			physicsWorld.getMechanics().put("GravityMagnet", new GravityModifierMechanic(physicsWorld, false, 20));
			physicsWorld.getMechanics().put("ToolMan",
					new ToolMechanics(physics.getGameManager().getToolGunRegistry(), physicsWorld, false, 20));
			// worldStepSimulator.getMechanics().put("EntityCollision",
			// new EntityCollisionResponseMechanic(world, worldStepSimulator,
			// true,
			// 20));
			physicsWorld.init();
			if (multiThread) {
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						createdPhysicsWorld.tick();
					}
				}, physicsWorld.toString());
				thread.start();
			} else
				tickableWorlds.add(physicsWorld);
			getPhysicsWorldMap().put(access, physicsWorld);
			physics.getLogger().info("Started running new physics world: " + physicsWorld.toString());
		}

		return physicsWorld;
	}

	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event) {
		PhysicsWorld physicsWorld;
		if ((physicsWorld = getPhysicsWorldMap().get(event.world)) != null) {
			physicsWorld.dispose();
			Iterator it = physicsWorld.physicsMechanics.entrySet().iterator();
			while (it.hasNext()) {
				PhysicsMechanic mechanic = ((Map.Entry<String, PhysicsMechanic>) it.next()).getValue();
				mechanic.dispose();
				mechanic.setEnabled(false);
			}
			getPhysicsWorldMap().remove(event.world);
			Physics.getLogger().info("Destroyed " + event.world.getWorldInfo().getWorldName() + " physics world.");
		}
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == Phase.START) {
			for (int i = 0; i < tickableWorlds.size(); i++) {
				PhysicsWorld physicsWorld = tickableWorlds.get(i);
				physicsWorld.tick();
			}
		}
	}

	private static final AxisAlignedBB BLOCK_BREAK_BB = new AxisAlignedBB(-1.75, -1.75, -1.75, 1.75, 1.75, 1.75);

	@SubscribeEvent
	public void onBlockEvent(BlockEvent event) {
		PhysicsWorld physicsWorld;
		if ((physicsWorld = getPhysicsWorldMap().get(event.world)) != null) {
			AxisAlignedBB bb = BLOCK_BREAK_BB.offset(event.pos.getX(), event.pos.getY(), event.pos.getZ());
			physicsWorld.awakenArea(new Vector3((float) bb.minX, (float) bb.minY, (float) bb.minZ),
					new Vector3((float) bb.maxX, (float) bb.maxY, (float) bb.maxZ));
		}
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
			Physics.getLogger().debug("Mechanic: " + string + " doesn't exists, or is misspelled.");
		return mechanic;
	}

	protected Map<World, PhysicsWorld> physicsWorlds = new HashMap<World, PhysicsWorld>();

	public Map<World, PhysicsWorld> getPhysicsWorldMap() {
		return physicsWorlds;
	}

	private Physics physics;

	public PhysicsOverworld(Physics physics) {
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		this.physics = physics;
		this.tickableWorlds = new ArrayList<PhysicsWorld>();
		// Registers available mechanics.
		// TODO finish: mechanics
		getMechanicsMap().put("EnvironmentGravity", new EnvironmentGravityMechanic());
		getMechanicsMap().put("EnvironmentResponse", new EnvironmentResponseMechanic());
		getMechanicsMap().put("Bounce", new BounceMechanic());

		// TODO feature: get these mechanics working properly
		// getMechanicsMap().put("ActivateRedstone", new ActivateRedstoneMechanic());
		// getMechanicsMap().put("BlockInheritance", new BlockInheritanceMechanic());
		// getMechanicsMap().put("ClientBlockInheritance", new
		// ClientBlockInheritanceMechanic().setCommon(true));
	}

	public interface IPhysicsWorldConfiguration {

		public boolean shouldSimulate(World world, PhysicsWorld physicsWorld);

		public int getTicksPerSecond();

		public World getWorld();

		public Vector3 getRegularGravity();

	}

	static int world;

	private PhysicsWorld getCorrectPhysicsWorld(boolean useNative, boolean useMultithread,
			IPhysicsWorldConfiguration physicsConfig) {
		return new NativePhysicsWorld(physics, this, physicsConfig);

	}
}
