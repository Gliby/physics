package gliby.minecraft.physics.common.entity.models;

import gliby.minecraft.physics.Physics;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Loads mobs from generated JSONs.
 **/
public class MobModelManager {

    //
    private Map<Class, MobModel> modelRegistry;

    public MobModelManager(final Physics physics) {
        this.modelRegistry = new HashMap<Class, MobModel>();
        Thread loadingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                File tempFile = null;
                ZipFile tempZip = null;
                if ((tempFile = new File(physics.getConfig().getDirectory(), "/custom/mobs.zip")).exists()) {
                    try {
                        Physics.getLogger().info("Mob models found under configuration directory.");
                        tempZip = new ZipFile(tempFile);
                    } catch (ZipException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                int loaded = 0;
                // TODO (0.7.0) (unfinished)
//              net.minecraftforge.fml.common.registry.EntityEntry entry = net.minecraftforge.registries.GameData.getEntityRegistry();

//                Gson gson = new Gson();
//                for (ResourceLocation entityResource : entities) {
//                    String mobModel = null;
//                    int uniqueEntityId = EntityList.getID()
//                    final ZipFile otherZip = tempZip;
//                    final File otherFile = tempFile;
//                    boolean loadedFromZip = false;
//                    if (otherFile.exists()) {
//                        ZipEntry zipEntry = otherZip.getEntry(uniqueEntityId + ".json");
//                        if (zipEntry != null) {
//                            InputStream stream = null;
//                            try {
//                                stream = otherZip.getInputStream(zipEntry);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            if (stream != null) {
//                                loadedFromZip = true;
//                                try {
//                                    mobModel = IOUtils.toString(stream);
//                                    stream.close();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    } else {
//                        try {
//                            InputStream stream = MinecraftResourceLoader.getResource(Physics.getLogger(),
//                                    FMLCommonHandler.instance().getSide(),
//                                    new ResourceLocation(Physics.ID, "mobs/" + uniqueEntityId + ".json"));
//                            if (stream != null) {
//                                mobModel = IOUtils.toString(stream);
//                                stream.close();
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    if (mobModel != null) {
//                        MobModel model;
//                        //System.out.println("loaded: " + entry.getValue());
//                        modelRegistry.put(entry.getValue(), model = gson.fromJson(mobModel, MobModel.class));
//                        loaded++;
//                    }
//
//
//                    if (tempZip != null) {
//                        try {
//                            tempZip.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
                Physics.getLogger().info("Loaded " + loaded + " mob models.");
            }
        }, "Mob Model Loader");
        loadingThread.start();
    }

    public Map<Class, MobModel> getModelRegistry() {
        return modelRegistry;
    }

}
