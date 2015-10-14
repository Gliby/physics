package net.gliby.physics.common;

import net.gliby.gman.settings.BooleanSetting;
import net.gliby.physics.Physics;
import net.gliby.physics.common.game.events.ExplosionHandler;
import net.gliby.physics.common.packets.PacketPlayerJoin;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class PhysicsServer implements IPhysicsProxy {

	public void preInit(Physics physics, FMLPreInitializationEvent event) {
	}

	public void init(Physics physics, FMLInitializationEvent event) {
	}

	public void postInit(FMLPostInitializationEvent event) {
	}

	// TODO Replace this event.
	@SubscribeEvent
	public void playerJoinEvent(final PlayerLoggedInEvent event) {
		MinecraftServer.getServer().addScheduledTask(new Runnable() {

			@Override
			public void run() {
				if (event.player instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) event.player;
					synchronized (this) {
						try {
							wait(player.ping + 20);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				Physics physics = Physics.getInstance();
				physics.getDispatcher().sendTo(new PacketPlayerJoin(physics.getToolGunRegistry().getValueDefinitions()),
						(EntityPlayerMP) event.player);
				event.player
						.addChatComponentMessage(new ChatComponentText("Running: " + System.getProperty("os.name")));
			}

		});
	}

	boolean hasStarted;

	public final void serverAboutToStart(Physics physics, FMLServerAboutToStartEvent event) {
		if (!hasStarted) {
			MinecraftForge.EVENT_BUS.register(new ExplosionHandler(physics));
			hasStarted = true;
		}
	}

	public final void serverStarted(FMLServerStartedEvent event) {
		BooleanSetting setting;
		if ((setting = Physics.getInstance().getSettings().getBooleanSetting("Miscellaneous.DisableAllowFlight"))
				.getBooleanValue()) {
			Physics.getLogger()
					.warn("Configuration Setting: " + setting + " has disabled server.properties allow-flight.");
			MinecraftServer.getServer().setAllowFlight(true);
		}
	}
}
