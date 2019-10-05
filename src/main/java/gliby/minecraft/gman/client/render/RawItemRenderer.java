package gliby.minecraft.gman.client.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class RawItemOverrideList extends ItemOverrideList {

    protected RawItemRenderer rawItem;

    public RawItemOverrideList(List<ItemOverride> overridesIn, RawItemRenderer rawItem) {
        super(overridesIn);
        this.rawItem = rawItem;
    }

    @Override
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
        this.rawItem.handleItemState(stack);
        return super.handleItemState(originalModel, stack, world, entity);
    }
}

/**
 * Credits: iChun
 *
 * @author Gliby
 * <p>
 * Based on iChunUtil's own item renderer, but with a some of my own
 * spice. # Usage: # Simply extend this class, then register with
 * ItemRendererManager!
 */
@SuppressWarnings("deprecation")
public abstract class RawItemRenderer implements IBakedModel {

    private static final List<BakedQuad> DUMMY_LIST = Collections.emptyList();
    public ModelResourceLocation modelResourceLocation;
    protected ModelBiped playerBiped;
    protected TransformType transformType;
    // Entity Data
    protected EntityPlayer owner;
    protected ItemStack itemStack;
    protected Item itemInstance;
    private Pair<IBakedModel, Matrix4f> pair;
    private RawItemOverrideList itemOverride;
    public RawItemRenderer(ModelResourceLocation resourceLocation) {
        this.modelResourceLocation = resourceLocation;

        this.pair = Pair.of((IBakedModel) this, null);
        this.playerBiped = new ModelBiped();
        this.playerBiped.textureWidth = 64;
        this.playerBiped.textureHeight = 64;
        this.itemOverride = new RawItemOverrideList(new ArrayList<ItemOverride>(), this);
    }

    public Item getItemInstance() {
        return itemInstance;
    }

    public RawItemRenderer setItemInstance(Item itemInstance) {
        this.itemInstance = itemInstance;

        ItemCameraTransforms transforms = getItemCameraTransforms();
        if (transforms != null) {
            ItemTransformVec3f[] fixed = {transforms.thirdperson_right, transforms.thirdperson_left, transforms.firstperson_left,
                    transforms.firstperson_right, transforms.gui, transforms.head, transforms.fixed, transforms.ground};


            for (ItemTransformVec3f transform : fixed) {
//                transform.rotation.set((float) Math.toRadians(transform.rotation.x),
//                        (float) Math.toRadians(transform.rotation.y),
//                        (float) Math.toRadians(transform.rotation.z));
//                transform.translation.scale(2);
            }
        }

        return this;
    }

    public abstract void render();

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null || !Tessellator.getInstance().getBuffer().isDrawing) {
            return DUMMY_LIST;
        }

        // Method that this get's called in, is using startDrawingQuads. We
        // finish
        // drawing it so we can move on to render our own thing.
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        // finish drawing, if we are.
        boolean isDrawing = tessellator.getBuffer().isDrawing;
        if (isDrawing)
            tessellator.draw();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5f, 0.5f, 0.5f);
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);

        if (owner != null) {
            if (transformType == TransformType.THIRD_PERSON_LEFT_HAND || transformType == TransformType.THIRD_PERSON_RIGHT_HAND) {
                if (owner.isSneaking())
                    GlStateManager.translate(0.0F, -0.2F, 0.0F);
            }
        }

        if (onGround()) {
            GlStateManager.scale(-3f, -3f, -3f);
        }

        render();
        rebindTexture();
        GlStateManager.popMatrix();

        // Reset the dynamic values.
        this.owner = null;
        this.itemStack = null;
        this.transformType = null;
        // Method that this gets called is expecting that we are still using
        // startDrawingQuads.

        if (isDrawing) //restart the drawing of the tessellator
        {
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.ITEM);
        }

        return DUMMY_LIST;
    }

    public void bindTexture(@Nonnull ResourceLocation rs) {
        Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(rs);
    }

    protected void rebindTexture() {
        Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }

    protected boolean onGround() {
        return transformType == null;
    }


    @Override
    public final boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public final boolean isGui3d() {
        return true;
    }

    @Override
    public final boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return itemOverride;
    }


    @Override
    public abstract ItemCameraTransforms getItemCameraTransforms();

    public IBakedModel handleItemState(ItemStack stack) {
        this.itemStack = stack;
        return this;
    }

    public void setOwner(EntityPlayer player) {
        this.owner = player;
    }

    @Override
    public Pair<IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
        this.transformType = cameraTransformType;
        switch (cameraTransformType) {
            case FIRST_PERSON_LEFT_HAND:
                ItemCameraTransforms.applyTransformSide(getItemCameraTransforms().firstperson_left, true);
                break;
            case FIRST_PERSON_RIGHT_HAND:
                ItemCameraTransforms.applyTransformSide(getItemCameraTransforms().firstperson_right, false);
                break;
            case GUI:
                ItemCameraTransforms.applyTransformSide(getItemCameraTransforms().gui, false);
                break;
            case HEAD:
                ItemCameraTransforms.applyTransformSide(getItemCameraTransforms().head, false);
                break;
            case THIRD_PERSON_LEFT_HAND:
                ItemCameraTransforms.applyTransformSide(getItemCameraTransforms().thirdperson_left, true);
                break;
            case THIRD_PERSON_RIGHT_HAND:
                ItemCameraTransforms.applyTransformSide(getItemCameraTransforms().thirdperson_right, false);
                break;
            default:
                break;
        }
        return pair;
    }

//    public static void applyVanillaTransform(ItemTransformVec3f transform, boolean leftHand)
//    {
//        if (transform != ItemTransformVec3f.DEFAULT)
//        {
//            int swap = 1;
//
//            GlStateManager.translate(transform.translation.x * swap, transform.translation.y , transform.translation.z);
//            GlStateManager.rotate((float) Math.toRadians(transform.rotation.y), 0.0F, 1.0F, 0.0F);
//            GlStateManager.rotate((float) Math.toRadians(transform.rotation.x * swap), 1.0F, 0.0F, 0.0F);
//            GlStateManager.rotate((float) Math.toRadians(transform.rotation.z * swap), 0.0F, 0.0F, 1.0F);
//            GlStateManager.scale(transform.scale.x , transform.scale.y, transform.scale.z);
//        }
//    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
    }
}
