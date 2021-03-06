package gliby.minecraft.physics.client.render.items;

import gliby.minecraft.gman.client.render.RawItemRenderer;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.client.render.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
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
public class RenderItemPhysicsGun extends RawItemRenderer {

    // TODO (0.6.0) Add Item Frame transform
    private ItemCameraTransforms
            transforms = new ItemCameraTransforms(
            new ItemTransformVec3f(new Vector3f(180, 0, 0), new Vector3f(0, 0, 0), new Vector3f(-1, 1, 1)),
            new ItemTransformVec3f(new Vector3f(180, 0, 0), new Vector3f(0, 0, 0), new Vector3f(-1, 1, 1)),
            new ItemTransformVec3f(new Vector3f(10, 0, 0), new Vector3f(-0.25f, 0.25f, -1.0f / 16.0f), new Vector3f(-1, -1, -1)),
            new ItemTransformVec3f(new Vector3f(10, 0, 0), new Vector3f(-0.25f, 0.25f, -1.0f / 16.0f), new Vector3f(-1, -1, -1)),
            ItemTransformVec3f.DEFAULT,
            new ItemTransformVec3f(new Vector3f(30, 135, 0), new Vector3f(0.05f, 0.075f, 0), new Vector3f(-0.9f, -0.9f, -0.9f)),
            ItemTransformVec3f.DEFAULT,
            ItemTransformVec3f.DEFAULT);

    private ModelRenderer bipedLeftArm;
    private RenderHandler renderHandler;
    private ModelItemPhysicsGun model = new ModelItemPhysicsGun(0xFF87FFFF, 200);
    private ResourceLocation textureColorable = new ResourceLocation(Physics.ID,
            "textures/physics_gun_colorable.png");


    public RenderItemPhysicsGun(RenderHandler renderHandler, ModelResourceLocation resourceLocation) {
        super(resourceLocation);
        this.bipedLeftArm = new ModelRenderer(playerBiped, 40, 16);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0);
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + 0, 0.0F);
        this.renderHandler = renderHandler;
    }

    @Override
    public void render() {
        model.setColor(0xFF87FFFF);
        float scale = -0.0625f;
        float f1 = 40F - MathHelper.abs(
                MathHelper.sin((float) (Minecraft.getSystemTime() % 1000L) / 1000.0F * (float) Math.PI * 1.0F) * 40F);
        int lightValue = (int) MathHelper.clamp((200 + f1), 0, 250);

        if (owner != null) {
            if (transformType == TransformType.FIRST_PERSON_LEFT_HAND || transformType == TransformType.FIRST_PERSON_RIGHT_HAND) {
                if (!owner.isInvisible()) {
                    bindTexture(Minecraft.getMinecraft().player.getLocationSkin());
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(-0.19f, 0.05f, -0.55f);
                    this.bipedLeftArm.rotateAngleY = 15 / (180F / (float) Math.PI);
                    this.bipedLeftArm.rotateAngleX = 270 / (180F / (float) Math.PI);
                    bipedLeftArm.render(scale * 0.75f);
                    GlStateManager.popMatrix();
                }
            }

            if (renderHandler.getPhysicsGunColors().containsKey(owner.getUniqueID().toString())) {
                model.setColor(renderHandler.getPhysicsGunColors().get(owner.getUniqueID().toString()));
            }
        }

        bindTexture(textureColorable);
        model.setLightValue(lightValue);
        model.render(null, 0, 0, 0, 0, 0, scale);

    }


    public ItemCameraTransforms getItemCameraTransforms() {
        return transforms;
    }
}
