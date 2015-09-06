package net.gliby.physics.client;

import net.gliby.gman.item.ItemHandler;
import net.gliby.gman.settings.SettingsHandler;
import net.gliby.physics.Physics;
import net.gliby.physics.client.gui.GuiDebug;
import net.gliby.physics.client.keybindings.KeyManager;
import net.gliby.physics.client.render.Render;
import net.gliby.physics.client.render.entity.RenderPhysicsBlock;
import net.gliby.physics.client.resources.ResourceManager;
import net.gliby.physics.common.PhysicsServer;
import net.gliby.physics.common.entity.EntityPhysicsBlock;
import net.gliby.physics.common.event.EntityDeathHandler;
import net.gliby.physics.common.event.ExplosionHandler;
import net.gliby.physics.common.physics.ServerPhysicsOverworld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelZombie;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

public class PhysicsClient extends PhysicsServer {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		ResourceManager.init();
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
