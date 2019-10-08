package gliby.minecraft.gman;

import com.google.common.base.Predicate;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import gliby.minecraft.physics.Physics;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GMan {

    public static final boolean GMAN_DEBUG = false;

    protected static final Gson GSON = new Gson();
    // net.minecraftforge.common.ForgeVersion results
    protected static final int RESULTS_FIELD = 11;
    private final static String LOCATION = "https://raw.githubusercontent.com/Gliby/Mod-Information-Storage/master/";
    private static final int MAX_HTTP_REDIRECTS = Integer.getInteger("http.maxRedirects", 20);
    private final Predicate ACCEPT_ALL = new Predicate<String>() {

        @Override
        public boolean apply(String input) {
            return true;
        }
    };
    public HashMap<String, Object> properties;
    private Logger logger;
    private ModInfo modInfo;

    public GMan(Logger logger, ModInfo modInfo) {
        this.logger = logger;
        this.modInfo = modInfo;
        this.properties = new HashMap<String, Object>();
    }

    public static Gson getGSON() {
        return GSON;
    }

    public static boolean isNotDevelopment() {
        boolean development = (Boolean) (Launch.blackboard.get("fml.deobfuscatedEnvironment"));
        return !development || GMAN_DEBUG;
    }

    public static GMan create(final Logger logger, ModInfo modInfo, final String minecraftVersion,
                              final String modVersion) {
        StringBuilder builder = new StringBuilder();
        builder.append(LOCATION);
        builder.append(modInfo.modId);
        builder.append("/mod.json");
        final Gson gson = GMan.getGSON();
        try {
            Reader reader = new InputStreamReader(new URL(builder.toString()).openStream());
            if (isNotDevelopment()) {
                ModContainer modContainer = Loader.instance().activeModContainer();
                final ModInfo externalInfo = gson.fromJson(reader, ModInfo.class);
                modInfo.donateURL = externalInfo.donateURL;
                modInfo.updateURL = externalInfo.updateURL;
                modInfo.versions = externalInfo.versions;
                modInfo.determineUpdate(modVersion, minecraftVersion);
                modInfo.applyToMod(modContainer);
                addCheckResult(modInfo, modContainer);
                logger.info(modInfo.isUpdated() ? String.format("Mod is up-to-date. (%s)", modVersion)
                        : String.format("Mod is outdated (%s), download latest at (%s)", modVersion, modInfo.updateURL));
            }
            reader.close();
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            logger.warn("Failed to retrieve mod info, either mod doesn't exist or host(" + builder.toString()
                    + ") is down?");
        }
        return new GMan(logger, modInfo);
    }

    /**
     * :)
     * https://xkcd.com/927/
     */
    public static void addCheckResult(ModInfo modInfo, ModContainer container) {
        try {
            Field resultField = ForgeVersion.class.getDeclaredFields()[RESULTS_FIELD];
            resultField.setAccessible(true);
            Map<ModContainer, ForgeVersion.CheckResult> results = (Map<ModContainer, ForgeVersion.CheckResult>) resultField.get(null);
            if (results != null) {
                Constructor<ForgeVersion.CheckResult> constructor = (Constructor<ForgeVersion.CheckResult>) ForgeVersion.CheckResult.class.getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                ForgeVersion.Status status = modInfo.getStatus();
                String url = modInfo.updateURL;
                ForgeVersion.CheckResult result = constructor.newInstance(status, null, null, url);
                results.put(container, result);
                Physics.getLogger().info("Registered update information to Forge.");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
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

    public <O> O getJSONObject(String filePath, Class<O> classOfO) {
        StringBuilder builder = new StringBuilder();
        builder.append(LOCATION);
        builder.append(modInfo.modId);
        builder.append("/");
        builder.append(filePath);
        final String url = builder.toString();

        try {
            InputStream con = openUrlStream(new URL(url));
            String data = new String(ByteStreams.toByteArray(con), StandardCharsets.UTF_8);
            con.close();
            return GMan.getGSON().fromJson(data, classOfO);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            logger.warn("Failed to retrieve URL, doesn't exist or host(" + builder.toString() + ") is down?");
            e.printStackTrace();
        }

        return null;
    }

    public <T extends BufferedImage> T getImage(String filePath) {
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
        return (T) image;
    }

    public Map<String, Object> getJSONMap(String filePath) {
        StringBuilder builder = new StringBuilder();
        builder.append(LOCATION);
        builder.append(modInfo.modId);
        builder.append("/");
        builder.append(filePath);

        try {
            Reader reader = new InputStreamReader(new URL(builder.toString()).openStream());
            return GMan.getGSON().fromJson(reader, Map.class);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            logger.warn("Failed to retrieve URL, doesn't exist or host(" + builder.toString() + ") is down?");
            e.printStackTrace();
        }
        return null;
    }

    public ModInfo getModInfo() {
        return modInfo;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    /**
     * Opens stream for given URL while following redirects
     */
    private InputStream openUrlStream(URL url) throws IOException {
        URL currentUrl = url;
        for (int redirects = 0; redirects < MAX_HTTP_REDIRECTS; redirects++) {
            URLConnection c = currentUrl.openConnection();
            if (c instanceof HttpURLConnection) {
                HttpURLConnection huc = (HttpURLConnection) c;
                huc.setInstanceFollowRedirects(false);
                int responseCode = huc.getResponseCode();
                if (responseCode >= 300 && responseCode <= 399) {
                    try {
                        String loc = huc.getHeaderField("Location");
                        currentUrl = new URL(currentUrl, loc);
                        continue;
                    } finally {
                        huc.disconnect();
                    }
                }
            }

            return c.getInputStream();
        }
        throw new IOException("Too many redirects while trying to fetch " + url);
    }

    public interface CustomRequest {

        void request(GMan gman);

    }

}
