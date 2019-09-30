package gliby.minecraft.gman.client.render;

import gliby.minecraft.gman.RawItem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gliby
 * <p>
 * Manages raw item renderer's.
 */
@SideOnly(Side.CLIENT)
public class ItemRendererManager {


    private static ItemRendererManager instance;
    private List<RawItemRenderer> itemRenderer = new ArrayList<RawItemRenderer>();

    /**
     * Should be created on FMLInitializationEvent event, everything takes care
     * of it's self.
     */
    private ItemRendererManager() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ItemRendererManager getInstance() {
        if (instance == null) instance = new ItemRendererManager();
        return instance;
    }

    public void registerItemRenderer(RawItem item, RawItemRenderer itemRender) {
        this.itemRenderer.add(itemRender);
        item.setRenderer(itemRender);
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, itemRender.resourceLocation);
    }

    @SubscribeEvent
    public void bakeModel(ModelBakeEvent event) {
        System.out.println("baking model");
        for (int i = 0; i < itemRenderer.size(); i++) {
            RawItemRenderer itemRender = itemRenderer.get(i);
            event.modelRegistry.putObject(itemRender.resourceLocation, itemRender);

        }
    }
}
