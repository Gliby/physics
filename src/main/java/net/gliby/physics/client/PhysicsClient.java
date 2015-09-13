package net.gliby.physics.client;

import net.gliby.gman.item.ItemHandler;
import net.gliby.physics.Physics;
import net.gliby.physics.client.gui.GuiDebug;
import net.gliby.physics.client.keybindings.KeyManager;
import net.gliby.physics.client.render.Render;
import net.gliby.physics.common.PhysicsServer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

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

	private Render render;

	@Override
	public void init(FMLInitializationEvent event) {
		Physics.getLogger().info("Started!");
		boolean dynamicLightsPresent = Loader.isModLoaded("DynamicLights");
		if (dynamicLightsPresent) {
			Physics.getLogger().info("DynamicLights by AtomicStryker has been found, enabling dynamic light creation!");
		}
		ItemHandler itemHandler = ItemHandler.getInstance();

		itemHandler.addAlwaysUsedItem(Physics.itemPhysicsGun, false, false);
		itemHandler.addAlwaysUsedItem(Physics.itemToolgun, false, false);
		// TODO Settings
		MinecraftForge.EVENT_BUS.register(physicsWorld = new ClientPhysicsOverworld());
		// MinecraftForge.EVENT_BUS.register(new
		// EntityDeathHandler(physicsWorld));
		render = new Render(dynamicLightsPresent);
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
