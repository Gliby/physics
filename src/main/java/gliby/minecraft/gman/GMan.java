package gliby.minecraft.gman;

import com.google.common.base.Predicate;
import com.google.gson.Gson;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GMan {

    private final static String LOCATION = "https://raw.githubusercontent.com/Gliby/Mod-Information-Storage/master/";
    private final Predicate ACCEPT_ALL = new Predicate<String>() {

        @Override
        public boolean apply(String input) {
            return true;
        }
    };
    public HashMap<String, Object> properties;
    private Logger logger;
    private ModInfo modInfo;
    private String minecraftVersion;
    private String modVersion;

    public static Gson getGSON() {
        return GSON;
    }

    protected static final Gson GSON = new Gson();

    public GMan(Logger logger, ModInfo modInfo, String minecraftVersion, String modVersion) {
        this.logger = logger;
        this.modInfo = modInfo;
        this.minecraftVersion = minecraftVersion;
        this.modVersion = modVersion;
        this.properties = new HashMap<String, Object>();
    }

    public static boolean isDevelopment() {
        boolean development = (Boolean) (Launch.blackboard.get("fml.deobfuscatedEnvironment"));
        return development;
    }

    public static GMan create(final Logger logger, ModInfo modInfo, final String minecraftVersion,
                              final String modVersion) {
        StringBuilder builder = new StringBuilder();
        builder.append(LOCATION);
        builder.append(modInfo.modId);
        builder.append("/mod.json");
        final Gson gson = GMan.getGSON();
        Reader reader = null;
        try {
            reader = new InputStreamReader(new URL(builder.toString()).openStream());
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            logger.warn("Failed to retrieve mod info, either mod doesn't exist or host(" + builder.toString()
                    + ") is down?");
        }
        if (reader != null && !isDevelopment()) {
            final ModInfo externalInfo = gson.fromJson(reader, ModInfo.class);
            modInfo.donateURL = externalInfo.donateURL;
            modInfo.updateURL = externalInfo.updateURL;
            modInfo.versions = externalInfo.versions;
            modInfo.determineUpdate(modVersion, minecraftVersion);
            logger.info(modInfo.isUpdated() ? String.format("Mod is up-to-date. (%s)", modVersion)
                    : "Mod is outdated, download latest at " + modInfo.updateURL);
        }
        return new GMan(logger, modInfo, minecraftVersion, modVersion);
    }

    public String[] getVersionsBetween(String from, String to, Predicate<String> predicate) {
        int fromNum = Integer.parseInt(from.trim().replaceAll("\\.", ""));
        int toNum = Integer.parseInt(to.trim().replaceAll("\\.", ""));
        ArrayList<String> list = new ArrayList<String>();
        for (int i = fromNum + 1; i <= toNum; i++) {
            String numberedVersion = String.format("%03d", i);
            if (numberedVersion.length() <= 3) {
                String version = numberedVersion.substring(0, 1) + "." + numberedVersion.substring(1, 2) + "."
                        + numberedVersion.substring(2, 3);
                if (predicate.apply(version)) {
                    list.add(version);
                }
            }

        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getVersionsBetween(String from, String to) {
        return getVersionsBetween(from, to, ACCEPT_ALL);
    }

    public GMan request(CustomRequest customRequest) {
        customRequest.request(this);
        return this;
    }

    public Object getJSON(String filePath, Class clz) {
        StringBuilder builder = new StringBuilder();
        builder.append(LOCATION);
        builder.append(modInfo.modId);
        builder.append("/");
        builder.append(filePath);
        Reader reader = null;
        try {
            reader = new InputStreamReader(new URL(builder.toString()).openStream());
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            logger.warn("Failed to retrieve URL, doesn't exist or host(" + builder.toString() + ") is down?");
            e.printStackTrace();
        }
        if (reader != null)
            return GMan.getGSON().fromJson(reader, clz);
        return null;
    }

    public BufferedImage getImage(String filePath) {
        StringBuilder builder = new StringBuilder();
        builder.append(LOCATION);
        builder.append(modInfo.modId);
        builder.append("/");
        builder.append(filePath);
        BufferedImage image = null;
        try {
            URL url = new URL(builder.toString());
            image = ImageIO.read(url);
        } catch (IOException e) {
            logger.warn(
                    "Failed to retrieve image from URL, doesn't exist or host(" + builder.toString() + ") is down?");
            e.printStackTrace();
        }
        return image;
    }

    public Map<String, Object> getJSONMap(String filePath) {
        StringBuilder builder = new StringBuilder();
        builder.append(LOCATION);
        builder.append(modInfo.modId);
        builder.append("/");
        builder.append(filePath);

        Reader reader = null;
        try {
            reader = new InputStreamReader(new URL(builder.toString()).openStream());
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            logger.warn("Failed to retrieve URL, doesn't exist or host(" + builder.toString() + ") is down?");
            e.printStackTrace();
        }
        if (reader != null)
            return GMan.getGSON().fromJson(reader, Map.class);
        return null;
    }

    public ModInfo getModInfo() {
        return modInfo;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public interface CustomRequest {

        void request(GMan gman);

    }

}
