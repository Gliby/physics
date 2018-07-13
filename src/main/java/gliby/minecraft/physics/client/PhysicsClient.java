package gliby.minecraft.physics.client;

import java.util.ArrayList;

import gliby.minecraft.gman.item.ItemHandler;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.VersionChanges;
import gliby.minecraft.physics.client.gui.GuiScreenChangeLog;
import gliby.minecraft.physics.client.keybindings.KeyManager;
import gliby.minecraft.physics.client.render.RenderHandler;
import gliby.minecraft.physics.common.PhysicsServer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
	public void preInit(Physics physics, FMLPreInitializationEvent event) {
		// TODO debug: re-add GuiDebug
		//MinecraftForge.EVENT_BUS.register(new GuiDebug());
		this.keyManager = new KeyManager();
		keyManager.init();
	}

	private KeyManager keyManager;

	public KeyManager getKeyMananger() {
		return keyManager;
	}

	private RenderHandler render;

	private boolean init;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onJoin(PlayerTickEvent event) {
		if (event.phase == Phase.END) {
			if (event.side == Side.CLIENT && !init) {
				final Minecraft mc = Minecraft.getMinecraft();
				final Physics physics = Physics.getInstance();
				if (physics.getGMan().getProperties().containsKey("VersionChanges")) {
					mc.displayGuiScreen(new GuiScreenChangeLog(
							(ArrayList<VersionChanges>) physics.getGMan().getProperties().get("VersionChanges")));
					physics.getGMan().getProperties().remove("VersionChanges");
					Physics.getLogger().info("Displayed change logs.");
				}
				init = true;
			}
		}
	}

	@Override
	public void init(Physics physics, FMLInitializationEvent event) {
		physics.getLogger().info("Started!");
		/*
		 * TODO cosmetic: Enable dynamic lights. boolean dynamicLightsPresent =
		 * Loader.isModLoaded("DynamicLights"); if (dynamicLightsPresent) {
		 * physics.getLogger().info(
		 * "DynamicLights by AtomicStryker has been found, enabling dynamic light creation!"
		 * ); }
		 */ ItemHandler itemHandler = ItemHandler.getInstance();

		itemHandler.addAlwaysUsedItem(physics.getGameManager().itemPhysicsGun, false, false);
		itemHandler.addAlwaysUsedItem(physics.getGameManager().itemToolgun, false, false);
		FMLCommonHandler.instance().bus().register(this);
		// TODO unfinished: EntityDeathHandler
		// MinecraftForge.EVENT_BUS.register(new
		// EntityDeathHandler(physicsWorld));
		render = new RenderHandler(physics,
				false);
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
