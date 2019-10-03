package gliby.minecraft.physics.client.render.entity;

import gliby.minecraft.gman.client.render.GBlockModelRenderer;
import gliby.minecraft.physics.client.render.RenderHandler;
import gliby.minecraft.physics.client.render.VecUtility;
import gliby.minecraft.physics.common.entity.EntityPhysicsBase;
import gliby.minecraft.physics.common.game.items.ItemPhysicsGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;

public abstract class RenderPhysics extends Render {

    protected static Minecraft mc;
    private final int DEFAULT_PHYSICS_COLOR = 0xFF87FFFF;
    private RenderHandler renderHandler;

    private int beamColor = -1;

    protected static final GBlockModelRenderer blockModelRenderer = new GBlockModelRenderer(Minecraft.getMinecraft().getBlockColors());

    /**
     * @param renderManager
     */
    protected RenderPhysics(RenderHandler renderHandler, RenderManager renderManager) {
        super(renderManager);
        this.renderHandler = renderHandler;
        this.mc = Minecraft.getMinecraft();
    }

    public abstract Vector3f getRenderHitPoint(EntityPhysicsBase entity, float partialTick);

    @Override
    protected final ResourceLocation getEntityTexture(Entity parEntity) {
        return getEntityTexture(parEntity);
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

    public void doRender(Entity uncast, double entityX, double entityY, double entityZ, float twen, float partialTick) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        EntityPhysicsBase entity = (EntityPhysicsBase) uncast;
        drawBeam:
        if (entity.pickerEntity != null) {
            Item item = entity.pickerEntity.getHeldItemMainhand() != null ? entity.pickerEntity.getHeldItemMainhand().getItem() : null;
            if (item instanceof ItemPhysicsGun) {


                Vector3f hitPoint = getRenderHitPoint(entity, partialTick);
                Vec3d firstPersonOffset = new Vec3d(-0.22D, -0.08D, 0.35D);

                firstPersonOffset = firstPersonOffset.rotatePitch(-(entity.pickerEntity.prevRotationPitch
                        + (entity.pickerEntity.rotationPitch - entity.pickerEntity.prevRotationPitch) * partialTick)
                        * (float) Math.PI / 180.0F);

                firstPersonOffset = firstPersonOffset.rotateYaw(-(entity.pickerEntity.prevRotationYaw
                        + (entity.pickerEntity.rotationYaw - entity.pickerEntity.prevRotationYaw) * partialTick)
                        * (float) Math.PI / 180.0F);

                double targetX = entity.pickerEntity.prevPosX
                        + (entity.pickerEntity.posX - entity.pickerEntity.prevPosX) * (double) partialTick
                        + firstPersonOffset.x;
                double targetY = entity.pickerEntity.prevPosY
                        + (entity.pickerEntity.posY - entity.pickerEntity.prevPosY) * (double) partialTick
                        + firstPersonOffset.y;
                double targetZ = entity.pickerEntity.prevPosZ
                        + (entity.pickerEntity.posZ - entity.pickerEntity.prevPosZ) * (double) partialTick
                        + firstPersonOffset.z;

                if (this.renderManager.options.thirdPersonView != 0 || entity.pickerEntity != mc.player) {
                    Vec3d beamStart = VecUtility.calculateRay(entity.pickerEntity, 1.0f, partialTick,
                            new Vector3f(-0.1f, -0.25F, 0));
                    targetX = beamStart.x;
                    targetY = beamStart.y;
                    targetZ = beamStart.z;
                    GlStateManager.glLineWidth(2.0f);

                } else {
                    // stop drawing beam if gui disabled
                    if (!Minecraft.isGuiEnabled())
                        break drawBeam;

                    GlStateManager.glLineWidth(10.0f);
                }
                double eyeHeight = entity.pickerEntity.getEyeHeight();

                double x = (entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTick) + hitPoint.x - entityX;
                double y = (entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTick + 0.25D)
                        + hitPoint.y - entityY;
                double z = (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTick) + hitPoint.z
                        - entityZ;
                double diffX = (float) (targetX - x);
                double diffY = (double) ((float) (targetY - y)) + eyeHeight;
                double diffZ = (float) (targetZ - z);

                int beamColor = getBeamColor(entity.pickerEntity);
                GlStateManager.disableTexture2D();
                GlStateManager.disableLighting();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);
                bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
                float beamRed = (float) (beamColor >> 16 & 255) / 255.0F;
                float beamGreen = (float) (beamColor & 255) / 255.0F;
                float beamBlue = (float) (beamColor >> 8 & 255) / 255.0F;
                bufferBuilder.color(beamRed, beamGreen, beamBlue, 1);
                byte points = 16;
                for (int i = 0; i <= points; ++i) {
                    float curve = (float) i / (float) points;
                    bufferBuilder.pos(hitPoint.x + diffX * (double) curve,
                            hitPoint.y + (diffY + 0.0f) * (double) (curve * curve + curve) * 0.5D + 0.25D,
                            hitPoint.z + diffZ * (double) curve).endVertex();
                }
                tessellator.draw();

                // draw outline
                GlStateManager.glLineWidth(3);
                GlStateManager.glPolygonMode(GL11.GL_FRONT,GL11.GL_LINE);
                GlStateManager.disableTexture2D();
                GlStateManager.disableDepth();
                GlStateManager.enableRescaleNormal();

                draw(uncast, entityX, entityY, entityZ, partialTick, beamColor, true);

                GlStateManager.disableRescaleNormal();
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.glPolygonMode(GL11.GL_FRONT,GL11.GL_FILL);
                GlStateManager.glLineWidth(3);

                // Entity lighting
                int brightness = entity.getBrightnessForRender();
                int lightX = brightness % 65536;
                int lightY = brightness / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lightX / 1.0F,
                        (float) lightY / 1.0F);

                GlStateManager.enableLighting();
                GlStateManager.enableTexture2D();
                GlStateManager.glLineWidth(1.0f);
            }
        }

        draw(uncast, entityX, entityY, entityZ, partialTick, -1, false);
    }

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
