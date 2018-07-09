/**
 * Copyright (c) 2015, Mine Fortress.
 */
package gliby.minecraft.physics.client.render.items;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;

/**
 *
 */
public class ModelItemPhysicsGun extends ModelBase {

	private ModelRenderer Shape1;
	private ModelRenderer Shape1_1;
	private ModelRenderer Shape1_2;
	private ModelRenderer Shape1_3;
	private ModelRenderer Shape1_4;
	private ModelRenderer Shape1_5;
	private ModelRenderer Shape1_6;
	private ModelRenderer Shape1_7;
	private ModelRenderer Shape1_8;
	private ModelRenderer Shape1_9;
	private ModelRenderer Shape1_10;
	private ModelRenderer Shape1_11;
	private ModelRenderer Shape1_12;
	private ModelRenderer Shape1_13;
	private ModelRenderer Shape1_14;
	private ModelRenderer Shape1_15;
	private ModelRenderer Shape1_16;
	private ModelRenderer Shape1_17;
	private ModelRenderer Shape1_18;
	private ModelRenderer Shape1_19;
	private ModelRenderer Shape1_20;
	private ModelRenderer Shape1_21;
	private ModelRenderer Shape1_22;
	private ModelRenderer Shape1_23;
	private ModelRenderer Shape1_24;
	private ModelRenderer Shape1_25;
	private ModelRenderer Shape1_27;
	private ModelRenderer Shape1_28;
	private ModelRenderer Shape1_29;
	private ModelRenderer Shape1_30;

