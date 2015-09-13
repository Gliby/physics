package net.gliby.physics;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.gliby.gman.RawItem;
import net.gliby.gman.settings.SettingsHandler;
import net.gliby.physics.client.PhysicsClient;
import net.gliby.physics.common.PhysicsServer;
import net.gliby.physics.common.entity.EntityPhysicsBlock;
import net.gliby.physics.common.entity.EntityPhysicsModelPart;
import net.gliby.physics.common.entity.EntityToolGunBeam;
import net.gliby.physics.common.entity.IEntityPhysics;
import net.gliby.physics.common.game.items.ItemPhysicsGun;
import net.gliby.physics.common.game.items.ItemSpawnRigidBody;
import net.gliby.physics.common.game.items.toolgun.ItemToolGun;
import net.gliby.physics.common.packets.PacketPlayerJoin;
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

@Mod(modid = Physics.MOD_ID, name = Physics.MOD_NAME, version = "0.0.1")
public class Physics {

	/**
	 * Cache that contains classes generated from configuration.
	 */
	public static Class[] entityBlacklistClassCache;

	public static final String MOD_NAME = "Gliby's Physics";
	public static final String MOD_ID = "glibysphysics";

	@Instance
	private static Physics instance;

	@SidedProxy(serverSide = "net.gliby.physics.common.PhysicsServer", clientSide = "net.gliby.physics.client.PhysicsClient")
	private static PhysicsServer proxy;

	private static Item itemRigidBodySpawner;

	public static RawItem itemPhysicsGun, itemToolgun;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Physics.instance = this;
		registerEntity(EntityPhysicsBlock.class, "PhysicsBlock", 64, 1, false);
		registerEntity(EntityPhysicsModelPart.class, "Model Part Entity", 64, 1, false);
		registerEntity(EntityToolGunBeam.class, "Tool Gun Beam", 64, 1, false);
		File dir = new File(event.getModConfigurationDirectory(), MOD_ID);
		if (!dir.exists())
			dir.mkdir();
		settings = new SettingsHandler(new File(dir, "Settings.ini"));

		// TODO Add on client
		settings.registerBoolean("PhysicsEngine", "UseJavaPhysics", false, Side.SERVER);

		settings.registerObject("PhysicsEntities", "EntityColliderBlacklist", new String[] {
				IEntityPhysics.class.getName(), EntityToolGunBeam.class.getName(), EntityItem.class.getName() },
				Side.SERVER);

		settings.registerFloat("PhysicsEntities", "InactivityDeathTime", 30, Side.SERVER);
		settings.registerFloat("PhysicsEntities", "EntityColliderCleanupTime", 0.25f, Side.SERVER);
		settings.registerFloat("Game", "ProjectileImpulseForce", 30, Side.SERVER);
		settings.registerFloat("Game", "ExplosionImpulseRadius", 16, Side.SERVER);
		settings.registerFloat("Game", "ExplosionImpulseForce", 150, Side.SERVER);

		settings.registerInteger("Tools", "AttractRadius", 16, Side.SERVER);
		settings.registerInteger("Tools", "GravitizerRadius", 16, Side.SERVER);
		settings.registerInteger("Tools", "GravitizerForce", 10, Side.SERVER);
		settings.registerInteger("Tools", "AttractForce", 10, Side.SERVER);

		settings.registerBoolean("Miscellaneous", "DisableAllowFlight", true, Side.SERVER);
		settings.load();

		/*
		 * EntityRegistry.registerGlobalEntityID(EntityPhysicsRagdoll.class,
		 * "Ragdoll Entity", baseId + 1);
		 * EntityRegistry.registerModEntity(EntityPhysicsRagdoll.class,
		 * "Ragdoll Entity", baseId + 1, this, 64, 1, false);
		 */

		// EntityRegistry.registerGlobalEntityID(EntityPhysicsModelPart.class,
		// "Model Part Entity", baseId + 2);
		// EntityRegistry.registerModEntity(EntityPhysicsModelPart.class, "Model
		// Part Entity", baseId + 2, this, 64, 1, false);

		// EntityRegistry.registerGlobalEntityID(EntityPhysicsModelPart.class,
		// "Model Part Entity", baseId + 3);
		// EntityRegistry.registerModEntity(EntityPhysicsModelPart.class, "Model
		// Part Entity", baseId + 3, this, 64, 1, false);

		GameRegistry.registerItem(itemToolgun = new ItemToolGun(this), itemToolgun.getUnlocalizedName());
		GameRegistry.registerItem(itemRigidBodySpawner = new ItemSpawnRigidBody(),
				itemRigidBodySpawner.getUnlocalizedName());
		GameRegistry.registerItem(itemPhysicsGun = new ItemPhysicsGun(this), itemPhysicsGun.getUnlocalizedName());
		// GameRegistry.registerItem(itemPhysicsTransformer = new
		// ItemPhysicsTransformer(),
		// itemPhysicsTransformer.getUnlocalizedName());
		// GameRegistry.registerItem(itemRagdollSpawner = new
		// ItemSpawnRagdoll(), itemRagdollSpawner.getUnlocalizedName());

		MinecraftForge.EVENT_BUS.register(Physics.itemPhysicsGun);
		MinecraftForge.EVENT_BUS.register(Physics.itemToolgun);

		registerPacket(PacketPlayerJoin.class, PacketPlayerJoin.class, Side.CLIENT);

		proxy.preInit(event);
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

		proxy.init(event);
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

}
