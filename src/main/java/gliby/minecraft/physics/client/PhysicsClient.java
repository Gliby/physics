package gliby.minecraft.physics.client;

import gliby.minecraft.gman.item.ItemHandler;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.VersionChanges;
import gliby.minecraft.physics.client.gui.GuiScreenChangeLog;
import gliby.minecraft.physics.client.keybindings.KeyManager;
import gliby.minecraft.physics.client.render.RenderHandler;
import gliby.minecraft.physics.client.render.world.RenderDebugAdditionalWorld;
import gliby.minecraft.physics.common.PhysicsServer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class PhysicsClient extends PhysicsServer {

    private KeyManager keyManager;
    private RenderHandler renderHandler;
    private SoundHandler soundHandler;


    private boolean init;

    @Override
    public void preInit(Physics physics, FMLPreInitializationEvent event) {
        // GuiDebug
        //MinecraftForge.EVENT_BUS.register(new GuiDebug());

        this.keyManager = new KeyManager();
        keyManager.init();
        renderHandler = new RenderHandler(physics);
        renderHandler.preInit(event);

        MinecraftForge.EVENT_BUS.register(soundHandler = new SoundHandler());
//        MinecraftForge.EVENT_BUS.register(new RenderDebugAdditionalWorld(renderHandler));


    }

    public KeyManager getKeyMananger() {
        return keyManager;
    }

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
        Physics.getLogger().info("Started!");
        renderHandler.init(event);

        // TODO unfinished: EntityDeathHandler
        // MinecraftForge.EVENT_BUS.register(new
        // EntityDeathHandler(physicsWorld));

        ItemHandler itemHandler = ItemHandler.getInstance();

        itemHandler.addAlwaysUsedItem(physics.getGameManager().itemPhysicsGun, false, false);
        itemHandler.addAlwaysUsedItem(physics.getGameManager().itemToolgun, false, false);
        MinecraftForge.EVENT_BUS.register(this);

    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }

    /**
     * @return soundHandler
     */
    public SoundHandler getSoundHandler() {
        return soundHandler;
    }

    public RenderHandler getRenderHandler() {
        return renderHandler;
    }
}
