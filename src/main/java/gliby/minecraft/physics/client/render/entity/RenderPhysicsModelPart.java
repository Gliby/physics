package gliby.minecraft.physics.client.render.entity;

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


import org.lwjgl.BufferUtils;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import gliby.minecraft.physics.client.render.RenderHandler;
import gliby.minecraft.physics.client.render.RenderUtilities;
import gliby.minecraft.physics.client.render.blocks.CustomModelRenderer;
import gliby.minecraft.physics.common.entity.EntityPhysicsBase;
import gliby.minecraft.physics.common.entity.EntityPhysicsModelPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

/**
 *
 */
public class RenderPhysicsModelPart extends RenderPhysics {

	/**
	 * @param renderManager
	 */
	public RenderPhysicsModelPart(RenderHandler renderHandler, RenderManager renderManager) {
		super(renderHandler, renderManager);
		entityTransform = new Matrix4();
		rotatableBlockModelRenderer = new CustomModelRenderer();
		tessellator = Tessellator.getInstance();
		worldRenderer = tessellator.getWorldRenderer();
		renderMatrix = BufferUtils.createFloatBuffer(16);
	}

	private Matrix4 entityTransform;
	private CustomModelRenderer rotatableBlockModelRenderer;
	private Tessellator tessellator;
	private WorldRenderer worldRenderer;
	private FloatBuffer renderMatrix;

	protected void draw(Entity castEntity, double entityX, double entityY, double entityZ, float partialTick, int color, boolean outline) {
		EntityPhysicsModelPart entity = (EntityPhysicsModelPart) castEntity;
		// Logic
		Vector3 worldTranslation = RenderUtilities.getWorldTranslation(mc, partialTick);
		entityTransform.idt();
		entityTransform.set(entity.renderPosition, entity.renderRotation);
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
	public Vector3 getRenderHitPoint(EntityPhysicsBase entity, float partialTick) {
		EntityPhysicsModelPart entityBlock = (EntityPhysicsModelPart) entity;
		Vector3 worldTranslation = RenderUtilities.getWorldTranslation(Minecraft.getMinecraft(), partialTick);
		Vector3 hitPoint = new Vector3(entityBlock.renderPosition);
		hitPoint.add(entity.pickLocalHit);
		hitPoint.add(new Vector3(0.5f, 0.5f, 0.5f));
		hitPoint.sub(worldTranslation);
		return hitPoint;
	}
}