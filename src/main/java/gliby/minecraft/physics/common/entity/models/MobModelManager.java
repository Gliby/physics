package gliby.minecraft.physics.common.entity.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;

import gliby.minecraft.gman.io.MinecraftResourceLoader;
import gliby.minecraft.physics.Physics;
import io.netty.util.internal.StringUtil;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.FMLCommonHandler;
import scala.reflect.api.Internals.ReificationSupportApi.SyntacitcSingletonTypeExtractor;

/**
 * Loads mobs from generated JSONs.
 **/
public class MobModelManager {

	//
	private Map<Class, MobModel> modelRegistry;

	public Map<Class, MobModel> getModelRegistry() {
		return modelRegistry;
	}

	public MobModelManager(final Physics physics) {
		this.modelRegistry = new HashMap<Class, MobModel>();
		Thread loadingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				File tempFile = null;
				ZipFile tempZip = null;
				if ((tempFile = new File(physics.getSettings().getDirectory(), "/custom/mobs.zip")).exists()) {
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
				Iterator<Map.Entry<Integer, Class>> it = EntityList.idToClassMapping.entrySet().iterator();
				Gson gson = new Gson();
				while (it.hasNext()) {
					Map.Entry<Integer, Class> entry = it.next();
					if (entry.getValue() != null) {
						String mobModel = null;
						int uniqueEntityId = entry.getKey();
						final ZipFile otherZip = tempZip;
						final File otherFile = tempFile;
						boolean loadedFromZip = false;
						if (otherFile.exists()) {
							ZipEntry zipEntry = otherZip.getEntry(uniqueEntityId + ".json");
							if (zipEntry != null) {
								InputStream stream = null;
								try {
									stream = otherZip.getInputStream(zipEntry);
								} catch (IOException e) {
									e.printStackTrace();
								}
								if (stream != null) {
									loadedFromZip = true;
									try {
										mobModel = IOUtils.toString(stream);
										stream.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
						} else {
							try {
								InputStream stream = MinecraftResourceLoader.getResource(Physics.getLogger(),
										FMLCommonHandler.instance().getSide(),
										new ResourceLocation(Physics.ID, "mobs/" + uniqueEntityId + ".json"));
								if (stream != null) {
									mobModel = IOUtils.toString(stream);
									stream.close();
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						if (mobModel != null) {
							MobModel model;
							//System.out.println("loaded: " + entry.getValue());
							modelRegistry.put(entry.getValue(), model = gson.fromJson(mobModel, MobModel.class));
							loaded++;
						}
					}
					
					if (tempZip != null) {
						try {
							tempZip.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				physics.getLogger().info("Loaded " + loaded + " mob models.");
			}
		}, "Mob Model Loader");
		loadingThread.start();
	}

}
