//package gliby.minecraft.physics.client.render.entity;
//
//import com.bulletphysicsx.linearmath.Transform;
//import gliby.minecraft.physics.client.render.RenderHandler;
//import gliby.minecraft.physics.client.render.RenderUtilities;
//import gliby.minecraft.physics.common.entity.EntityPhysicsBase;
//import gliby.minecraft.physics.common.entity.EntityPhysicsModelPart;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.Tessellator;
//import net.minecraft.client.renderer.entity.RenderManager;
//import net.minecraft.entity.Entity;
//import org.lwjgl.BufferUtils;
//
//import javax.vecmath.Vector3f;
//import java.nio.FloatBuffer;
//
//import static org.lwjgl.opengl.GL11.*;
//
///**
// *
// */
//public class RenderPhysicsModelPart extends RenderPhysics {
//
//    private Transform entityTransform;
//    private Tessellator tessellator;
//    private FloatBuffer renderMatrix;
//
//    /**
//     * @param renderManager
//     */
//    public RenderPhysicsModelPart(RenderHandler renderHandler, RenderManager renderManager) {
//        super(renderHandler, renderManager);
//        entityTransform = new Transform();
//        tessellator = Tessellator.getInstance();
//        renderMatrix = BufferUtils.createFloatBuffer(16);
//    }
//
//    protected void draw(Entity castEntity, double entityX, double entityY, double entityZ, float partialTick, int color, boolean outline) {
//        EntityPhysicsModelPart entity = (EntityPhysicsModelPart) castEntity;
//        // Logic
//        Vector3f worldTranslation = RenderUtilities.getWorldTranslation(mc, partialTick);
//        entityTransform.setIdentity();
//        entityTransform.setRotation(entity.renderRotation);
//        entityTransform.origin.set(entity.renderPosition);
//        RenderUtilities.setBufferFromTransform(renderMatrix, entityTransform);
//
//        // Render
//        glPushMatrix();
//        // Apply world translation with bullet specific offset.
//        glTranslatef(-worldTranslation.x + 0.5f, -worldTranslation.y + 0.5f, -worldTranslation.z + 0.5f);
//        // Apply transformation.
//        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
//        glDisable(GL_TEXTURE_2D);
//
//        glMultMatrix(renderMatrix);
//        glScalef(2f * entity.renderExtent.x, 2f * entity.renderExtent.y, 2f * entity.renderExtent.z);
//        RenderUtilities.drawCube(1.0f, true);
//        glLineWidth(1.5f);
//        glColor4f(1.0f, 0.2f, 0.2f, 0.6f);
//        RenderUtilities.drawCube(1.0f, false);
//        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
//        glEnable(GL_TEXTURE_2D);
//        glPopMatrix();
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see
//     * net.gliby.physics.client.render.entity.RenderPhysics#getRenderHitPoint
//     * (net.gliby.physics.common.entity.EntityPhysicsBase, float)
//     */
//    @Override
//    public Vector3f getRenderHitPoint(EntityPhysicsBase entity, float partialTick) {
//        EntityPhysicsModelPart entityBlock = (EntityPhysicsModelPart) entity;
//        Vector3f worldTranslation = RenderUtilities.getWorldTranslation(Minecraft.getMinecraft(), partialTick);
//        Vector3f hitPoint = new Vector3f(entityBlock.renderPosition);
//        hitPoint.add(entity.pickLocalHit);
//        hitPoint.add(new Vector3f(0.5f, 0.5f, 0.5f));
//        hitPoint.sub(worldTranslation);
//        return hitPoint;
//    }
//}