package net.gliby.physics.common.physics;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

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
import net.gliby.physics.common.physics.engine.javabullet.JavaPhysicsWorld;
import net.gliby.physics.common.physics.engine.nativebullet.NativePhysicsWorld;
import net.gliby.physics.common.physics.mechanics.gravitymagnets.GravityModifierMechanic;
import net.gliby.physics.common.physics.mechanics.physicsgun.PickUpMechanic;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PhysicsOverworld {

	/**
	 * @return
	 */
	public PhysicsWorld getPhysicsByWorld(final World access) {
		PhysicsWorld physicsWorld = getPhysicsWorldMap().get(access);
		if (physicsWorld == null) {
			final Vector3f gravity = new Vector3f(0, -9.8F, 0);
			physicsWorld = createPhysicsWorld(
					physics.getSettings().getBooleanSetting("PhysicsEngine.UseJavaPhysics").getBooleanValue() || true,
					new IPhysicsWorldConfiguration() {

						@Override
						public boolean shouldSimulate(World world, PhysicsWorld physicsWorld) {
							return !world.playerEntities.isEmpty();
						}

						@Override
						public World getWorld() {
							return access;
						}

						@Override
						public int getTicksPerSecond() {
							// TODO Settings!
							return 30;
						}

						@Override
						public Vector3f getRegularGravity() {
							// TODO Settings!
							return gravity;
						}
					});
			physicsWorld.getMechanics().put("PickUp", new PickUpMechanic(physicsWorld, true, 20));
			physicsWorld.getMechanics().put("GravityMagnet", new GravityModifierMechanic(physicsWorld, false, 20));
			// worldStepSimulator.getMechanics().put("EntityCollision",
			// new EntityCollisionResponseMechanic(world, worldStepSimulator,
			// true,
			// 20));
			physicsWorld.create();
			Thread thread = new Thread(physicsWorld, physicsWorld.toString());
			thread.start();
			getPhysicsWorldMap().put(access, physicsWorld);
			physics.getLogger().info("Started running new physics world on thread: " + thread.toString());
		}

		return physicsWorld;
	}

	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event) {
		PhysicsWorld physicsWorld;
		if ((physicsWorld = getPhysicsWorldMap().get(event.world)) != null) {
			physicsWorld.destroy();
			getPhysicsWorldMap().remove(event.world);
			Physics.getLogger().info("Destroyed " + event.world.getWorldInfo().getWorldName() + " physics world.");
		}
	}

	private static final AxisAlignedBB BLOCK_BREAK_BB = new AxisAlignedBB(-1.75, -1.75, -1.75, 1.75, 1.75, 1.75);

	@SubscribeEvent
	public void onBlockEvent(BlockEvent event) {
		PhysicsWorld physicsWorld;
		if ((physicsWorld = getPhysicsWorldMap().get(event.world)) != null) {
			AxisAlignedBB bb = BLOCK_BREAK_BB.offset(event.pos.getX(), event.pos.getY(), event.pos.getZ());
			physicsWorld.awakenArea(new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.minZ),
					new Vector3f((float) bb.maxX, (float) bb.maxY, (float) bb.maxZ));
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
			Physics.getLogger().error("Mechanic: " + string + " doesn't exists, or is misspelled.");
		return mechanic;
	}

	protected Map<World, PhysicsWorld> physicsWorlds = new HashMap<World, PhysicsWorld>();

	public Map<World, PhysicsWorld> getPhysicsWorldMap() {
		return physicsWorlds;
	}

	private Physics physics;

	public PhysicsOverworld(Physics physics) {
		MinecraftForge.EVENT_BUS.register(this);
		this.physics = physics;
		getMechanicsMap().put("EnvironmentGravity", new EnvironmentGravityMechanic());
		getMechanicsMap().put("EnvironmentResponse", new EnvironmentResponseMechanic());
		getMechanicsMap().put("Bounce", new BounceMechanic());
		getMechanicsMap().put("ActivateRedstone", new ActivateRedstoneMechanic());
		getMechanicsMap().put("BlockInheritance", new BlockInheritanceMechanic());
		getMechanicsMap().put("ClientBlockInheritance", new ClientBlockInheritanceMechanic().setCommon(true));
	}

	public interface IPhysicsWorldConfiguration {

		public boolean shouldSimulate(World world, PhysicsWorld physicsWorld);

		public int getTicksPerSecond();

		public World getWorld();

		public Vector3f getRegularGravity();
	}

	private PhysicsWorld createPhysicsWorld(boolean useNative, IPhysicsWorldConfiguration physicsConfig) {
		PhysicsWorld physicsWorld = useNative ? new NativePhysicsWorld(physics, this, physicsConfig)
				: new JavaPhysicsWorld(physics, this, physicsConfig);
		return physicsWorld;
	}
}
