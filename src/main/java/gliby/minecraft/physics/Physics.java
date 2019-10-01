package gliby.minecraft.physics;

import com.google.common.base.Predicate;
import gliby.minecraft.gman.GMan;
import gliby.minecraft.gman.ModInfo;
import gliby.minecraft.gman.settings.Setting;
import gliby.minecraft.gman.settings.SettingsHandler;
import gliby.minecraft.gman.settings.StringSetting;
import gliby.minecraft.physics.client.PhysicsClient;
import gliby.minecraft.physics.common.IPhysicsProxy;
import gliby.minecraft.physics.common.PhysicsServer;
import gliby.minecraft.physics.common.blocks.BlockManager;
import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import gliby.minecraft.physics.common.entity.EntityPhysicsModelPart;
import gliby.minecraft.physics.common.entity.EntityToolGunBeam;
import gliby.minecraft.physics.common.entity.models.MobModelManager;
import gliby.minecraft.physics.common.game.GameManager;
import gliby.minecraft.physics.common.packets.PacketPlayerJoin;
import gliby.minecraft.physics.common.physics.PhysicsOverworld;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

// TODO: look into ObjectPooling.
@Mod(modid = Physics.ID, name = Physics.NAME, guiFactory = "gliby.minecraft.physics.client.gui.options.GuiFactory")
public class Physics {

    public static final String VERSION = "@VERSION@";
    public static final String NAME = "Gliby's Physics";
    public static final String ID = "glibysphysics";
    private static final SimpleNetworkWrapper DISPATCHER = NetworkRegistry.INSTANCE.newSimpleChannel(ID);
    private static final Logger LOGGER = LogManager.getLogger("Gliby's Physics");
        /**
         * Cache that contains classes generated from configuration.
         */
        public static Class[] entityBlacklistClassCache;
        @Instance
        private static Physics instance;
        @SidedProxy(serverSide = "gliby.minecraft.physics.common.PhysicsServer", clientSide = "gliby.minecraft.physics.client.PhysicsClient")
        private static PhysicsServer proxy;
        private static int entityIDIndex = 0;
        private static int packetIDIndex;
        /**
         * Manages anything game related, e.g items.
         */
        private GameManager gameManager;
        private GMan gman;
        private PhysicsOverworld physicsOverworld;
        private SettingsHandler settings;
        private BlockManager blockManager;
        private MobModelManager mobModelManager;

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

        public static boolean isDevelopment() {
            return Launch.blackboard.get("fml.deobfuscatedEnvironment") != null;
        }

        public GMan getGMan() {
            return gman;
        }

        @EventHandler
        public void preInit(FMLPreInitializationEvent event) {
            instance = this;

            registerEntity(EntityPhysicsBlock.class, "PhysicsBlock", 64, 1, false);
            registerEntity(EntityPhysicsModelPart.class, "Model Part Entity", 64, 1, false);
            registerEntity(EntityToolGunBeam.class, "Tool Gun Beam", 64, 1, false);
            File dir = new File(event.getModConfigurationDirectory(), ID);
            if (!dir.exists())
                dir.mkdir();

            settings = new SettingsHandler(dir, new File(dir, "Settings.ini"));

            settings.registerBoolean("PhysicsEngine", "UseJavaPhysics", true, Setting.Side.BOTH);

            // TODO re-implement: entity blacklist
            /*
             * settings.registerObject("PhysicsEntities", "EntityColliderBlacklist", new
             * String[] { IEntityPhysics.class.getName(), EntityToolGunBeam.class.getName(),
             * EntityItem.class.getName() }, Setting.Side.BOTH);
             */

            settings.registerInteger("PhysicsEngine", "TickRate", 20, Setting.Side.BOTH);
            settings.registerFloat("PhysicsEngine", "GravityForce", -9.8f, Setting.Side.BOTH);
            settings.registerFloat("PhysicsEntities", "InactivityDeathTime", 30, Setting.Side.BOTH);
            settings.registerFloat("PhysicsEntities", "EntityColliderCleanupTime", 0.25f, Setting.Side.BOTH);
            settings.registerFloat("Game", "ProjectileImpulseForce", 30, Setting.Side.BOTH);
            settings.registerFloat("Game", "ExplosionImpulseRadius", 16, Setting.Side.BOTH);
            settings.registerFloat("Game", "ExplosionImpulseForce", 100, Setting.Side.BOTH);
            settings.registerInteger("Tools", "AttractRadius", 16, Setting.Side.BOTH);
            settings.registerInteger("Tools", "GravitizerRadius", 16, Setting.Side.BOTH);
            settings.registerInteger("Tools", "GravitizerForce", 10, Setting.Side.BOTH);
            settings.registerInteger("Tools", "AttractForce", 10, Setting.Side.BOTH);

            settings.registerString("Miscellaneous", "LastVersion", VERSION, Setting.Side.BOTH);
            settings.registerBoolean("Miscellaneous", "DisableAllowFlight", true, Setting.Side.BOTH);
            settings.load();
            gman = GMan.create(getLogger(), new ModInfo(ID, event.getModMetadata().updateUrl), MinecraftForge.MC_VERSION,
                    VERSION, !isDevelopment());

            if (!isDevelopment()) {
                StringSetting lastVersionSetting = settings.getStringSetting("Miscellaneous.LastVersion");
                final String lastVersion = settings.getStringSetting("Miscellaneous.LastVersion").getString();
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
                                        VersionChanges versionChanges = (VersionChanges) gman.getJSON("news/" + s + ".json",
                                                VersionChanges.class);

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
                lastVersionSetting.setString(VERSION);
            } else {
                getLogger().info("Development environment detected.");
            }
        settings.save();
        gameManager = new GameManager(this);
        gameManager.preInit();

        registerPacket(PacketPlayerJoin.class, PacketPlayerJoin.class, Side.CLIENT);

        physicsOverworld = new PhysicsOverworld(this);
        blockManager = new BlockManager(this);
        // TODO feature: finish this, would help with server-side ragdolls.
        // mobModelManager = new MobModelManager(this);

        FMLCommonHandler.instance().bus().register(physicsOverworld);
        FMLCommonHandler.instance().bus().register(this);
        proxy.preInit(this, event);
        getLogger().info("Pre-initialization completed on " + FMLCommonHandler.instance().getEffectiveSide());
    }

    public void registerPacket(Class handler, Class packet, Side side) {
        getDispatcher().registerMessage(packet, handler, packetIDIndex++, side);
    }

    protected void registerEntity(Class<? extends Entity> clazz, String name, int trackRange, int trackFrequency,
                                  boolean trackVelocity) {
        EntityRegistry.registerGlobalEntityID(clazz, name, EntityRegistry.findGlobalUniqueEntityId());
        EntityRegistry.registerModEntity(clazz, name, entityIDIndex++, this, trackRange, trackFrequency, trackVelocity);
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

    public SettingsHandler getSettings() {
        return settings;
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
