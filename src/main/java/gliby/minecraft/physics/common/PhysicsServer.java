package gliby.minecraft.physics.common;

import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.PhysicsConfig;
import gliby.minecraft.physics.common.game.events.GameEventHandler;
import gliby.minecraft.physics.common.packets.PacketReceiveTools;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class PhysicsServer implements IPhysicsProxy {

    boolean hasStarted;

    public void preInit(Physics physics, FMLPreInitializationEvent event) {
    }

    public void init(Physics physics, FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    @SubscribeEvent
    public void playerJoinEvent(final PlayerLoggedInEvent event) {
        event.player.getServer().addScheduledTask(() -> {
            Physics physics = Physics.getInstance();
            Physics.getDispatcher().sendTo(new PacketReceiveTools(physics.getGameManager().getToolGunRegistry().getValueDefinitions()),
                    (EntityPlayerMP) event.player);
        });
    }

    @SubscribeEvent
    public void playerLeaveEvent(final PlayerEvent.PlayerLoggedOutEvent event) {
        event.player.getServer().addScheduledTask(() -> {
            Physics physics = Physics.getInstance();
            PhysicsWorld world = physics.getPhysicsOverworld().getPhysicsByWorld(event.player.world);
            if (world != null)
                physics.getGameManager().getToolGunRegistry().stopUsingAll(world, (EntityPlayerMP) event.player);
        });
    }

    public final void serverAboutToStart(Physics physics, FMLServerAboutToStartEvent event) {
        if (!hasStarted) {
            MinecraftForge.EVENT_BUS.register(new GameEventHandler(physics));
            hasStarted = true;
        }
    }

    public final void serverStarted(FMLServerStartedEvent event) {
        if (PhysicsConfig.MISCELLANEOUS.disableAllowFlight) {
            Physics.getLogger()
                    .warn("Configuration Setting: " + "'DisableAllowFlight'" + " has disabled server.properties allow-flight.");
            FMLCommonHandler.instance().getMinecraftServerInstance()
                    .setAllowFlight(true);
        }
    }
}
