/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.vecmath.Vector3f;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.gliby.gman.OSUtil;
import net.gliby.gman.io.MinecraftResourceLoader;
import net.gliby.physics.MetadataLoader;
import net.gliby.physics.Physics;
import net.gliby.physics.common.entity.EntityPhysicsBlock;
import net.gliby.physics.common.physics.block.PhysicsBlockMetadata;
import net.gliby.physics.common.physics.jbullet.JavaPhysicsWorld;
import net.gliby.physics.common.physics.nativebullet.NativePhysicsWorld;
import net.gliby.physics.common.physics.worldmechanics.EntityCollisionResponseMechanic;
import net.gliby.physics.common.physics.worldmechanics.gravitymagnets.GravityModifierMechanic;
import net.gliby.physics.common.physics.worldmechanics.physicsgun.PickUpMechanic;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;

/**
 * 
 */
public class ServerPhysicsOverworld extends PhysicsOverworld {

	/**
	 * @return the physicsBlockMetadata
	 */
	public Map<String, PhysicsBlockMetadata> getPhysicsBlockMetadata() {
		return physicsBlockMetadata;
	}

	public String getBlockIdentity(Block block) {
		UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(block);
		return id.modId + "." + id.name;
	}

	private ConcurrentHashMap<String, PhysicsBlockMetadata> physicsBlockMetadata;

	public ServerPhysicsOverworld() {
		// TODO When metadata has finished loading, copy concurrenthashmap from
		// MetadataLoader to physicsBlockMetadata hashmap.
		MetadataLoader loader = new MetadataLoader(
				physicsBlockMetadata = new ConcurrentHashMap<String, PhysicsBlockMetadata>()) {
			@Override
			public Map<String, Object> loadMetadataJSON(String name) throws JsonSyntaxException, IOException {
				String text = IOUtils.toString(
						MinecraftResourceLoader.getResource(Physics.getLogger(), FMLCommonHandler.instance().getSide(),
								new ResourceLocation(Physics.MOD_ID, "blocks/" + name + ".json")));
				if (text != null) {
					Map<String, Object> json = new Gson().fromJson(text, Map.class);
					return json;
				}
				return null;
			}
		};

	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerJoinWorld(EntityJoinWorldEvent event) {
		if (event.entity instanceof EntityPlayer) {
			if (!event.world.isRemote) {
				EntityPlayer player = (EntityPlayer) event.entity;
				World world = event.world;
				// TODO Settings
				if (!getPhysicsWorldMap().containsKey(world)) {
					PhysicsWorld physicsWorld = createPhysics(world);
					Thread thread = new Thread(physicsWorld,
							world.getWorldInfo().getWorldName() + " Physics Simulator, " + physicsWorld);
					thread.start();
					Physics.getLogger().info("Running " + thread.getName() + ".");
					getPhysicsWorldMap().put(world, physicsWorld);
				}
			}
		}
	}

	protected PhysicsWorld createPhysics(World world) {
		// TODO Settings.
		int tps = 30;
		Vector3f gravity = new Vector3f(0, -9.8F, 0);
		boolean forceJava = Physics.getInstance().getSettings().getBooleanSetting("PhysicsEngine.UseJavaPhysics")
				.getBooleanValue();
		PhysicsWorld worldStepSimulator = OSUtil.getOSType() == OSUtil.EnumOS.WINDOWS && !forceJava
				? new NativePhysicsWorld(world, tps, gravity) {

					@Override
					public boolean shouldSimulate() {
						return true;
					}
				} : new JavaPhysicsWorld(world, tps, gravity) {

					@Override
					public boolean shouldSimulate() {
						return true;
					}
				};

		worldStepSimulator.getMechanics().put("PickUp", new PickUpMechanic(worldStepSimulator, true, 20));
		worldStepSimulator.getMechanics().put("GravityMagnet",
				new GravityModifierMechanic(worldStepSimulator, false, 20));
		// worldStepSimulator.getMechanics().put("EntityCollision",
		// new EntityCollisionResponseMechanic(world, worldStepSimulator, true,
		// 20));
		worldStepSimulator.create();
		return worldStepSimulator;
	}

	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event) {
		PhysicsWorld stepSimulator;
		if ((stepSimulator = getPhysicsWorldMap().get(event.world)) != null) {
			stepSimulator.dispose();
			getPhysicsWorldMap().remove(event.world);
			Physics.getLogger().info(
					"Stopped and disposed of " + event.world.getWorldInfo().getWorldName() + " physics simulator.");
		}
	}

	@SubscribeEvent
	public void onBlockEvent(BlockEvent event) {
		PhysicsWorld stepSimulator;
		if ((stepSimulator = getPhysicsWorldMap().get(event.world)) != null) {
			// 1.75f is completely arbitrary. It works quite well for the time
			// being.
			float size = 1.75f;
			final AxisAlignedBB bb = new AxisAlignedBB(-size, -size, -size, size, size, size).offset(event.pos.getX(),
					event.pos.getY(), event.pos.getZ());
			stepSimulator.awakenArea(new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.minZ),
					new Vector3f((float) bb.maxX, (float) bb.maxY, (float) bb.maxZ));
		}
	}

}
