package gliby.minecraft.gman;

import gliby.minecraft.gman.client.render.RawItemRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Gliby
 */
public abstract class RawItem extends Item {

    @SideOnly(Side.CLIENT)
    private RawItemRenderer renderer;

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        this.renderer.setOwner(player);
        super.onUsingTick(stack, player, count);
    }

    /**
     * @param itemRender
     */
    @SideOnly(Side.CLIENT)
    public void setRenderer(RawItemRenderer itemRender) {
        this.renderer = itemRender;
    }
}
