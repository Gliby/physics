package gliby.minecraft.physics;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Predicate;

import gliby.minecraft.gman.GMan;
import gliby.minecraft.gman.ModInfo;
import gliby.minecraft.gman.RawItem;
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
import gliby.minecraft.physics.common.entity.IEntityPhysics;
import gliby.minecraft.physics.common.entity.models.MobModelManager;
import gliby.minecraft.physics.common.game.GameManager;
import gliby.minecraft.physics.common.game.items.ItemPhysicsGun;
import gliby.minecraft.physics.common.game.items.toolgun.ItemToolGun;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunActionRegistry;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunAlignAction;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunAttachAction;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunAttractAction;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunChangeGravityAction;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunRemoveAction;
import gliby.minecraft.physics.common.game.items.toolgun.actions.ToolGunReviveAction;
import gliby.minecraft.physics.common.packets.PacketPlayerJoin;
import gliby.minecraft.physics.common.physics.PhysicsOverworld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = Physics.MOD_ID, name = Physics.MOD_NAME, version = Physics.MOD_VERSION, guiFactory = "gliby.minecraft.physics.client.gui.options.GuiFactory")
public class Physics {

	/**
	 * Cache that contains classes generated from configuration.
	 */
	public static Class[] entityBlacklistClassCache;

	public static final String MOD_NAME = "Gliby's Physics";
	public static final String MOD_ID = "glibysphysics";
	public static final String MOD_VERSION = "0.2";

	@Instance
	private static Physics instance;

	@SidedProxy(serverSide = "gliby.minecraft.physics.common.PhysicsServer", clientSide = "gliby.minecraft.physics.client.PhysicsClient")
	private static PhysicsServer proxy;
	
	/**
	 * Manages anything game related, e.g items.
	 */
	private GameManager gameManager;

	private GMan gman;

	private PhysicsOverworld physicsOverworld;

	public GMan getGMan() {
		return gman;
	}


	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		this.instance = this;
		registerEntity(EntityPhysicsBlock.class, "PhysicsBlock", 64, 1, false);
		registerEntity(EntityPhysicsModelPart.class, "Model Part Entity", 64, 1, false);
		registerEntity(EntityToolGunBeam.class, "Tool Gun Beam", 64, 1, false);
		File dir = new File(event.getModConfigurationDirectory(), MOD_ID);
		if (!dir.exists())
			dir.mkdir();
		
		settings = new SettingsHandler(dir, new File(dir, "Settings.ini"));

		settings.registerBoolean("PhysicsEngine", "UseJavaPhysics", true, Setting.Side.SERVER);

		settings.registerObject("PhysicsEntities", "EntityColliderBlacklist", new String[] {
				IEntityPhysics.class.getName(), EntityToolGunBeam.class.getName(), EntityItem.class.getName() },
				Setting.Side.SERVER);
		settings.registerInteger("PhysicsEngine", "TickRate", 30, Setting.Side.SERVER);
		settings.registerFloat("PhysicsEngine", "GravityForce", -9.8f, Setting.Side.SERVER);
		settings.registerFloat("PhysicsEntities", "InactivityDeathTime", 30, Setting.Side.SERVER);
		settings.registerFloat("PhysicsEntities", "EntityColliderCleanupTime", 0.25f, Setting.Side.SERVER);
		settings.registerFloat("Game", "ProjectileImpulseForce", 30, Setting.Side.SERVER);
		settings.registerFloat("Game", "ExplosionImpulseRadius", 16, Setting.Side.SERVER);
		settings.registerFloat("Game", "ExplosionImpulseForce", 300, Setting.Side.SERVER);
		settings.registerInteger("Tools", "AttractRadius", 16, Setting.Side.SERVER);
		settings.registerInteger("Tools", "GravitizerRadius", 16, Setting.Side.SERVER);
		settings.registerInteger("Tools", "GravitizerForce", 10, Setting.Side.SERVER);
		settings.registerInteger("Tools", "AttractForce", 10, Setting.Side.SERVER);

		settings.registerString("Miscellaneous", "LastVersion", MOD_VERSION, Setting.Side.BOTH);
		settings.registerBoolean("Miscellaneous", "DisableAllowFlight", true, Setting.Side.SERVER);
		settings.load();
		gman = GMan.create(getLogger(), new ModInfo(MOD_ID, event.getModMetadata().updateUrl),
				MinecraftForge.MC_VERSION, MOD_VERSION);

		StringSetting lastVersion = settings.getStringSetting("Miscellaneous.LastVersion");
		final boolean modUpdated = !lastVersion.getString().equals(MOD_VERSION);
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
							String versions[] = gman.getVersionsBetween(MOD_VERSION,
									gman.getModInfo().getLatestVersion(), new Predicate<String>() {
										@Override
										public boolean apply(String input) {
											return index.contains(input) && !input.equals(MOD_VERSION);
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
							gman.getProperties().put("VersionChanges", changes);
						}
					}, "GMAN Update/News").start();
				}
			});
		}

		lastVersion.setString(MOD_VERSION);
		settings.save();
		gameManager = new GameManager(this);
		gameManager.preInit();

		registerPacket(PacketPlayerJoin.class, PacketPlayerJoin.class, Side.CLIENT);

		physicsOverworld = new PhysicsOverworld(this);
		blockManager = new BlockManager(this);
		// TODO feature: finish this, would help with server-side ragdolls.
		//mobModelManager = new MobModelManager(this);

		FMLCommonHandler.instance().bus().register(this);
		proxy.preInit(this, event);
		getLogger().info("Pre-initialization completed on " + FMLCommonHandler.instance().getEffectiveSide());
	}

	private static int entityIDIndex = 0;

	public void registerPacket(Class handler, Class packet, Side side) {
		getDispatcher().registerMessage(packet, handler, packetIDIndex++, side);
	}

	protected void registerEntity(Class<? extends Entity> clazz, String name, int trackRange, int trackFrequency,
			boolean trackVelocity) {
		EntityRegistry.registerGlobalEntityID(clazz, name, EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerModEntity(clazz, name, entityIDIndex++, this, trackRange, trackFrequency, trackVelocity);
	}

	private static final SimpleNetworkWrapper DISPATCHER = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

	/**
	 * @return the dispatcher
	 */
	public static SimpleNetworkWrapper getDispatcher() {
		return DISPATCHER;
	}

	private static int packetIDIndex;

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

	private static final Logger LOGGER = LogManager.getLogger("Gliby's Physics");

	public static Logger getLogger() {
		return LOGGER;
	}

	public static Physics getInstance() {
		return instance;
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

	private SettingsHandler settings;

	public SettingsHandler getSettings() {
		return settings;
	}

	private BlockManager blockManager;

	public BlockManager getBlockManager() {
		return blockManager;
	}

	private MobModelManager mobModelManager;

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
