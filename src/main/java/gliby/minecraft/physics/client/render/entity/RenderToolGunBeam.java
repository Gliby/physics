package gliby.minecraft.physics.client.render.entity;

import gliby.minecraft.physics.client.render.RenderHandler;
import gliby.minecraft.physics.client.render.VecUtility;
import gliby.minecraft.physics.common.entity.EntityToolGunBeam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

/**
 *
 */
public class RenderToolGunBeam extends Render {

    protected Minecraft mc;

    public RenderToolGunBeam(RenderHandler renderHandler, RenderManager renderManager) {
        super(renderManager);
        this.mc = Minecraft.getMinecraft();
    }

    public void doRender(Entity uncast, double entityX, double entityY, double entityZ, float yaw, float deltaTime) {
        EntityToolGunBeam entity = (EntityToolGunBeam) uncast;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        Vector3f worldTranslation = VecUtility.getWorldTranslation(mc, deltaTime);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, 1);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-worldTranslation.x, -worldTranslation.y, -worldTranslation.z);
//        renderOffsetAABB(new AxisAlignedBB(100, 100, 100, 100, 100, 100), entity.clientOrigin.x, entity.clientOrigin.y, entity.clientOrigin.z);
        float alpha = (entity.msUntilGone - MathHelper.clamp((System.currentTimeMillis() - entity.timeCreated), 0, entity.msUntilGone)) / entity.msUntilGone;
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        if (entity.clientOrigin != null && renderManager.options.thirdPersonView == 0) {
            bufferBuilder.pos(entity.clientOrigin.x, entity.clientOrigin.y, entity.clientOrigin.z).color(1, 1, 1, alpha).endVertex();
        } else {
            bufferBuilder.pos(entity.worldOrigin.x, entity.worldOrigin.y, entity.worldOrigin.z).color(1, 1, 1, alpha).endVertex();
        }
        bufferBuilder.pos(entity.hit.x, entity.hit.y, entity.hit.z).color(1, 1, 1, alpha).endVertex();
        tessellator.draw();

        //Render lighting

        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }
}
