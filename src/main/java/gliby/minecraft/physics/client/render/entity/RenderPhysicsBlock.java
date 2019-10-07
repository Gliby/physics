package gliby.minecraft.physics.client.render.entity;

import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.client.render.RenderHandler;
import gliby.minecraft.physics.client.render.VecUtility;
import gliby.minecraft.physics.common.entity.EntityPhysicsBase;
import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;

import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.glMultMatrix;

/**
 *
 */
public class RenderPhysicsBlock extends RenderPhysics {


    private static FloatBuffer renderMatrix = BufferUtils.createFloatBuffer(16);

    /**
     * @param renderManager
     */
    public RenderPhysicsBlock(RenderHandler renderHandler, RenderManager renderManager) {
        super(renderHandler, renderManager);
    }

    protected static Transform transform = new Transform();

    protected void draw(Entity castEntity, double entityX, double entityY, double entityZ, float deltaTime,
                        int color, boolean outline) {

        EntityPhysicsBlock entity = (EntityPhysicsBlock) castEntity;
        IBlockState state = entity.getBlockState();
        if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
            Vector3f worldTranslation = VecUtility.getWorldTranslation(mc, deltaTime);
            BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
            IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(state);


            // start drawing
            GlStateManager.pushMatrix();
            World world = castEntity.getEntityWorld();
            // Apply world translation with bullet pivot offset.
            GlStateManager.translate(-worldTranslation.x + 0.5f, -worldTranslation.y + 0.5f, -worldTranslation.z + 0.5f);

            // render debug direction
//            GlStateManager.pushMatrix();
//            transform.setIdentity();
//            transform.origin.set(entity.renderPosition);
//            VecUtility.setBufferFromTransform(renderMatrix, transform);
//            glMultMatrix(renderMatrix);
////            GlStateManager.translate(-(0.5f), -(0.5f), -(0.5f));
//            Vector3f direction = entity.getDirection();
//            direction.scale(500);
//            Tessellator tessellator = Tessellator.getInstance();
//            BufferBuilder builder = tessellator.getBuffer();
//            builder.begin(GL_LINES, DefaultVertexFormats.POSITION);
//            builder.pos(0, 0,0).endVertex();
//            builder.pos(direction.x, direction.y, direction.z).endVertex();
//            tessellator.draw();
//
//            GlStateManager.popMatrix();


            // setup transform;
            transform.setIdentity();
            transform.setRotation(entity.getRenderRotation());
            transform.origin.set(entity.getRenderPosition());
            VecUtility.setBufferFromTransform(renderMatrix, transform);

            // Apply transformation.
            glMultMatrix(renderMatrix);

            // fix pivot.
            GlStateManager.translate(-(0.5f), -(0.5f), -(0.5f));

            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            blockModelRenderer.renderModelNoLightmap(entity.world, state, entity.getPosition(), ibakedmodel, !outline, VecUtility.fromColor(color));

            GlStateManager.popMatrix();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.gliby.physics.client.render.entity.RenderPhysics#getWorldHitPoint()
     */
    @Override
    public Vector3f getRenderHitPoint(EntityPhysicsBase entity, float partialTick) {
        EntityPhysicsBlock entityBlock = (EntityPhysicsBlock) entity;
        Vector3f worldTranslation = VecUtility.getWorldTranslation(Minecraft.getMinecraft(), partialTick);
        Vector3f hitPoint = new Vector3f(entityBlock.getRenderPosition());
        hitPoint.add(entity.getPickLocalHit());
        hitPoint.add(new Vector3f(0.5f, 0.5f, 0.5f));
        hitPoint.sub(worldTranslation);
        return hitPoint;
    }
}