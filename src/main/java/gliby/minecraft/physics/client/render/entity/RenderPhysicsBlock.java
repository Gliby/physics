package gliby.minecraft.physics.client.render.entity;

import static org.lwjgl.opengl.GL11.glMultMatrix;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import gliby.minecraft.physics.client.render.RenderHandler;
import gliby.minecraft.physics.client.render.RenderUtilities;
import gliby.minecraft.physics.client.render.blocks.CustomModelRenderer;
import gliby.minecraft.physics.common.entity.EntityPhysicsBase;
import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;

/**
 *
 */
public class RenderPhysicsBlock extends RenderPhysics {

	/**
	 * @param renderManager
	 */
	public RenderPhysicsBlock(RenderHandler renderHandler, RenderManager renderManager) {
		super(renderHandler, renderManager);
	}

	private static Matrix4 entityTransform = new Matrix4();
	private static CustomModelRenderer rotatableBlockModelRenderer = new CustomModelRenderer();
	private static Tessellator tessellator = Tessellator.getInstance();
	private static WorldRenderer worldrenderer = tessellator.getWorldRenderer();
	private static Minecraft mc = Minecraft.getMinecraft();
	private static FloatBuffer renderMatrix = BufferUtils.createFloatBuffer(16);

	protected void draw(Entity castEntity, double entityX, double entityY, double entityZ, float partialTick,
			int color, boolean outline) {
		EntityPhysicsBlock entity = (EntityPhysicsBlock) castEntity;
		// Logic
		IBlockState state = entity.getBlockState();
		Block block = state.getBlock();
		Vector3 worldTranslation = RenderUtilities.getWorldTranslation(mc, partialTick);
		BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
		IBakedModel ibakedmodel = blockrendererdispatcher.getModelFromBlockState(state, entity.getEntityWorld(),
				(BlockPos) null);
		entityTransform.idt();
		entityTransform.set(entity.renderPosition, entity.renderRotation);
		RenderUtilities.setBufferFromTransform(renderMatrix, entityTransform);
		// Render
		GlStateManager.pushMatrix();
		// Apply world translation with bullet specific offset.
		GlStateManager.translate(-worldTranslation.x + 0.5f, -worldTranslation.y + 0.5f, -worldTranslation.z + 0.5f);
		// Apply transformation.
		glMultMatrix(renderMatrix);
		bindTexture(TextureMap.locationBlocksTexture);
		// GlStateManager.translate(-(0.5f + offset.getX()), -(0.5f +
		// offset.getY()), -(0.5f + offset.getZ()));
		// Correct block rotation.
		// GlStateManager.rotate(180, 0, 1, 0);
		// GlStateManager.rotate(180, 0, 0, 1);
		GlStateManager.translate(-(0.5f), -(0.5f), -(0.5f));
		if (block.getRenderType() == 3) {
			float red = (float) (color >> 16 & 255) / 255.0F;
			float green = (float) (color >> 8 & 255) / 255.0F;
			float blue = (float) (color & 255) / 255.0F;
			rotatableBlockModelRenderer.renderModel(entity.worldObj, state, entity.getPosition(), ibakedmodel,
					tessellator, worldrenderer, entity.getTintIndex(), red, green, blue, !outline);
		}
		GlStateManager.popMatrix();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.gliby.physics.client.render.entity.RenderPhysics#getWorldHitPoint()
	 */
	@Override
	public Vector3 getRenderHitPoint(EntityPhysicsBase entity, float partialTick) {
		EntityPhysicsBlock entityBlock = (EntityPhysicsBlock) entity;
		Vector3 worldTranslation = RenderUtilities.getWorldTranslation(Minecraft.getMinecraft(), partialTick);
		Vector3 hitPoint = new Vector3(entityBlock.renderPosition);
		hitPoint.add(entity.pickLocalHit);
		hitPoint.add(new Vector3(0.5f, 0.5f, 0.5f));
		hitPoint.sub(worldTranslation);
		return hitPoint;
	}
}