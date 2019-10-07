package gliby.minecraft.gman.client.render;

import gliby.minecraft.gman.RawItem;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
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

    public void registerItemRenderer(RawItem item, RawItemRenderer itemRender) {
        itemRender.setItemInstance(item);
        this.itemRenderer.add(itemRender);
        item.setRenderer(itemRender);
//        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, itemRender.modelResourceLocation);
    }

    @SubscribeEvent
    public void bakeModel(ModelBakeEvent event) {
        for (int i = 0; i < itemRenderer.size(); i++) {
            RawItemRenderer itemRender = itemRenderer.get(i);
            event.getModelRegistry().putObject(itemRender.modelResourceLocation, itemRender);

        }
    }

    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event) {
        for (int i = 0; i < itemRenderer.size(); i++) {
            RawItemRenderer itemRender = itemRenderer.get(i);
            ModelLoader.setCustomModelResourceLocation(itemRender.getItemInstance(), 0, itemRender.modelResourceLocation);
//            ModelBakery.registerItemVariants(itemRender.getItemInstance(), itemRender.modelResourceLocation);

        }
    }
}
