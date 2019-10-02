package gliby.minecraft.physics.client.render;

import com.bulletphysicsx.linearmath.Transform;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;

/**
 *
 */
public class RenderUtilities {

    private static float[] buffer = new float[16];

    /**
     * Calculates player to world transform and returns accordingly.
     *
     * @param mc
     * @param delta
     * @returns javax.vecmath.Vector3f
     */
    public static Vector3f getWorldTranslation(Minecraft mc, float delta) {
        return new Vector3f((float) mc.player.prevPosX + (float) (mc.player.posX - mc.player.prevPosX) * delta, (float) mc.player.prevPosY + (float) (mc.player.posY - (float) mc.player.prevPosY) * delta, (float) mc.player.prevPosZ + (float) (mc.player.posZ - (float) mc.player.prevPosZ) * delta);
    }

    public static Vector3f getSmoothedEntityPosition(Entity entity, float delta) {
        return new Vector3f((float) entity.prevPosX + (float) (entity.posX - entity.prevPosX) * delta, (float) entity.prevPosY + (float) (entity.posY - (float) entity.prevPosY) * delta, (float) entity.prevPosZ + (float) (entity.posZ - (float) entity.prevPosZ) * delta);
    }

    public static Vector3f toVector3f(Vec3d vec3) {
        return new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z);
    }

    public static Vec3d toVec3(Vector3f vec3) {
        return new Vec3d(vec3.x, vec3.y, vec3.z);
    }

    public static FloatBuffer setBufferFromTransform(FloatBuffer matrixBuffer, Transform transform) {
        transform.getOpenGLMatrix(buffer);
        matrixBuffer.clear();
        matrixBuffer.put(buffer);
        matrixBuffer.flip();
        return matrixBuffer;
    }


    public static Vec3d calculateRay(Entity base, float distance, float partialTick, Vector3f offset) {
        Vec3d vec3 = toVec3(getSmoothedEntityPosition(base, partialTick)).add(toVec3(offset));
        Vec3d vec31 = base.getLook(partialTick);
        Vec3d vec32 = vec3.addVector(vec31.x * distance, vec31.y * distance, vec31.z * distance);
        return new Vec3d((float) vec32.x, (float) vec32.y, (float) vec32.z);
    }

    public static void drawCube(float extent, boolean fill) {
//        Tessellator tessellator = Tessellator.getInstance();
//        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
//        extent = extent * 0.5f;
//        if (!fill) {
//            glPolygonMode(GL_FRONT, GL_LINE);
//            glPolygonMode(GL_BACK, GL_LINE);
//        }
//        worldRenderer.startDrawingQuads();
//        worldRenderer.setNormal(1f, 0f, 0f);
//        worldRenderer.addVertex(+extent, -extent, +extent);
//        worldRenderer.addVertex(+extent, -extent, -extent);
//        worldRenderer.addVertex(+extent, +extent, -extent);
//        worldRenderer.addVertex(+extent, +extent, +extent);
//        worldRenderer.setNormal(0f, 1f, 0f);
//        worldRenderer.addVertex(+extent, +extent, +extent);
//        worldRenderer.addVertex(+extent, +extent, -extent);
//        worldRenderer.addVertex(-extent, +extent, -extent);
//        worldRenderer.addVertex(-extent, +extent, +extent);
//        worldRenderer.setNormal(0f, 0f, 1f);
//        worldRenderer.addVertex(+extent, +extent, +extent);
//        worldRenderer.addVertex(-extent, +extent, +extent);
//        worldRenderer.addVertex(-extent, -extent, +extent);
//        worldRenderer.addVertex(+extent, -extent, +extent);
//        worldRenderer.setNormal(-1f, 0f, 0f);
//        worldRenderer.addVertex(-extent, -extent, +extent);
//        worldRenderer.addVertex(-extent, +extent, +extent);
//        worldRenderer.addVertex(-extent, +extent, -extent);
//        worldRenderer.addVertex(-extent, -extent, -extent);
//        worldRenderer.setNormal(0f, -1f, 0f);
//        worldRenderer.addVertex(-extent, -extent, +extent);
//        worldRenderer.addVertex(-extent, -extent, -extent);
//        worldRenderer.addVertex(+extent, -extent, -extent);
//        worldRenderer.addVertex(+extent, -extent, +extent);
//        worldRenderer.setNormal(0f, 0f, -1f);
//        worldRenderer.addVertex(-extent, -extent, -extent);
//        worldRenderer.addVertex(-extent, +extent, -extent);
//        worldRenderer.addVertex(+extent, +extent, -extent);
//        worldRenderer.addVertex(+extent, -extent, -extent);
//        tessellator.draw();
//        if (!fill) {
//            glPolygonMode(GL_FRONT, GL_FILL);
//            glPolygonMode(GL_BACK, GL_FILL);
//        }
    }
}
