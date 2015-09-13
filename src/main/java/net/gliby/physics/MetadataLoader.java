/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonSyntaxException;

import net.gliby.physics.common.entity.mechanics.RigidBodyMechanic;
import net.gliby.physics.common.physics.ServerPhysicsOverworld;
import net.gliby.physics.common.physics.block.PhysicsBlockMetadata;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;

/**
 *
 */
public abstract class MetadataLoader {

	private ConcurrentHashMap<String, PhysicsBlockMetadata> metadataMap;

	public MetadataLoader(ConcurrentHashMap<String, PhysicsBlockMetadata> metadataMap) {
		this.metadataMap = metadataMap;
		start();
	}

	BlockingQueue<Callable> blockLoadQueue = new LinkedBlockingQueue<Callable>();

	int loaded;

	private Runnable loadQueue() {
		return new Runnable() {

			@Override
			public void run() {
				while (!blockLoadQueue.isEmpty()) {
					Callable c = blockLoadQueue.poll();
					if (c != null) {
						try {
							c.call();
							loaded++;
						} catch (Exception e) {
							if (!(e instanceof NullPointerException))
								e.printStackTrace();
						} finally {
						}
					}
				}
				Physics.getLogger().info("Loaded " + loaded + " physics blocks.");
			}

		};
	}

	public void start() {
		Iterator<Block> itr = Block.blockRegistry.iterator();
		while (itr.hasNext()) {
			final Block block = itr.next();
			UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(block);
			final String blockID = id.modId + "." + id.name;
			if (!metadataMap.containsKey(blockID)) {
				blockLoadQueue.offer(new Callable() {
					@Override
					public Object call() {

						Map<String, Object> json = null;

						try {
							if ((json = loadMetadataJSON(blockID)) != null) {
								PhysicsBlockMetadata metadata = getMetadata(blockID, json);
								metadataMap.put(blockID, metadata);
							} else {

							}
						} catch (JsonSyntaxException | IOException e) {
							// e.printStackTrace();
						}

						/*
						 * Gson gson = new
						 * GsonBuilder().setPrettyPrinting().create();
						 *
						 * JsonObject writable = new JsonObject();
						 *
						 * float hardness = block.getBlockHardness(null, null);
						 * writable.addProperty("mass",
						 * MathHelper.clamp_float(hardness * 20, 1,
						 * Float.MAX_VALUE)); writable.addProperty("friction",
						 * (1 - block.slipperiness) * 5);
						 *
						 * if (block.getCollisionBoundingBox(null, new
						 * BlockPos(0, 0, 0), null) == null) {
						 * writable.addProperty("collisionEnabled", false); }
						 *
						 * if (block.getBlockState().getBaseState().
						 * getPropertyNames().contains("explode") || hardness <
						 * 0) { writable.addProperty("shouldSpawnInExplosion",
						 * false); }
						 *
						 * JsonArray mechanics = new JsonArray();
						 * mechanics.add(new
						 * JsonPrimitive("EnvironmentGravity"));
						 * mechanics.add(new
						 * JsonPrimitive("EnvironmentResponse"));
						 *
						 * if (hasMethod(block.getClass(),
						 * "onEntityCollidedWithBlock")) { System.out.println(
						 * "Has special method!"); mechanics.add(new
						 * JsonPrimitive("BlockInheritance")); mechanics.add(new
						 * JsonPrimitive("ClientBlockInheritance")); }
						 *
						 * writable.add("mechanics", mechanics); String fileName
						 * = "C:/GenGSON/" + blockID + ".json"; try { FileWriter
						 * writer = new FileWriter(fileName);
						 * gson.toJson(writable, writer); // writer.flush();
						 * writer.close(); } catch (IOException e) {
						 * e.printStackTrace(); } return null;
						 */
						return null;
					}
				});
			}
		}

		Thread blockLoadQueue = new Thread(loadQueue(), "Block Load Queue");
		blockLoadQueue.start();

	}

	// TODO Add domains + don't load if already exists.

	private PhysicsBlockMetadata getMetadata(String name, Map<String, Object> json) {
		ServerPhysicsOverworld overworld = Physics.getInstance().getCommonProxy().getPhysicsOverworld();
		PhysicsBlockMetadata metadata = metadataMap.get(name);
		if (metadata == null) {
			metadata = new PhysicsBlockMetadata();
			if (json.containsKey("friction"))
				metadata.friction = new Float((Double) json.get("friction")).floatValue();
			if (json.containsKey("mass"))
				metadata.mass = new Float((Double) json.get("mass")).floatValue();
			if (json.containsKey("shouldSpawnInExplosion"))
				metadata.shouldSpawnInExplosion = (Boolean) json.get("shouldSpawnInExplosion");
			if (json.containsKey("overrideCollisionShape"))
				metadata.defaultCollisionShape = (Boolean) json.get("overrideCollisionShape");
			if (json.containsKey("restitution"))
				metadata.restitution = new Float((Double) json.get("restitution")).floatValue();
			if (json.containsKey("collisionEnabled")) {
				metadata.collisionEnabled = (Boolean) json.get("collisionEnabled");
			}

			if (json.containsKey("mechanics")) {
				ArrayList<String> mechanicNames = (ArrayList<String>) json.get("mechanics");
				for (int i = 0; i < mechanicNames.size(); i++) {
					RigidBodyMechanic mechanic = overworld.getMechanicFromName(mechanicNames.get(i));
					metadata.mechanics.add(mechanic);

				}
			}
		}
		return metadata;
	}

	public abstract Map<String, Object> loadMetadataJSON(String name) throws JsonSyntaxException, IOException;
}
