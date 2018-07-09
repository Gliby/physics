package net.gliby.minecraft.physics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonSyntaxException;

import net.gliby.minecraft.physics.common.blocks.BlockManager;
import net.gliby.minecraft.physics.common.blocks.PhysicsBlockMetadata;
import net.gliby.minecraft.physics.common.entity.mechanics.RigidBodyMechanic;
import net.gliby.minecraft.physics.common.physics.PhysicsOverworld;
import net.minecraft.block.Block;

/**
 *
 */
public abstract class MetadataLoader {

	private HashMap<String, PhysicsBlockMetadata> actualMap;
	private ConcurrentHashMap<String, PhysicsBlockMetadata> tempData;
	private BlockManager blockManager;
	private Physics physics;

	public MetadataLoader(Physics physics, BlockManager blockManager,
			HashMap<String, PhysicsBlockMetadata> metadataMap) {
		this.physics = physics;
		this.actualMap = metadataMap;
		this.tempData = new ConcurrentHashMap<String, PhysicsBlockMetadata>();
		this.blockManager = blockManager;
		start();
	}

	private BlockingQueue<Callable> blockLoadQueue = new LinkedBlockingQueue<Callable>();

	private int loaded;

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
						}
					}
				}
				actualMap.putAll(tempData);
				tempData.clear();
				Physics.getLogger().info("Loaded " + loaded + " physics blocks.");
			}

		};
	}

	public void start() {
		Iterator<Block> itr = Block.blockRegistry.iterator();
		while (itr.hasNext()) {
			final Block block = itr.next();
			final String blockID = blockManager.getBlockIdentity(block);
			if (!tempData.containsKey(blockID)) {
				blockLoadQueue.offer(new Callable() {
					@Override
					public Object call() throws JsonSyntaxException, IOException {
						Map<String, Object> json = null;
						if ((json = loadMetadata(blockID)) != null) {
							PhysicsBlockMetadata metadata = formatMetadata(blockID, json);
							tempData.put(blockID, metadata);
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

		Thread blockLoadQueue = new Thread(loadQueue(), "Block Loader");
		blockLoadQueue.start();

	}

	private PhysicsBlockMetadata formatMetadata(String name, Map<String, Object> json) {
		PhysicsOverworld overworld = Physics.getInstance().getPhysicsOverworld();
		PhysicsBlockMetadata metadata = tempData.get(name);
		if (metadata == null) {
			metadata = new PhysicsBlockMetadata();
			if (json.containsKey("friction"))
				metadata.friction = new Float((Double) json.get("friction")).floatValue();
			if (json.containsKey("mass"))
				metadata.mass = new Float((Double) json.get("mass")).floatValue();
			if (json.containsKey("shouldSpawnInExplosion"))
				metadata.spawnInExplosions = (Boolean) json.get("shouldSpawnInExplosion");
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

	public abstract Map<String, Object> loadMetadata(String name) throws JsonSyntaxException, IOException;
}
