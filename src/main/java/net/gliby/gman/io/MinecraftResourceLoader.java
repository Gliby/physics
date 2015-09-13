package net.gliby.gman.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class MinecraftResourceLoader {

	public static InputStream getResource(Logger logger, Side side, ResourceLocation location) {
		if (side.isClient()) {
			Minecraft mc = Minecraft.getMinecraft();
			try {
				InputStream stream = mc.mcDefaultResourcePack.getInputStream(location);
				return stream;
			} catch (FileNotFoundException e) {
				if (logger != null)
					logger.warn("Couldn't find resource: " + location);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			String fileLocation = "/assets/" + location.getResourceDomain() + "/" + location.getResourcePath();
			InputStream stream = MinecraftServer.class.getResourceAsStream(fileLocation);
			return stream;
		}
		return null;
	}

}
