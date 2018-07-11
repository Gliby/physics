package gliby.minecraft.physics.client.render.items;

import javax.vecmath.Vector3f;

import gliby.minecraft.gman.client.render.RawItemRenderer;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.client.render.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

/**
 *
 */
public class RenderItemPhysicsGun extends RawItemRenderer {

	private ItemCameraTransforms transforms = new ItemCameraTransforms(
			new ItemTransformVec3f(new Vector3f(-90, -180, 0), new Vector3f(0.1f, 0.19f, -0.1F),
					new Vector3f(-1, 1, 1)),
			new ItemTransformVec3f(new Vector3f(27, -40, 8), new Vector3f(-0.33f, 0.14f, 0.25f),
					new Vector3f(-1, -1, -1)),
			new ItemTransformVec3f(new Vector3f(), new Vector3f(), new Vector3f()), new ItemTransformVec3f(
					new Vector3f(0, -90, 0), new Vector3f(-0.05f, 0, 0), new Vector3f(-1.5f, -1.5f, -1.5f)));

	private ModelRenderer bipedLeftArm;
	private RenderHandler renderHandler;

	/**
	 * @param renderMananger
	 * @param resourceLocation
	 */

	public RenderItemPhysicsGun(RenderHandler renderHandler, ModelResourceLocation resourceLocation) {
		super(resourceLocation);
		this.bipedLeftArm = new ModelRenderer(playerBiped, 40, 16);
		this.bipedLeftArm.mirror = true;
		this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0);
		this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + 0, 0.0F);
		this.renderHandler = renderHandler;
	}

	private ModelItemPhysicsGun model = new ModelItemPhysicsGun(0xFF87FFFF, 200);
	private ResourceLocation textureColorable = new ResourceLocation(Physics.MOD_ID,
			"textures/physics_gun_colorable.png");

	@Override
	public void render() {
		model.setColor(0xFF87FFFF);
		float scale = -0.0625f;
		float f1 = 40F - MathHelper.abs(
				MathHelper.sin((float) (Minecraft.getSystemTime() % 1000L) / 1000.0F * (float) Math.PI * 1.0F) * 40F);
		int lightValue = (int) MathHelper.clamp_float((200 + f1), 0, 250);
		if (owner != null) {
			if (transformType == TransformType.FIRST_PERSON) {
				GlStateManager.rotate(1.0f, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(12.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.translate(-0.7F, 0.8F, -0.2F);
				if (!owner.isInvisible()) {
					textureManager.bindTexture(((AbstractClientPlayer) mc.thePlayer).getLocationSkin());
					GlStateManager.pushMatrix();
					GlStateManager.translate(-0.19f, 0.05f, -0.55f);
					this.bipedLeftArm.rotateAngleY = 15 / (180F / (float) Math.PI);
					this.bipedLeftArm.rotateAngleX = 270 / (180F / (float) Math.PI);
					bipedLeftArm.render(scale * 0.75f);
					GlStateManager.popMatrix();
				}
			}

			if (renderHandler.getPhysicsGunColors().containsKey(owner.getUniqueID().toString())) {
				model.setColor(renderHandler.getPhysicsGunColors().get(owner.getUniqueID().toString()));
			}
		}

		textureManager.bindTexture(textureColorable);
		model.setLightValue(lightValue);
		model.render(null, 0, 0, 0, 0, 0, scale);

	}

	public ItemCameraTransforms getItemCameraTransforms() {
		return transforms;
	}
}
