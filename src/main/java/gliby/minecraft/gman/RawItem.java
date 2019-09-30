/**
 * Copyright (c) 2015, Mine Fortress.
 */
package gliby.minecraft.gman;

import gliby.minecraft.gman.client.render.RawItemRenderer;
import net.minecraft.entity.player.EntityPlayer;
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
    @SideOnly(Side.CLIENT)
    public net.minecraft.client.resources.model.ModelResourceLocation getModel(ItemStack stack, EntityPlayer player,
                                                                               int useRemaining) {
        this.renderer.setOwner(player);
        return super.getModel(stack, player, useRemaining);
    }

    /**
     * @param itemRender
     */
    @SideOnly(Side.CLIENT)
    public void setRenderer(RawItemRenderer itemRender) {
        this.renderer = itemRender;
    }
}
