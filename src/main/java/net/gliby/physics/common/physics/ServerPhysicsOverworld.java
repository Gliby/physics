/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.vecmath.Vector3f;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.gliby.gman.io.MinecraftResourceLoader;
import net.gliby.physics.MetadataLoader;
import net.gliby.physics.Physics;
import net.gliby.physics.common.blocks.PhysicsBlockMetadata;
import net.gliby.physics.common.physics.engine.javabullet.JavaPhysicsWorld;
import net.gliby.physics.common.physics.engine.nativebullet.NativePhysicsWorld;
import net.gliby.physics.common.physics.mechanics.gravitymagnets.GravityModifierMechanic;
import net.gliby.physics.common.physics.mechanics.physicsgun.PickUpMechanic;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
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

	private Physics physics;

	public ServerPhysicsOverworld(Physics physics) {
		this.physics = physics;
		// TODO When metadata has finished loading, copy concurrenthashmap from
		// MetadataLoader to physicsBlockMetadata hashmap.
		File tempFile = null;
		ZipFile tempZip = null;
		if ((tempFile = new File(physics.getSettings().getDirectory(), "/custom/blocks.zip")).exists()) {
			try {
				tempZip = new ZipFile(tempFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		final ZipFile otherZip = tempZip;
		final File otherFile = tempFile;
		MetadataLoader loader = new MetadataLoader(
				physicsBlockMetadata = new ConcurrentHashMap<String, PhysicsBlockMetadata>()) {
			@Override
			public Map<String, Object> loadMetadata(String name) throws JsonSyntaxException, IOException {
				if (otherFile.exists()) {
					ZipEntry entry = otherZip.getEntry(name + ".json");
					if (entry != null) {
						InputStream stream = otherZip.getInputStream(entry);
						if (stream != null) {
							return new Gson().fromJson(IOUtils.toString(stream), Map.class);
						}
					}
				}

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

	@Override
	public PhysicsWorld getPhysicsByWorld(World access) {
		PhysicsWorld physicsWorld = getPhysicsWorldMap().get(access);
		if (physicsWorld == null) {
			physicsWorld = createPhysics(access);
			Thread thread = new Thread(physicsWorld,
					access.getWorldInfo().getWorldName() + " Physics Simulator, " + physicsWorld);
			thread.start();
			Physics.getLogger().info("Running " + physicsWorld + ".");
			getPhysicsWorldMap().put(access, physicsWorld);
		}
		return physicsWorld;
	}

	protected PhysicsWorld createPhysics(World world) {
		// TODO Settings tps.
		int tps = 30;
		Vector3f gravity = new Vector3f(0, -9.8F, 0);
		boolean forceJava = physics.getSettings().getBooleanSetting("PhysicsEngine.UseJavaPhysics").getBooleanValue();
		PhysicsWorld worldStepSimulator = !forceJava ? new NativePhysicsWorld(physics, this, world, tps, gravity) {

			@Override
			public boolean shouldSimulate(World world, PhysicsWorld physicsWorld) {
				return !world.playerEntities.isEmpty();
			}
		} : new JavaPhysicsWorld(physics, this, world, tps, gravity) {

			@Override
			public boolean shouldSimulate(World world, PhysicsWorld physicsWorld) {
				return !world.playerEntities.isEmpty();
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
			stepSimulator.destroy();
			getPhysicsWorldMap().remove(event.world);
			Physics.getLogger().info(
					"Stopped and disposed of " + event.world.getWorldInfo().getWorldName() + " physics simulator.");
		}
	}

	final AxisAlignedBB blockEventBB = new AxisAlignedBB(-1.75, -1.75, -1.75, 1.75, 1.75, 1.75);

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
	}

	@SubscribeEvent
	public void onBlockEvent(BlockEvent event) {
		PhysicsWorld stepSimulator;
		if ((stepSimulator = getPhysicsWorldMap().get(event.world)) != null) {
			AxisAlignedBB bb = blockEventBB.offset(event.pos.getX(), event.pos.getY(), event.pos.getZ());
			stepSimulator.awakenArea(new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.minZ),
					new Vector3f((float) bb.maxX, (float) bb.maxY, (float) bb.maxZ));
		}
	}

}
