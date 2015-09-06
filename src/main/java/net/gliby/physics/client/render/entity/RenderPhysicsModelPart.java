/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.client.render.entity;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glMultMatrix;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import net.gliby.physics.client.render.RenderUtilities;
import net.gliby.physics.client.render.blocks.CustomModelRenderer;
import net.gliby.physics.common.entity.EntityPhysicsBase;
import net.gliby.physics.common.entity.EntityPhysicsBlock;
import net.gliby.physics.common.entity.EntityPhysicsModelPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

import org.lwjgl.BufferUtils;

import com.bulletphysics.linearmath.Transform;

/**
 *
 */
public class RenderPhysicsModelPart extends RenderPhysics {

	/**
	 * @param renderManager
	 */
	public RenderPhysicsModelPart(RenderManager renderManager) {
		super(renderManager);
	}

	private static Transform entityTransform = new Transform();
	private static CustomModelRenderer rotatableBlockModelRenderer = new CustomModelRenderer();
	private static Tessellator tessellator = Tessellator.getInstance();
	private static WorldRenderer worldrenderer = tessellator.getWorldRenderer();
	private static Minecraft mc = Minecraft.getMinecraft();
	private static FloatBuffer renderMatrix = BufferUtils.createFloatBuffer(16);

	protected void draw(Entity castEntity, double entityX, double entityY, double entityZ, float partialTick) {
		EntityPhysicsModelPart entity = (EntityPhysicsModelPart) castEntity;
		// Logic
		Vector3f worldTranslation = RenderUtilities.getWorldTranslation(mc, partialTick);
		entityTransform.setIdentity();
		entityTransform.setRotation(entity.renderRotation);
		entityTransform.origin.set(entity.renderPosition);
		RenderUtilities.setBufferFromTransform(renderMatrix, entityTransform);

		// Render
		glPushMatrix();
		// Apply world translation with bullet specific offset.
		glTranslatef(-worldTranslation.x + 0.5f, -worldTranslation.y + 0.5f, -worldTranslation.z + 0.5f);
		// Apply transformation.
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		glDisable(GL_TEXTURE_2D);

		glMultMatrix(renderMatrix);
		glScalef(2f * entity.renderExtent.x, 2f * entity.renderExtent.y, 2f * entity.renderExtent.z);
		RenderUtilities.drawCube(1.0f, true);
		glLineWidth(1.5f);
		glColor4f(1.0f, 0.2f, 0.2f, 0.6f);
		RenderUtilities.drawCube(1.0f, false);
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		glEnable(GL_TEXTURE_2D);
		glPopMatrix();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.gliby.physics.client.render.entity.RenderPhysics#getRenderHitPoint
	 * (net.gliby.physics.common.entity.EntityPhysicsBase, float)
	 */
	@Override
	public Vector3f getRenderHitPoint(EntityPhysicsBase entity, float partialTick) {
		EntityPhysicsModelPart entityBlock = (EntityPhysicsModelPart) entity;
		Vector3f worldTranslation = RenderUtilities.getWorldTranslation(Minecraft.getMinecraft(), partialTick);
		Vector3f hitPoint = new Vector3f(entityBlock.renderPosition);
		hitPoint.add(entity.pickLocalHit);
		hitPoint.add(new Vector3f(0.5f, 0.5f, 0.5f));
		hitPoint.sub(worldTranslation);
		return hitPoint;
	}
}