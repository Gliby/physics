/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.client.render.entity;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import net.gliby.physics.client.render.RenderHandler;
import net.gliby.physics.client.render.RenderUtilities;
import net.gliby.physics.common.entity.EntityPhysicsBase;
import net.gliby.physics.common.entity.EntityPhysicsBlock;
import net.gliby.physics.common.game.items.ItemPhysicsGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

//TODO fix outline color
public abstract class RenderPhysics extends Render {

	private final int DEFAULT_PHYSICS_COLOR = 0xFF87FFFF;

	public abstract Vector3f getRenderHitPoint(EntityPhysicsBase entity, float partialTick);

	protected Minecraft mc;
	private RenderHandler renderHandler;

	/**
	 * @param renderManager
	 */
	protected RenderPhysics(RenderHandler renderHandler, RenderManager renderManager) {
		super(renderManager);
		this.mc = Minecraft.getMinecraft();
		this.renderHandler = renderHandler;
	}

	@Override
	protected final ResourceLocation getEntityTexture(Entity parEntity) {
		return getEntityTexture((EntityPhysicsBlock) parEntity);
	}

	@Override
	public final boolean shouldRender(Entity entity, ICamera camera, double camX, double camY, double camZ) {
		EntityPhysicsBase interpolatableEntity = (EntityPhysicsBase) entity;
		interpolatableEntity.interpolate();
		return interpolatableEntity.pickerEntity != null
				|| entity.isInRangeToRender3d(camX, camY, camZ) && (entity.ignoreFrustumCheck
						|| camera.isBoundingBoxInFrustum(interpolatableEntity.getRenderBoundingBox()));
	}

	protected abstract void draw(Entity uncast, double entityX, double entityY, double entityZ, float partialTick,
			int color, boolean outline);

	// TODO Outline color bugs.

	public void doRender(Entity uncast, double entityX, double entityY, double entityZ, float twen, float partialTick) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		EntityPhysicsBase entity = (EntityPhysicsBase) uncast;
		drawBeam: if (entity.pickerEntity != null) {
			Item item = entity.pickerEntity.getHeldItem() != null ? entity.pickerEntity.getHeldItem().getItem() : null;
			if (item instanceof ItemPhysicsGun) {
				Vector3f hitPoint = getRenderHitPoint(entity, partialTick);
				Vec3 firstPersonOffset = new Vec3(-0.22D, -0.08D, 0.35D);
				firstPersonOffset = firstPersonOffset.rotatePitch(-(entity.pickerEntity.prevRotationPitch
						+ (entity.pickerEntity.rotationPitch - entity.pickerEntity.prevRotationPitch) * partialTick)
						* (float) Math.PI / 180.0F);
				firstPersonOffset = firstPersonOffset.rotateYaw(-(entity.pickerEntity.prevRotationYaw
						+ (entity.pickerEntity.rotationYaw - entity.pickerEntity.prevRotationYaw) * partialTick)
						* (float) Math.PI / 180.0F);

				double d3 = entity.pickerEntity.prevPosX
						+ (entity.pickerEntity.posX - entity.pickerEntity.prevPosX) * (double) partialTick
						+ firstPersonOffset.xCoord;
				double d4 = entity.pickerEntity.prevPosY
						+ (entity.pickerEntity.posY - entity.pickerEntity.prevPosY) * (double) partialTick
						+ firstPersonOffset.yCoord;
				double d5 = entity.pickerEntity.prevPosZ
						+ (entity.pickerEntity.posZ - entity.pickerEntity.prevPosZ) * (double) partialTick
						+ firstPersonOffset.zCoord;

				if (this.renderManager.options.thirdPersonView != 0 || entity.pickerEntity != mc.thePlayer) {
					Vec3 beamStart = RenderUtilities.calculateRay(entity.pickerEntity, 1.0f, partialTick,
							new Vector3f(-0.1f, -0.25F, 0));
					d3 = beamStart.xCoord;
					d4 = beamStart.yCoord;
					d5 = beamStart.zCoord;
					GL11.glLineWidth(2.0f);
				} else {
					if (!mc.isGuiEnabled()) {
						break drawBeam;
					}
					GL11.glLineWidth(10.0f);
				}
				double d6 = (double) entity.pickerEntity.getEyeHeight();

				double d16 = (entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTick) + hitPoint.x
						- entityX;
				double d8 = (entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTick + 0.25D)
						+ hitPoint.y - entityY;
				double d10 = (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTick) + hitPoint.z
						- entityZ;
				double d12 = (double) ((float) (d3 - d16));
				double d14 = (double) ((float) (d4 - d8)) + d6;
				double d15 = (double) ((float) (d5 - d10));
				GlStateManager.disableTexture2D();
				GlStateManager.disableLighting();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);
				worldRenderer.startDrawing(3);
				worldRenderer.setColorOpaque_I(getBeamColor(entity.pickerEntity));
				byte b2 = 16;
				for (int i = 0; i <= b2; ++i) {
					float f12 = (float) i / (float) b2;
					worldRenderer.addVertex(hitPoint.x + d12 * (double) f12,
							hitPoint.y + (d14 + 0.0f) * (double) (f12 * f12 + f12) * 0.5D + 0.25D,
							hitPoint.z + d15 * (double) f12);
				}
				tessellator.draw();

				// Outline
				GL11.glLineWidth(3);
				GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				draw(uncast, entityX, entityY, entityZ, partialTick, getBeamColor(entity.pickerEntity), true);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glLineWidth(3);
				GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				
				// Entity lighting
				int brightness = entity.getBrightnessForRender(partialTick);
				int lightX = brightness % 65536;
				int lightY = brightness / 65536;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lightX / 1.0F,
						(float) lightY / 1.0F);

				GlStateManager.enableLighting();
				GlStateManager.enableTexture2D();
				GL11.glLineWidth(1.0f);
			}
		}
		draw(uncast, entityX, entityY, entityZ, partialTick, -1, false);

	}

	// TODO What if beam color is actually -1 ?
	private int beamColor = -1;

	public int getBeamColor(Entity pickerEntity) {
		if (beamColor == -1) {
			String UUID;
			if (renderHandler.getPhysicsGunColors().containsKey(UUID = pickerEntity.getUniqueID().toString())) {
				beamColor = renderHandler.getPhysicsGunColors().get(UUID);
			} else
				beamColor = DEFAULT_PHYSICS_COLOR;
		}
		return beamColor;
	}
}
