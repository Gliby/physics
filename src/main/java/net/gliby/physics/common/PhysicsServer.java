package net.gliby.physics.common;

import java.util.ArrayList;
import java.util.Arrays;

import net.gliby.gman.settings.BooleanSetting;
import net.gliby.gman.settings.SettingsHandler;
import net.gliby.physics.Physics;
import net.gliby.physics.common.entity.EntityToolGunBeam;
import net.gliby.physics.common.entity.IEntityPhysics;
import net.gliby.physics.common.event.ExplosionHandler;
import net.gliby.physics.common.items.toolgun.ToolGunActionRegistry;
import net.gliby.physics.common.items.toolgun.defaultactions.ToolGunAlignAction;
import net.gliby.physics.common.items.toolgun.defaultactions.ToolGunAttachAction;
import net.gliby.physics.common.items.toolgun.defaultactions.ToolGunAttractAction;
import net.gliby.physics.common.items.toolgun.defaultactions.ToolGunChangeGravityAction;
import net.gliby.physics.common.items.toolgun.defaultactions.ToolGunRemoveAction;
import net.gliby.physics.common.items.toolgun.defaultactions.ToolGunReviveAction;
import net.gliby.physics.common.packets.PacketPlayerJoin;
import net.gliby.physics.common.physics.ServerPhysicsOverworld;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;
import net.minecraftforge.fml.relauncher.Side;

public class PhysicsServer {

	private ServerPhysicsOverworld physicsWorld;

	public void preInit(FMLPreInitializationEvent event) {
	}

	public void init(FMLInitializationEvent event) {
	}

	public void postInit(FMLPostInitializationEvent event) {
	}

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
				Physics.getDispatcher().sendTo(
						new PacketPlayerJoin(ToolGunActionRegistry.getInstance().getValueDefinitions()),
						(EntityPlayerMP) event.player);
				event.player
						.addChatComponentMessage(new ChatComponentText("Running: " + System.getProperty("os.name")));
			}

		});
	}

	private static boolean hasStartedOnce;

	public final void serverAboutToStart(Physics physics, FMLServerAboutToStartEvent event) {
		if (!hasStartedOnce) {

			MinecraftForge.EVENT_BUS.register(physicsWorld = new ServerPhysicsOverworld());
			MinecraftForge.EVENT_BUS.register(new ExplosionHandler(physics));
			FMLCommonHandler.instance().bus().register(this);
			ToolGunActionRegistry.getInstance().registerAction(new ToolGunAttachAction(), Physics.MOD_ID);
			ToolGunActionRegistry.getInstance().registerAction(new ToolGunReviveAction(), Physics.MOD_ID);
			ToolGunActionRegistry.getInstance().registerAction(new ToolGunAlignAction(), Physics.MOD_ID);
			ToolGunActionRegistry.getInstance().registerAction(new ToolGunAttractAction(), Physics.MOD_ID);
			ToolGunActionRegistry.getInstance().registerAction(new ToolGunChangeGravityAction(), Physics.MOD_ID);
			ToolGunActionRegistry.getInstance().registerAction(new ToolGunRemoveAction(), Physics.MOD_ID);

			hasStartedOnce = !hasStartedOnce;
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

	public final ServerPhysicsOverworld getPhysicsOverworld() {
		return physicsWorld;
	}

}
