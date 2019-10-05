package gliby.minecraft.physics.client.render.items;

import gliby.minecraft.gman.client.render.RawItemRenderer;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.game.items.toolgun.ItemToolGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.util.vector.Vector3f;


/**
 *
 */
@SuppressWarnings("deprecation")
public class RenderItemToolGun extends RawItemRenderer {

    ItemCameraTransforms transforms = new ItemCameraTransforms(
            new ItemTransformVec3f(new Vector3f(180, 0, 0), new Vector3f(0, 0, 0), new Vector3f(-0.5f, 0.5f, 0.5f)),
            new ItemTransformVec3f(new Vector3f(180, 0, 0), new Vector3f(0, 0.1F, -0.25f), new Vector3f(-0.75f, 0.75f, 0.75f)),
            new ItemTransformVec3f(new Vector3f(8, 1f, 0.0f), new Vector3f(0, 0.25f, -1), new Vector3f(-1, -1, -1)),
            new ItemTransformVec3f(new Vector3f(8, 1f, 0.0f), new Vector3f(0, 0.25f, -1), new Vector3f(-1, -1, -1)),
            ItemTransformVec3f.DEFAULT,
            new ItemTransformVec3f(new Vector3f(30, 135, 0), new Vector3f(-0.135f, -0.080f, 0), new Vector3f(-0.9f, -0.9f, -0.9f)),
            ItemTransformVec3f.DEFAULT,
            ItemTransformVec3f.DEFAULT);

    private ModelRenderer bipedRightArm;
    private ModelItemToolGun modelToolGun = new ModelItemToolGun();
    private ResourceLocation modelTexture = new ResourceLocation(Physics.ID, "textures/tool_gun.png");
    private ResourceLocation screenTexture = new ResourceLocation(Physics.ID, "textures/tool_gunScreen.png");

    /**
     * @param resourceLocation
     */
    public RenderItemToolGun(ModelResourceLocation resourceLocation) {
        super(resourceLocation);
        this.bipedRightArm = new ModelRenderer(playerBiped, 40, 16);
        this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0);
        this.bipedRightArm.setRotationPoint(-5.0F, 2.0F + 0, 0.0F);
    }

    // TODO When not held, use super low resolution textures.
    @Override
    public void render() {
        float scale = -(1.0f / 16.0f);
        String text = "Fine in 4K";

        if (owner != null) {
            if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND || transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
                text = ((ItemToolGun) itemStack.getItem()).getModeName();
                if (!owner.isInvisible()) {
                    bindTexture(Minecraft.getMinecraft().player.getLocationSkin());
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(1.25f, 1.25f, 1.25f);
                    GlStateManager.translate(-0.4f, -0.17f, -0.89f);
                    bipedRightArm.rotateAngleX = -93 / (180F / (float) Math.PI);
                    bipedRightArm.render(scale);
                    GlStateManager.popMatrix();
                }
            }
        }
        bindTexture(modelTexture);

        modelToolGun.render(null, 0, 0, 0, 0, 0, scale);
        // if (transformType == TransformType.FIRST_PERSON) {

        GlStateManager.disableLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);
        GlStateManager.pushMatrix();
        scale *= 1.0f / 8.0f;
        // GlStateManager.disableTexture2D();
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(modelToolGun.screen.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-16, 0, -62.41f);

        Minecraft mc = Minecraft.getMinecraft();


        GlStateManager.pushMatrix();
        mc.getTextureManager().bindTexture(screenTexture);
        GlStateManager.scale(0.25f, 0.25f, 1);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 128, 128, 128, 128);
        GlStateManager.popMatrix();

        if (transformType != TransformType.GUI) {
            // Gui.drawRect(0, 0, 32, 32, 0xFF00FF00);
            // gets rid of zbuffering with screen texture
            GlStateManager.translate(0, 0, -0.01f);

            int text_width = mc.fontRenderer.getStringWidth(text);
            int perfect_size = 32;
            float scaleFactor = ((float) perfect_size / text_width);
            scale = MathHelper.clamp(0.75f * scaleFactor, 0F, 1F);
            float x = 32;
            float y = 32;
            // TODO cosmetic: make text scale vertically
            GlStateManager.translate(21 - (text_width / 2) * scaleFactor, 8, 0);
            GlStateManager.scale(scale, scale, 0);
            mc.fontRenderer.drawString(text, 0, 0, -1);

        }
        // Gui.drawRect(0, 0, 3000, 3000, 0xFF999999);
        // GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        // }
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return transforms;
    }
}
