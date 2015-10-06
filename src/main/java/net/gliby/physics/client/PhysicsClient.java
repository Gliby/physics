package net.gliby.physics.client;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.gliby.gman.item.ItemHandler;
import net.gliby.physics.Physics;
import net.gliby.physics.VersionChanges;
import net.gliby.physics.client.gui.GuiDebug;
import net.gliby.physics.client.gui.GuiScreenChangeLog;
import net.gliby.physics.client.gui.GuiScreenWelcome;
import net.gliby.physics.client.gui.creator.GuiScreenPhysicsCreator;
import net.gliby.physics.client.keybindings.KeyManager;
import net.gliby.physics.client.render.RenderHandler;
import net.gliby.physics.common.PhysicsServer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PhysicsClient extends PhysicsServer {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new GuiDebug());
		KeyManager.getInstance().init();
		Minecraft mc = Minecraft.getMinecraft();

	}

	private ClientPhysicsOverworld physicsWorld;

	/**
	 * @return the physicsWorld
	 */
	public ClientPhysicsOverworld getPhysicsOverWorld() {
		return physicsWorld;
	}

	private RenderHandler render;

	boolean playerCreated;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onJoin(PlayerTickEvent event) {
		if (event.phase == Phase.END) {
			if (event.side == Side.CLIENT && !playerCreated) {
				final Minecraft mc = Minecraft.getMinecraft();
				final Physics physics = Physics.getInstance();
				if (physics.getSettings().isFirstTime()) {
					mc.displayGuiScreen(new GuiScreenWelcome());
				} else if (physics.getGMan().getProperties().containsKey("VersionChanges")) {
					mc.displayGuiScreen(new GuiScreenChangeLog(
							(ArrayList<VersionChanges>) physics.getGMan().getProperties().get("VersionChanges")));
					physics.getGMan().getProperties().remove("VersionChanges");
				}
				playerCreated = true;
			}
		}
	}

	@Override
	public void init(Physics physics, FMLInitializationEvent event) {
		physics.getLogger().info("Started!");
		boolean dynamicLightsPresent = Loader.isModLoaded("DynamicLights");
		if (dynamicLightsPresent) {
			physics.getLogger().info("DynamicLights by AtomicStryker has been found, enabling dynamic light creation!");
		}
		ItemHandler itemHandler = ItemHandler.getInstance();

		itemHandler.addAlwaysUsedItem(physics.itemPhysicsGun, false, false);
		itemHandler.addAlwaysUsedItem(physics.itemToolgun, false, false);
		// TODO Settings
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(physicsWorld = new ClientPhysicsOverworld(physics));
		// MinecraftForge.EVENT_BUS.register(new
		// EntityDeathHandler(physicsWorld));
		render = new RenderHandler(physics, dynamicLightsPresent);
		render.init(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {

	}

	/**
	 * @return soundHandler
	 */
	public SoundHandler getSoundHandler() {
		return SoundHandler.getInstance();
	}
}
