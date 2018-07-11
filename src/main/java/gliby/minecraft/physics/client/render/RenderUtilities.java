package gliby.minecraft.physics.client.render;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.glPolygonMode;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import com.bulletphysicsx.linearmath.Transform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

/**
 *
 */
public class RenderUtilities {

	/**
	 * Calculates player to world transform and returns accordingly.
	 * 
	 * @param mc
	 * @param delta
	 * @returns javax.vecmath.Vector3f
	 */
	public static Vector3f getWorldTranslation(Minecraft mc, float delta) {
		return new Vector3f((float) mc.thePlayer.prevPosX + (float) (mc.thePlayer.posX - mc.thePlayer.prevPosX) * (float) delta, (float) mc.thePlayer.prevPosY + (float) (mc.thePlayer.posY - (float) mc.thePlayer.prevPosY) * (float) delta, (float) mc.thePlayer.prevPosZ + (float) (mc.thePlayer.posZ - (float) mc.thePlayer.prevPosZ) * (float) delta);
	}

	public static Vector3f getSmoothedEntityPosition(Entity entity, float delta) {
		return new Vector3f((float) entity.prevPosX + (float) (entity.posX - entity.prevPosX) * (float) delta, (float) entity.prevPosY + (float) (entity.posY - (float) entity.prevPosY) * (float) delta, (float) entity.prevPosZ + (float) (entity.posZ - (float) entity.prevPosZ) * (float) delta);
	}

	public static Vector3f toVector3f(Vec3 vec3) {
		return new Vector3f((float) vec3.xCoord, (float) vec3.yCoord, (float) vec3.zCoord);
	}
	
	public static Vec3 toVec3(Vector3f vec3) {
		return new Vec3((float) vec3.x, (float) vec3.y, (float) vec3.z);
	}

	private static float[] buffer = new float[16];

	public static FloatBuffer setBufferFromTransform(FloatBuffer matrixBuffer, Transform transform) {
		transform.getOpenGLMatrix(buffer);
		matrixBuffer.clear();
		matrixBuffer.put(buffer);
		matrixBuffer.flip();
		return matrixBuffer;
	}

	
	public static Vec3 calculateRay(Entity base, float distance, float partialTick, Vector3f offset) {
		Vec3 vec3 = toVec3(getSmoothedEntityPosition(base, partialTick)).add(toVec3(offset));
		Vec3 vec31 = base.getLook(partialTick);
		Vec3 vec32 = vec3.addVector(vec31.xCoord * distance, vec31.yCoord * distance, vec31.zCoord * distance);
		return new Vec3((float) vec32.xCoord, (float) vec32.yCoord, (float) vec32.zCoord);
	}

	public static void drawCube(float extent, boolean fill) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		extent = extent * 0.5f;
		if (!fill) {
			glPolygonMode(GL_FRONT, GL_LINE);
			glPolygonMode(GL_BACK, GL_LINE);
		}
		worldRenderer.startDrawingQuads();
		worldRenderer.setNormal(1f, 0f, 0f);
		worldRenderer.addVertex(+extent, -extent, +extent);
		worldRenderer.addVertex(+extent, -extent, -extent);
		worldRenderer.addVertex(+extent, +extent, -extent);
		worldRenderer.addVertex(+extent, +extent, +extent);
		worldRenderer.setNormal(0f, 1f, 0f);
		worldRenderer.addVertex(+extent, +extent, +extent);
		worldRenderer.addVertex(+extent, +extent, -extent);
		worldRenderer.addVertex(-extent, +extent, -extent);
		worldRenderer.addVertex(-extent, +extent, +extent);
		worldRenderer.setNormal(0f, 0f, 1f);
		worldRenderer.addVertex(+extent, +extent, +extent);
		worldRenderer.addVertex(-extent, +extent, +extent);
		worldRenderer.addVertex(-extent, -extent, +extent);
		worldRenderer.addVertex(+extent, -extent, +extent);
		worldRenderer.setNormal(-1f, 0f, 0f);
		worldRenderer.addVertex(-extent, -extent, +extent);
		worldRenderer.addVertex(-extent, +extent, +extent);
		worldRenderer.addVertex(-extent, +extent, -extent);
		worldRenderer.addVertex(-extent, -extent, -extent);
		worldRenderer.setNormal(0f, -1f, 0f);
		worldRenderer.addVertex(-extent, -extent, +extent);
		worldRenderer.addVertex(-extent, -extent, -extent);
		worldRenderer.addVertex(+extent, -extent, -extent);
		worldRenderer.addVertex(+extent, -extent, +extent);
		worldRenderer.setNormal(0f, 0f, -1f);
		worldRenderer.addVertex(-extent, -extent, -extent);
		worldRenderer.addVertex(-extent, +extent, -extent);
		worldRenderer.addVertex(+extent, +extent, -extent);
		worldRenderer.addVertex(+extent, -extent, -extent);
		tessellator.draw();
		if (!fill) {
			glPolygonMode(GL_FRONT, GL_FILL);
			glPolygonMode(GL_BACK, GL_FILL);
		}
	}
}
