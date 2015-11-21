/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.client.render.entity;

import javax.vecmath.Vector3f;

import net.gliby.physics.client.render.RenderUtilities;
import net.gliby.physics.common.entity.EntityToolGunBeam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

/**
 *
 */
public class RenderToolGunBeam extends Render {

	private Minecraft mc;

	/**
	 * @param renderManager
	 */
	public RenderToolGunBeam(Minecraft mc) {
		super(mc.getRenderManager());
		this.mc = mc;
	}

	public void doRender(Entity uncast, double entityX, double entityY, double entityZ, float twen, float partialTick) {
		EntityToolGunBeam entity = (EntityToolGunBeam) uncast;				
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		Vector3f worldTranslation = RenderUtilities.getWorldTranslation(mc, partialTick);
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 1);
		GlStateManager.pushMatrix();
		GlStateManager.translate(-worldTranslation.x, -worldTranslation.y, -worldTranslation.z);
		float alpha = (entity.msUntilGone - MathHelper.clamp_float((System.currentTimeMillis() - entity.timeCreated), 0, entity.msUntilGone)) / entity.msUntilGone;
		worldRenderer.startDrawing(3);
		worldRenderer.setColorRGBA_F(1, 1, 1, alpha);
		if(entity.clientOrigin != null && renderManager.options.thirdPersonView == 0) {
			worldRenderer.addVertex(entity.clientOrigin.x, entity.clientOrigin.y, entity.clientOrigin.z);
		} else {
			worldRenderer.addVertex(entity.worldOrigin.x, entity.worldOrigin.y, entity.worldOrigin.z);
		}
		worldRenderer.addVertex(entity.hit.x, entity.hit.y, entity.hit.z);
		tessellator.draw();
		
		//Render lighting
		
		GlStateManager.popMatrix();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.minecraft.client.renderer.entity.Render#getEntityTexture(net.minecraft
	 * .entity.Entity)
	 */
	 @Override
	 protected final ResourceLocation getEntityTexture(Entity parEntity) {
		return getEntityTexture((EntityToolGunBeam) parEntity);
	}

}