	ModelItemPhysicsGun(int defaultColor, int defaultLightValue) {
		setColor(defaultColor);
		setLightValue(defaultLightValue);

		this.textureWidth = 128;
		this.textureHeight = 128;
		this.Shape1 = new ModelRenderer(this, 0, 0);
		this.Shape1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1.addBox(-1.5F, -1.0F, -5.0F, 3, 2, 5, 0.0F);
		this.Shape1_11 = new ModelRenderer(this, 26, 0);
		this.Shape1_11.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_11.addBox(-0.5F, -3.0F, -6.0F, 0, 1, 7, 0.0F);
		this.setRotateAngle(Shape1_11, 0.0F, -0.0F, -0.6108652353286743F);
		this.Shape1_29 = new ModelRenderer(this, 0, 41);
		this.Shape1_29.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_29.addBox(-0.5F, -2.200000047683716F, 2.0F, 1, 2, 3, 0.0F);
		this.setRotateAngle(Shape1_29, -0.34906584024429316F, -0.0F, 0.0F);
		this.Shape1_6 = new ModelRenderer(this, 16, 0);
		this.Shape1_6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_6.addBox(-2.0F, -1.5F, -6.0F, 4, 3, 1, 0.0F);
		this.Shape1_8 = new ModelRenderer(this, 16, 9);
		this.Shape1_8.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_8.addBox(-1.5F, -1.0F, -6.5F, 3, 2, 1, 0.0F);
		this.Shape1_20 = new ModelRenderer(this, 26, 8);
		this.Shape1_20.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_20.addBox(1.5F, 1.5F, -9.0F, 1, 1, 4, 0.0F);
		this.Shape1_22 = new ModelRenderer(this, 26, 13);
		this.Shape1_22.setRotationPoint(0.0F, -3.0F, -8.5F);
		this.Shape1_22.addBox(-0.5F, -0.5F, -2.0F, 1, 1, 2, 0.0F);
		this.setRotateAngle(Shape1_22, 0.43633231520652765F, -0.0F, 0.0F);
		this.Shape1_13 = new ModelRenderer(this, 0, 32);
		this.Shape1_13.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_13.addBox(-0.5F, -2.0F, 4.5F, 1, 2, 3, 0.0F);
		this.setRotateAngle(Shape1_13, -0.34906584024429316F, -0.0F, 0.0F);
		this.Shape1_5 = new ModelRenderer(this, 0, 21);
		this.Shape1_5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_5.addBox(-1.2999999523162842F, 1.0F, 0.0F, 3, 1, 4, 0.0F);
		this.Shape1_4 = new ModelRenderer(this, 0, 21);
		this.Shape1_4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_4.addBox(-1.7000000476837158F, 1.0F, 0.0F, 3, 1, 4, 0.0F);
		this.Shape1_12 = new ModelRenderer(this, 26, 0);
		this.Shape1_12.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_12.addBox(0.5F, -3.0F, -6.0F, 0, 1, 7, 0.0F);
		this.setRotateAngle(Shape1_12, 0.0F, -0.0F, 0.6108652353286743F);
		this.Shape1_15 = new ModelRenderer(this, 16, 16);
		this.Shape1_15.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_15.addBox(1.5F, -0.5F, 1.0F, 2, 2, 3, 0.0F);
		this.Shape1_1 = new ModelRenderer(this, 0, 7);
		this.Shape1_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_1.addBox(-1.0F, -1.5F, -5.0F, 2, 3, 5, 0.0F);
		this.Shape1_24 = new ModelRenderer(this, 26, 13);
		this.Shape1_24.setRotationPoint(2.5F, 2.5F, -8.5F);
		this.Shape1_24.addBox(-0.5F, -0.5F, -2.0F, 1, 1, 2, 0.0F);
		this.setRotateAngle(Shape1_24, -0.43633231520652765F, 0.43633231520652765F, 0.0F);
		this.Shape1_14 = new ModelRenderer(this, 0, 37);
		this.Shape1_14.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_14.addBox(-0.30000001192092896F, 0.5F, 6.9666666984558105F, 1, 2, 2, 0.0F);
		this.Shape1_19 = new ModelRenderer(this, 26, 8);
		this.Shape1_19.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_19.addBox(-0.5F, -3.0F, -9.0F, 1, 1, 4, 0.0F);
		this.Shape1_23 = new ModelRenderer(this, 26, 13);
		this.Shape1_23.setRotationPoint(-2.5F, 2.5F, -8.5F);
		this.Shape1_23.addBox(-0.5F, -0.5F, -2.0F, 1, 1, 2, 0.0F);
		this.setRotateAngle(Shape1_23, -0.43633231520652765F, -0.43633231520652765F, 0.0F);
		this.Shape1_30 = new ModelRenderer(this, 26, 18);
		this.Shape1_30.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_30.addBox(-0.5F, -0.5F, -6.599999904632568F, 1, 1, 1, 0.0F);
		this.Shape1_25 = new ModelRenderer(this, 0, 37);
		this.Shape1_25.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_25.addBox(-0.699999988079071F, 0.5F, 6.9666666984558105F, 1, 2, 2, 0.0F);
		this.Shape1_3 = new ModelRenderer(this, 0, 26);
		this.Shape1_3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_3.addBox(-1.5F, -2.5F, 2.0F, 1, 3, 3, 0.0F);
		this.setRotateAngle(Shape1_3, -0.34906584024429316F, -0.0F, 0.0F);
		this.Shape1_10 = new ModelRenderer(this, 26, 0);
		this.Shape1_10.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_10.addBox(0.0F, -2.700000047683716F, -6.0F, 0, 1, 7, 0.0F);
		this.Shape1_21 = new ModelRenderer(this, 26, 8);
		this.Shape1_21.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_21.addBox(-2.5F, 1.5F, -9.0F, 1, 1, 4, 0.0F);
		this.Shape1_27 = new ModelRenderer(this, 0, 26);
		this.Shape1_27.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_27.addBox(0.5F, -2.5F, 2.0F, 1, 3, 3, 0.0F);
		this.setRotateAngle(Shape1_27, -0.34906584024429316F, -0.0F, 0.0F);
		this.Shape1_18 = new ModelRenderer(this, 16, 30);
		this.Shape1_18.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_18.addBox(4.0F, 0.0F, 2.0F, 2, 1, 1, 0.0F);
		this.Shape1_16 = new ModelRenderer(this, 16, 21);
		this.Shape1_16.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_16.addBox(1.5F, -1.0F, 1.5F, 2, 3, 2, 0.0F);
		this.Shape1_2 = new ModelRenderer(this, 0, 15);
		this.Shape1_2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_2.addBox(-2.0F, -2.0F, 0.0F, 4, 3, 3, 0.0F);
		this.Shape1_7 = new ModelRenderer(this, 16, 4);
		this.Shape1_7.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_7.addBox(-1.5F, -2.0F, -6.0F, 3, 4, 1, 0.0F);
		this.Shape1_17 = new ModelRenderer(this, 16, 26);
		this.Shape1_17.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_17.addBox(3.0F, -0.5F, 1.5F, 1, 2, 2, 0.0F);
		this.Shape1_9 = new ModelRenderer(this, 16, 12);
		this.Shape1_9.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_9.addBox(-1.0F, -1.5F, -6.5F, 2, 3, 1, 0.0F);
		this.Shape1_28 = new ModelRenderer(this, 0, 41);
		this.Shape1_28.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Shape1_28.addBox(-0.5F, -1.5F, 2.0F, 1, 2, 3, 0.0F);
		this.setRotateAngle(Shape1_28, -0.34906584024429316F, -0.0F, 0.0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		
		this.Shape1_29.render(f5);
		this.Shape1_6.render(f5);
		this.Shape1_8.render(f5);
		this.Shape1_20.render(f5);
		this.Shape1_22.render(f5);
		this.Shape1_13.render(f5);
		this.Shape1_5.render(f5);
		this.Shape1_4.render(f5);
		this.Shape1_15.render(f5);
		this.Shape1_24.render(f5);
		this.Shape1_14.render(f5);
		this.Shape1_19.render(f5);
		this.Shape1_23.render(f5);
		this.Shape1_30.render(f5);
		this.Shape1_25.render(f5);
		this.Shape1_3.render(f5);
		this.Shape1_21.render(f5);
		this.Shape1_27.render(f5);
		this.Shape1_18.render(f5);
		this.Shape1_16.render(f5);
		this.Shape1_2.render(f5);
		this.Shape1_7.render(f5);
		this.Shape1_17.render(f5);
		this.Shape1_9.render(f5);
		this.Shape1_28.render(f5);

		this.Shape1_11.render(f5);
		this.Shape1_12.render(f5);
		this.Shape1_10.render(f5);
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, 0);
		GlStateManager.color(r, g, b, a);
		this.Shape1_1.render(f5);
		this.Shape1.render(f5);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableLighting();
	}

	private float r, g, b, a;

	public void setColor(int value) {
		this.r = (float) (value >> 16 & 255) / 255.0F;
		this.g = (float) (value >> 8 & 255) / 255.0F;
		this.b = (float) (value & 255) / 255.0F;
		this.a = (float) (value >> 24 & 255) / 255.0F;
	}

	float lightX;

	public void setLightValue(int value) {
		this.lightX = (float) (value & 65535);
	}

	public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
