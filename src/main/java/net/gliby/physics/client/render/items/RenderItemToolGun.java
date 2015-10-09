/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.client.render.items;

import javax.vecmath.Vector3f;

import net.gliby.gman.client.render.RawItemRenderer;
import net.gliby.physics.Physics;
import net.gliby.physics.common.game.items.toolgun.ItemToolGun;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

/**
 *
 */
public class RenderItemToolGun extends RawItemRenderer {

	private ModelRenderer bipedRightArm;

	/**
	 * @param resourceLocation
	 */
	public RenderItemToolGun(ModelResourceLocation resourceLocation) {
		super(resourceLocation);
		this.bipedRightArm = new ModelRenderer(playerBiped, 40, 16);
		this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0);
		this.bipedRightArm.setRotationPoint(-5.0F, 2.0F + 0, 0.0F);
	}

	private ModelItemToolGun modelToolGun = new ModelItemToolGun();
	private ResourceLocation modelTexture = new ResourceLocation(Physics.MOD_ID, "textures/tool_gun.png");
	private ResourceLocation screenTexture = new ResourceLocation(Physics.MOD_ID, "textures/tool_gunScreen.png");

	@Override
	public void render() {
		float scale = -0.0625f;
		String text = "Fine in 4K";

		if (owner != null) {
			if (transformType == TransformType.FIRST_PERSON) {
				GlStateManager.rotate(-2f, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(9.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(22.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.translate(-0.5F, 0.27F, -0.2F);
				text = ((ItemToolGun) itemStack.getItem()).getModeName();
				if (!owner.isInvisible()) {
					textureManager.bindTexture(((AbstractClientPlayer) mc.thePlayer).getLocationSkin());
					GlStateManager.pushMatrix();
					GlStateManager.scale(1.25f, 1.25f, 1.25f);
					GlStateManager.translate(-0.4f, -0.17f, -0.89f);
					bipedRightArm.rotateAngleX = -93 / (180F / (float) Math.PI);
					bipedRightArm.render(scale);
					GlStateManager.popMatrix();
				}
			}
		}

		textureManager.bindTexture(modelTexture);
		modelToolGun.render(null, 0, 0, 0, 0, 0, scale);
		// if (transformType == TransformType.FIRST_PERSON) {

		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);
		GlStateManager.pushMatrix();
		scale *= 0.125f;
		// GlStateManager.disableTexture2D();
		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(modelToolGun.screen.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
		GlStateManager.translate(-16, 0, -62.41f);

		GlStateManager.pushMatrix();
		textureManager.bindTexture(screenTexture);
		scale = 0.25f;
		GlStateManager.scale(scale, scale, 1);
		Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 128, 128, 128, 128);
		GlStateManager.popMatrix();
		if (transformType != TransformType.GUI) {

			// Gui.drawRect(0, 0, 32, 32, 0xFF00FF00);
			GlStateManager.translate(0, 0, -0.01f);

			int length = mc.fontRendererObj.getStringWidth(text);
			scale = MathHelper.clamp_float(35.5F / length, 0, 0.50f);
			GlStateManager.translate((240 / length) + (scale / 2), 11.0f - (((mc.fontRendererObj.FONT_HEIGHT - 1) * scale) / 2), 0);
			GlStateManager.scale(scale, scale, 0);
			mc.fontRendererObj.drawString(text, 0, 0, -1);
		}
		// Gui.drawRect(0, 0, 3000, 3000, 0xFF999999);
		// GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
		// }
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return new ItemCameraTransforms(new ItemTransformVec3f(new Vector3f(90, 0, 0), new Vector3f(0, 0.2f, -0.15f), new Vector3f(0.5f, 0.5f, -0.5f)), new ItemTransformVec3f(new Vector3f(27, -39, 8), new Vector3f(0.2f, 0.3f, -0.1f), new Vector3f(-1.15f, -1.15f, -1.15f)), ItemTransformVec3f.DEFAULT, new ItemTransformVec3f(new Vector3f(0, 90, 0), new Vector3f(-0.15f, 0, 0), new Vector3f(-1.25F,
				-1.25F, -1.25F)));
	}
}
