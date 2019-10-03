package gliby.minecraft.physics.client.render.entity;

import com.bulletphysicsx.linearmath.Transform;
import gliby.minecraft.physics.client.render.RenderHandler;
import gliby.minecraft.physics.client.render.ConversionUtility;
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

    private Transform tempTransform = new Transform();
    private static FloatBuffer renderMatrix = BufferUtils.createFloatBuffer(16);

    /**
     * @param renderManager
     */
    public RenderPhysicsBlock(RenderHandler renderHandler, RenderManager renderManager) {
        super(renderHandler, renderManager);
    }

    protected void draw(Entity castEntity, double entityX, double entityY, double entityZ, float partialTick,
                        int color, boolean outline) {
        EntityPhysicsBlock entity = (EntityPhysicsBlock) castEntity;
        //
        IBlockState state = entity.getBlockState();
        if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
            Vector3f worldTranslation = ConversionUtility.getWorldTranslation(mc, partialTick);
            BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
            IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(state);

            // setup transform;
            tempTransform.setIdentity();
            tempTransform.setRotation(entity.renderRotation);
            tempTransform.origin.set(entity.renderPosition);
            ConversionUtility.setBufferFromTransform(renderMatrix, tempTransform);

            // start drawing
            GlStateManager.pushMatrix();
            World world = castEntity.getEntityWorld();
            // Apply world translation with bullet pivot offset.
            GlStateManager.translate(-worldTranslation.x + 0.5f, -worldTranslation.y + 0.5f, -worldTranslation.z + 0.5f);
            // Apply transformation.
            glMultMatrix(renderMatrix);

            // fix pivot.
            GlStateManager.translate(-(0.5f), -(0.5f), -(0.5f));

            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

//            float red = (float) (entity.getTintIndex() >> 16 & 255) / 255.0F;
//            float green = (float) (entity.getTintIndex() >> 8 & 255) / 255.0F;
//            float blue = (float) (entity.getTintIndex() & 255) / 255.0F;
            blockModelRenderer.renderModelNoLightmap(entity.world, state, entity.getPosition(), ibakedmodel);

//            bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
//            blockModelRenderer.renderModel(bufferbuilder, world, ibakedmodel, state, entity.getPosition(), entity.getBrightnessForRender());
//            tessellator.draw();

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
        Vector3f worldTranslation = ConversionUtility.getWorldTranslation(Minecraft.getMinecraft(), partialTick);
        Vector3f hitPoint = new Vector3f(entityBlock.renderPosition);
        hitPoint.add(entity.pickLocalHit);
        hitPoint.add(new Vector3f(0.5f, 0.5f, 0.5f));
        hitPoint.sub(worldTranslation);
        return hitPoint;
    }
}