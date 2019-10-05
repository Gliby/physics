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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
        return interpolatableEntity.getPickerEntity() != null
                || entity.isInRangeToRender3d(camX, camY, camZ) && (entity.ignoreFrustumCheck
                || camera.isBoundingBoxInFrustum(interpolatableEntity.getRenderBoundingBox()));
    }

    protected abstract void draw(Entity uncast, double entityX, double entityY, double entityZ, float partialTick,
                                 int color, boolean outline);


    public void doRender(Entity uncast, double entityX, double entityY, double entityZ, float twen, float deltaTime) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        EntityPhysicsBase entity = (EntityPhysicsBase) uncast;
        drawBeam:
        if (entity.getPickerEntity() != null) {
            EntityPlayer pickerEntity = entity.getPickerEntity();
            Item item = pickerEntity.getHeldItemMainhand() != null ? pickerEntity.getHeldItemMainhand().getItem() : null;
            if (item instanceof ItemPhysicsGun) {

                Vector3f hitPoint = getRenderHitPoint(entity, deltaTime);
                Vec3d firstPersonOffset = new Vec3d(-0.10f, -0.05D, 0.35D);

                firstPersonOffset = firstPersonOffset.rotatePitch(-(pickerEntity.prevRotationPitch
                        + (pickerEntity.rotationPitch - pickerEntity.prevRotationPitch) * deltaTime)
                        * (float) Math.PI / 180.0F);

                firstPersonOffset = firstPersonOffset.rotateYaw(-(pickerEntity.prevRotationYaw
                        + (pickerEntity.rotationYaw - pickerEntity.prevRotationYaw) * deltaTime)
                        * (float) Math.PI / 180.0F);

                double targetX = pickerEntity.prevPosX
                        + (pickerEntity.posX - pickerEntity.prevPosX) * (double) deltaTime
                        + firstPersonOffset.x;
                double targetY = pickerEntity.prevPosY
                        + (pickerEntity.posY - pickerEntity.prevPosY) * (double) deltaTime
                        + firstPersonOffset.y;
                double targetZ = pickerEntity.prevPosZ
                        + (pickerEntity.posZ - pickerEntity.prevPosZ) * (double) deltaTime
                        + firstPersonOffset.z;

                if (this.renderManager.options.thirdPersonView != 0 || pickerEntity != mc.player) {
                    Vec3d beamStart = VecUtility.calculateRay(pickerEntity, 1.0f, deltaTime,
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

                double eyeHeight = pickerEntity.getEyeHeight();

                double x = (entity.prevPosX + (entity.posX - entity.prevPosX) * (double) deltaTime) + hitPoint.x - entityX;
                double y = (entity.prevPosY + (entity.posY - entity.prevPosY) * (double) deltaTime + 0.25D)
                        + hitPoint.y - entityY;
                double z = (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) deltaTime) + hitPoint.z
                        - entityZ;
                double diffX = (float) (targetX - x);
                double diffY = (double) ((float) (targetY - y)) + eyeHeight;
                double diffZ = (float) (targetZ - z);

                int beamColor = getBeamColor(pickerEntity);
                GlStateManager.disableTexture2D();
                GlStateManager.disableLighting();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);

                bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
                float beamRed = (float) (beamColor >> 16 & 255) / 255.0F;
                float beamGreen = (float) (beamColor & 255) / 255.0F;
                float beamBlue = (float) (beamColor >> 8 & 255) / 255.0F;
//                bufferBuilder.color(beamRed, beamGreen, beamBlue, 1);
                byte points = 16;
                for (int i = 0; i <= points; ++i) {
                    float curve = (float) i / (float) points;
                    bufferBuilder.pos(hitPoint.x + diffX * (double) curve,
                            hitPoint.y + (diffY + 0.0f) * (double) (curve * curve + curve) * 0.5D + 0.25D,
                            hitPoint.z + diffZ * (double) curve).color(beamRed, beamBlue, beamGreen, 1.0f).endVertex();
                }
                tessellator.draw();

                // draw outline
                GlStateManager.glLineWidth(3);
                GlStateManager.glPolygonMode(GL11.GL_FRONT,GL11.GL_LINE);
                GlStateManager.disableTexture2D();
                GlStateManager.disableDepth();
                GlStateManager.disableCull();
                GlStateManager.enableRescaleNormal();

                draw(uncast, entityX, entityY, entityZ, deltaTime, beamColor, true);

                GlStateManager.disableRescaleNormal();
                GlStateManager.enableCull();
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

        draw(uncast, entityX, entityY, entityZ, deltaTime, -1, false);
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
