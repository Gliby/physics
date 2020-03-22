package gliby.minecraft.physics.common.blocks;

import com.google.gson.JsonSyntaxException;
import gliby.minecraft.gman.GMan;
import gliby.minecraft.gman.io.MinecraftResourceLoader;
import gliby.minecraft.physics.MetadataLoader;
import gliby.minecraft.physics.Physics;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * BlockManager is responsible for blocks and their physics metadata.
 */
public class BlockManager {

    private HashMap<ResourceLocation, PhysicsBlockMetadata> physicsBlockMetadata;
    private Map<String, IBlockGenerator> blockGenerators;
    private IBlockGenerator defaultGenerator;

    public BlockManager(Physics physics) {
        blockGenerators = new HashMap<String, IBlockGenerator>();
        defaultGenerator = new DefaultBlockGenerator();
        File tempFile;
        ZipFile tempZip = null;
        if ((tempFile = new File(physics.getConfig().getDirectory(), "/custom/blocks.zip")).exists()) {
            try {
                Physics.getLogger().info("Blocks found under configuration directory.");
                tempZip = new ZipFile(tempFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        final ZipFile otherZip = tempZip;
        final File otherFile = tempFile;
        MetadataLoader loader = new MetadataLoader(physics, this,
                physicsBlockMetadata = new HashMap<ResourceLocation, PhysicsBlockMetadata>()) {
            @SuppressWarnings("unchecked")
            @Override
            public Map<String, Object> loadMetadata(ResourceLocation location) throws JsonSyntaxException, IOException {
                String name = getBlockString(location);
                if (otherFile.exists()) {
                    ZipEntry entry = otherZip.getEntry(name + ".json");
                    if (entry != null) {
                        InputStream stream = otherZip.getInputStream(entry);
                        if (stream != null) {
                            String s = IOUtils.toString(stream);
                            stream.close();
                            return GMan.getGSON().fromJson(s, Map.class);
                        }
                    }
                }

                String text = IOUtils.toString(
                        MinecraftResourceLoader.getResource(Physics.getLogger(), FMLCommonHandler.instance().getSide(),
                                new ResourceLocation(Physics.ID, "blocks/" +  name + ".json")));
                if (text != null) {
                    return GMan.getGSON().fromJson(text, Map.class);
                }
                return null;
            }
        };
    }

    /**
     * @return the physicsBlockMetadata
     */
    public Map<ResourceLocation, PhysicsBlockMetadata> getPhysicsBlockMetadata() {
        return physicsBlockMetadata;
    }

    public ResourceLocation getBlockIdentity(Block block) {
        final ResourceLocation location = ForgeRegistries.BLOCKS.getKey(block);
        return location;

    }

    public String getBlockString(ResourceLocation resourceLocation) {
        return resourceLocation.getResourceDomain() + '.' + resourceLocation.getResourcePath();
    }

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

}
