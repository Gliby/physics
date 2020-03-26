package gliby.minecraft.physics;

import com.google.common.base.Predicate;
import gliby.minecraft.gman.GMan;
import gliby.minecraft.gman.ModInfo;
import gliby.minecraft.gman.networking.GDataSerializers;
import gliby.minecraft.physics.client.PhysicsClient;
import gliby.minecraft.physics.common.IPhysicsProxy;
import gliby.minecraft.physics.common.PhysicsServer;
import gliby.minecraft.physics.common.blocks.BlockManager;
import gliby.minecraft.physics.common.entity.models.MobModelManager;
import gliby.minecraft.physics.common.game.GameManager;
import gliby.minecraft.physics.common.packets.PacketReceiveTools;
import gliby.minecraft.physics.common.physics.PhysicsOverworld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
// Gliby's Physics Central Roadmap
// TODO: (0.7.0) Add cosmetic physics.
// TODO: (0.6.0) Add EntityPhysicsBase buffer culling, meaning if we are over our allowed Physics Entity limit, we force the the oldest entity to re-align.
// TODO  (0.6.0) look into NativeBullet by the Terasology, might solve memory leaks in the native PhysicsWorld and improve simulation perf.
// TODO  (0.6.0) FIXME: something is leaking memory every time we create/destroy a PhysicsWorld.
// TODO (0.6.0) Replace Settings with Forge's configuration
// TODO (0.6.0) Start using BlockPos.PooledMutableBlockPos
// TODO (0.6.0) Start using world.isAreaLoaded()

@Mod(modid = Physics.ID, name = Physics.NAME, acceptedMinecraftVersions = Physics.MC_VERSION, guiFactory = "gliby.minecraft.physics.client.gui.options.GuiFactory")
public class Physics {

    public static final String MC_VERSION = "@MC_VERSION@";
    public static final String VERSION = "@VERSION@";
    public static final String NAME = "Gliby's Physics";
    public static final String ID = "glibysphysics";

    private static final SimpleNetworkWrapper DISPATCHER = NetworkRegistry.INSTANCE.newSimpleChannel(ID);
    private static final Logger LOGGER = LogManager.getLogger(NAME);
    /**
     * Cache that contains classes generated from configuration.
     */
    public static Class[] entityBlacklistClassCache = new Class[0];
    @Instance
    private static Physics instance;
    @SidedProxy(serverSide = "gliby.minecraft.physics.common.PhysicsServer", clientSide = "gliby.minecraft.physics.client.PhysicsClient")
    private static PhysicsServer proxy;

    private static int packetIDIndex;
    /**
     * Manages anything game related, e.g items.
     */
    private GameManager gameManager;
    private GMan gman;
    private BlockManager blockManager;
    private MobModelManager mobModelManager;
    private PhysicsOverworld physicsOverworld;

    static {
        GDataSerializers.register();
    }

    /**
     * @return the dispatcher
     */
    public static SimpleNetworkWrapper getDispatcher() {
        return DISPATCHER;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Physics getInstance() {
        return instance;
    }

    public GMan getGMan() {
        return gman;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        MinecraftForge.EVENT_BUS.register(this);
        File dir = new File(event.getModConfigurationDirectory(), ID);
        if (!dir.exists())
            dir.mkdir();

        PhysicsConfig.setModDirectory(dir);


        gman = GMan.create(getLogger(), new ModInfo(ID, event.getModMetadata().updateUrl), MinecraftForge.MC_VERSION,
                VERSION);

        if (GMan.isNotDevelopment()) {
            final String lastVersion = PhysicsConfig.MISCELLANEOUS.lastVersion;
            final boolean modUpdated = !lastVersion.equals(VERSION);
            if (modUpdated) {
                getLogger().info("Version change detected, gathering change logs!");
                gman.request(new GMan.CustomRequest() {

                    @Override
                    public void request(final GMan gman) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                Map<String, Object> json = gman.getJSONMap("news/index.json");
                                final ArrayList<String> index = (ArrayList<String>) json.get("Versions");
                                String[] versions = gman.getVersionsBetween(lastVersion,
                                        gman.getModInfo().getLatestVersion(), new Predicate<String>() {
                                            @Override
                                            public boolean apply(String input) {
                                                return index.contains(input) && !input.equals(lastVersion);
                                            }
                                        });
                                ArrayList<VersionChanges> changes = new ArrayList<VersionChanges>();
                                for (String s : versions) {
                                    VersionChanges versionChanges = gman.getJSONObject("news/" + s + ".json", VersionChanges.class);

                                    if (versionChanges != null)
                                        if (versionChanges.getChanges() != null)
                                            changes.add(versionChanges.setVersion(s)
                                                    .setImage(gman.getImage("news/" + s + ".png")));
                                }
                                if (!changes.isEmpty())
                                    gman.getProperties().put("VersionChanges", changes);
                                else
                                    getLogger().info("No version changes.");
                            }
                        }, "GMAN Update/News").start();
                    }
                });
            }
            PhysicsConfig.MISCELLANEOUS.lastVersion = VERSION;
            ConfigManager.sync(ID, Config.Type.INSTANCE);
        } else {
            getLogger().info("Development environment detected.");
        }

        gameManager = new GameManager(this);
        MinecraftForge.EVENT_BUS.register(gameManager);

        gameManager.preInit();

        registerPacket(PacketReceiveTools.class, PacketReceiveTools.class, Side.CLIENT);

        physicsOverworld = new PhysicsOverworld(this);
        blockManager = new BlockManager(this);
        // TODO (0.7.0) feature: finish this, would help with server-side ragdolls.
        // mobModelManager = new MobModelManager(this);

        MinecraftForge.EVENT_BUS.register(physicsOverworld);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(proxy);
        proxy.preInit(this, event);
        getLogger().info("Pre-initialization completed on " + FMLCommonHandler.instance().getEffectiveSide());
    }


    public void registerPacket(Class handler, Class packet, Side side) {
        getDispatcher().registerMessage(packet, handler, packetIDIndex++, side);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(this, event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        proxy.serverAboutToStart(this, event);
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        proxy.serverStarted(event);
    }

    public IPhysicsProxy getProxy() {
        return proxy;
    }

    public PhysicsServer getCommonProxy() {
        return proxy;
    }

    @SideOnly(Side.CLIENT)
    public PhysicsClient getClientProxy() {
        return (PhysicsClient) proxy;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public MobModelManager getMobModelManager() {
        return mobModelManager;
    }

    public PhysicsOverworld getPhysicsOverworld() {
        return physicsOverworld;
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}
