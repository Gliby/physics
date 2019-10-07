package gliby.minecraft.gman.io;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MinecraftResourceLoader {

    public static InputStream getResource(Logger logger, Side side, ResourceLocation location) {
        if (side.isClient()) {
            Minecraft mc = Minecraft.getMinecraft();
            try {
                return mc.getResourceManager().getResource(location).getInputStream();
            } catch (FileNotFoundException e) {
                if (logger != null)
                    logger.warn("Couldn't find resource: " + location);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String fileLocation = "/assets/" + location.getResourceDomain() + "/" + location.getResourcePath();
            return MinecraftServer.class.getResourceAsStream(fileLocation);
        }
        return null;
    }

}
