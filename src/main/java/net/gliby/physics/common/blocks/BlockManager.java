package net.gliby.physics.common.blocks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.gliby.gman.io.MinecraftResourceLoader;
import net.gliby.physics.MetadataLoader;
import net.gliby.physics.Physics;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.ICollisionShape;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;

public class BlockManager {

	private Physics physics;

	/**
	 * @return the physicsBlockMetadata
	 */
	public Map<String, PhysicsBlockMetadata> getPhysicsBlockMetadata() {
		return physicsBlockMetadata;
	}

	private HashMap<String, PhysicsBlockMetadata> physicsBlockMetadata;

	public BlockManager(Physics physics) {
		this.physics = physics;
		blockShapeCache = new BlockShapeCache();
		blockGenerators = new HashMap<String, IBlockGenerator>();
		defaultGenerator = new DefaultBlockGenerator();
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
		MetadataLoader loader = new MetadataLoader(physics, this,
				physicsBlockMetadata = new HashMap<String, PhysicsBlockMetadata>()) {
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
	
	private Map<String, IBlockGenerator> blockGenerators;

	public String getBlockIdentity(Block block) {
		UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(block);
		return id.modId + "." + id.name;
	}

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
