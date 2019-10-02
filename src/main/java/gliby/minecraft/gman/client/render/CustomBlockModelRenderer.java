package gliby.minecraft.gman.client.render;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CustomBlockModelRenderer
{
    private final BlockColors blockColors;

    public CustomBlockModelRenderer(BlockColors blockColorsIn)
    {
        this.blockColors = blockColorsIn;
    }

    public boolean renderModel(BufferBuilder buffer, IBlockAccess worldIn, IBakedModel modelIn, IBlockState stateIn, BlockPos posIn, int brightness)
    {

        try
        {
            return this.renderModelFlat(worldIn, modelIn, stateIn, posIn, buffer, brightness);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block model");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Physics block model being tesselated");
            CrashReportCategory.addBlockInfo(crashreportcategory, posIn, stateIn);
//            crashreportcategory.addCrashSection("Using AO", Boolean.valueOf(flag));
            throw new ReportedException(crashreport);
        }
    }



    public boolean renderModelFlat(IBlockAccess worldIn, IBakedModel modelIn, IBlockState stateIn, BlockPos posIn, BufferBuilder buffer, int brightness)
    {
        boolean flag = false;
        BitSet bitset = new BitSet(3);

        for (EnumFacing enumfacing : EnumFacing.values())
        {
            List<BakedQuad> list = modelIn.getQuads(stateIn, enumfacing, 0);

            if (!list.isEmpty())
            {
//                int i = stateIn.getPackedLightmapCoords(worldIn, posIn.offset(enumfacing));
                this.renderQuadsFlat(worldIn, stateIn, posIn, brightness, false, buffer, list, bitset);
                flag = true;
            }
        }

        List<BakedQuad> list1 = modelIn.getQuads(stateIn, (EnumFacing)null, 0);

        if (!list1.isEmpty())
        {
            this.renderQuadsFlat(worldIn, stateIn, BlockPos.ORIGIN, brightness, false, buffer, list1, bitset);
            flag = true;
        }

        return flag;
    }

    private void renderQuadsSmooth(IBlockAccess blockAccessIn, IBlockState stateIn, BlockPos posIn, BufferBuilder buffer, List<BakedQuad> list, float[] quadBounds, BitSet bitSet, CustomBlockModelRenderer.AmbientOcclusionFace aoFace)
    {
        Vec3d vec3d = stateIn.getOffset(blockAccessIn, posIn);
        double d0 = (double)posIn.getX() + vec3d.x;
        double d1 = (double)posIn.getY() + vec3d.y;
        double d2 = (double)posIn.getZ() + vec3d.z;
        int i = 0;

        for (int j = list.size(); i < j; ++i)
        {
            BakedQuad bakedquad = list.get(i);
            this.fillQuadBounds(stateIn, bakedquad.getVertexData(), bakedquad.getFace(), quadBounds, bitSet);
            aoFace.updateVertexBrightness(blockAccessIn, stateIn, posIn, bakedquad.getFace(), quadBounds, bitSet);
            buffer.addVertexData(bakedquad.getVertexData());
            buffer.putBrightness4(aoFace.vertexBrightness[0], aoFace.vertexBrightness[1], aoFace.vertexBrightness[2], aoFace.vertexBrightness[3]);
            if(bakedquad.shouldApplyDiffuseLighting())
            {
                float diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(bakedquad.getFace());
                aoFace.vertexColorMultiplier[0] *= diffuse;
                aoFace.vertexColorMultiplier[1] *= diffuse;
                aoFace.vertexColorMultiplier[2] *= diffuse;
                aoFace.vertexColorMultiplier[3] *= diffuse;
            }
            if (bakedquad.hasTintIndex())
            {
                int k = this.blockColors.colorMultiplier(stateIn, blockAccessIn, posIn, bakedquad.getTintIndex());

                if (EntityRenderer.anaglyphEnable)
                {
                    k = TextureUtil.anaglyphColor(k);
                }

                float f = (float)(k >> 16 & 255) / 255.0F;
                float f1 = (float)(k >> 8 & 255) / 255.0F;
                float f2 = (float)(k & 255) / 255.0F;
                buffer.putColorMultiplier(aoFace.vertexColorMultiplier[0] * f, aoFace.vertexColorMultiplier[0] * f1, aoFace.vertexColorMultiplier[0] * f2, 4);
                buffer.putColorMultiplier(aoFace.vertexColorMultiplier[1] * f, aoFace.vertexColorMultiplier[1] * f1, aoFace.vertexColorMultiplier[1] * f2, 3);
                buffer.putColorMultiplier(aoFace.vertexColorMultiplier[2] * f, aoFace.vertexColorMultiplier[2] * f1, aoFace.vertexColorMultiplier[2] * f2, 2);
                buffer.putColorMultiplier(aoFace.vertexColorMultiplier[3] * f, aoFace.vertexColorMultiplier[3] * f1, aoFace.vertexColorMultiplier[3] * f2, 1);
            }
            else
            {
                buffer.putColorMultiplier(aoFace.vertexColorMultiplier[0], aoFace.vertexColorMultiplier[0], aoFace.vertexColorMultiplier[0], 4);
                buffer.putColorMultiplier(aoFace.vertexColorMultiplier[1], aoFace.vertexColorMultiplier[1], aoFace.vertexColorMultiplier[1], 3);
                buffer.putColorMultiplier(aoFace.vertexColorMultiplier[2], aoFace.vertexColorMultiplier[2], aoFace.vertexColorMultiplier[2], 2);
                buffer.putColorMultiplier(aoFace.vertexColorMultiplier[3], aoFace.vertexColorMultiplier[3], aoFace.vertexColorMultiplier[3], 1);
            }

            buffer.putPosition(d0, d1, d2);
        }
    }

    private void fillQuadBounds(IBlockState stateIn, int[] vertexData, EnumFacing face, @Nullable float[] quadBounds, BitSet boundsFlags)
    {
        float f = 32.0F;
        float f1 = 32.0F;
        float f2 = 32.0F;
        float f3 = -32.0F;
        float f4 = -32.0F;
        float f5 = -32.0F;

        for (int i = 0; i < 4; ++i)
        {
            float f6 = Float.intBitsToFloat(vertexData[i * 7]);
            float f7 = Float.intBitsToFloat(vertexData[i * 7 + 1]);
            float f8 = Float.intBitsToFloat(vertexData[i * 7 + 2]);
            f = Math.min(f, f6);
            f1 = Math.min(f1, f7);
            f2 = Math.min(f2, f8);
            f3 = Math.max(f3, f6);
            f4 = Math.max(f4, f7);
            f5 = Math.max(f5, f8);
        }

        if (quadBounds != null)
        {
            quadBounds[EnumFacing.WEST.getIndex()] = f;
            quadBounds[EnumFacing.EAST.getIndex()] = f3;
            quadBounds[EnumFacing.DOWN.getIndex()] = f1;
            quadBounds[EnumFacing.UP.getIndex()] = f4;
            quadBounds[EnumFacing.NORTH.getIndex()] = f2;
            quadBounds[EnumFacing.SOUTH.getIndex()] = f5;
            int j = EnumFacing.values().length;
            quadBounds[EnumFacing.WEST.getIndex() + j] = 1.0F - f;
            quadBounds[EnumFacing.EAST.getIndex() + j] = 1.0F - f3;
            quadBounds[EnumFacing.DOWN.getIndex() + j] = 1.0F - f1;
            quadBounds[EnumFacing.UP.getIndex() + j] = 1.0F - f4;
            quadBounds[EnumFacing.NORTH.getIndex() + j] = 1.0F - f2;
            quadBounds[EnumFacing.SOUTH.getIndex() + j] = 1.0F - f5;
        }

        float f9 = 1.0E-4F;
        float f10 = 0.9999F;

        switch (face)
        {
            case DOWN:
                boundsFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
                boundsFlags.set(0, (f1 < 1.0E-4F || stateIn.isFullCube()) && f1 == f4);
                break;
            case UP:
                boundsFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
                boundsFlags.set(0, (f4 > 0.9999F || stateIn.isFullCube()) && f1 == f4);
                break;
            case NORTH:
                boundsFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
                boundsFlags.set(0, (f2 < 1.0E-4F || stateIn.isFullCube()) && f2 == f5);
                break;
            case SOUTH:
                boundsFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
                boundsFlags.set(0, (f5 > 0.9999F || stateIn.isFullCube()) && f2 == f5);
                break;
            case WEST:
                boundsFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
                boundsFlags.set(0, (f < 1.0E-4F || stateIn.isFullCube()) && f == f3);
                break;
            case EAST:
                boundsFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
                boundsFlags.set(0, (f3 > 0.9999F || stateIn.isFullCube()) && f == f3);
        }
    }

    private void renderQuadsFlat(IBlockAccess blockAccessIn, IBlockState stateIn, BlockPos posIn, int brightnessIn, boolean ownBrightness, BufferBuilder buffer, List<BakedQuad> list, BitSet bitSet)
    {
        Vec3d vec3d = stateIn.getOffset(blockAccessIn, posIn);
        double d0 = vec3d.x;
        double d1 = vec3d.y;
        double d2 = vec3d.z;
        int i = 0;

        for (int j = list.size(); i < j; ++i)
        {
            BakedQuad bakedquad = list.get(i);

            if (ownBrightness)
            {
                this.fillQuadBounds(stateIn, bakedquad.getVertexData(), bakedquad.getFace(), (float[])null, bitSet);
                BlockPos blockpos = bitSet.get(0) ? posIn.offset(bakedquad.getFace()) : posIn;
                brightnessIn = stateIn.getPackedLightmapCoords(blockAccessIn, blockpos);
            }

            buffer.addVertexData(bakedquad.getVertexData());
            buffer.putBrightness4(brightnessIn, brightnessIn, brightnessIn, brightnessIn);

            if (bakedquad.hasTintIndex())
            {
                int k = this.blockColors.colorMultiplier(stateIn, blockAccessIn, posIn, bakedquad.getTintIndex());

                if (EntityRenderer.anaglyphEnable)
                {
                    k = TextureUtil.anaglyphColor(k);
                }

                float f = (float)(k >> 16 & 255) / 255.0F;
                float f1 = (float)(k >> 8 & 255) / 255.0F;
                float f2 = (float)(k & 255) / 255.0F;
                if(bakedquad.shouldApplyDiffuseLighting())
                {
                    float diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(bakedquad.getFace());
                    f *= diffuse;
                    f1 *= diffuse;
                    f2 *= diffuse;
                }
                buffer.putColorMultiplier(f, f1, f2, 4);
                buffer.putColorMultiplier(f, f1, f2, 3);
                buffer.putColorMultiplier(f, f1, f2, 2);
                buffer.putColorMultiplier(f, f1, f2, 1);
            }
            else if(bakedquad.shouldApplyDiffuseLighting())
            {
                float diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(bakedquad.getFace());
                buffer.putColorMultiplier(diffuse, diffuse, diffuse, 4);
                buffer.putColorMultiplier(diffuse, diffuse, diffuse, 3);
                buffer.putColorMultiplier(diffuse, diffuse, diffuse, 2);
                buffer.putColorMultiplier(diffuse, diffuse, diffuse, 1);
            }

            buffer.putPosition(d0, d1, d2);
        }
    }

    public void renderModelBrightnessColor(IBakedModel bakedModel, float red, float green, float blue, boolean shouldTint)
    {
        this.renderModelBrightnessColor((IBlockState)null, bakedModel, shouldTint, red, green, blue);
    }

    public void renderModelBrightnessColor(IBlockState state, IBakedModel bakedModel, boolean shouldTint, float red, float blue, float green)
    {
        for (EnumFacing enumfacing : EnumFacing.values())
        {
            this.renderModelBrightnessColorQuads(red, blue, green, shouldTint, bakedModel.getQuads(state, enumfacing, 0L));
        }

        this.renderModelBrightnessColorQuads(red, blue, green, shouldTint, bakedModel.getQuads(state, (EnumFacing)null, 0L));
    }


    private void renderModelBrightnessColorQuads(float red, float green, float blue, boolean shouldTint, List<BakedQuad> listQuads)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        int i = 0;

        for (int j = listQuads.size(); i < j; ++i)
        {
            BakedQuad bakedquad = listQuads.get(i);
            bufferbuilder.begin(7, DefaultVertexFormats.ITEM);
            bufferbuilder.addVertexData(bakedquad.getVertexData());

            if (bakedquad.hasTintIndex() && shouldTint)
            {
                bufferbuilder.putColorRGB_F4(red , green, blue );
            }

            Vec3i vec3i = bakedquad.getFace().getDirectionVec();
            bufferbuilder.putNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
            tessellator.draw();
        }
    }

    @SideOnly(Side.CLIENT)
    class AmbientOcclusionFace
    {
        private final float[] vertexColorMultiplier = new float[4];
        private final int[] vertexBrightness = new int[4];

        public void updateVertexBrightness(IBlockAccess worldIn, IBlockState state, BlockPos centerPos, EnumFacing direction, float[] faceShape, BitSet shapeState)
        {
            BlockPos blockpos = shapeState.get(0) ? centerPos.offset(direction) : centerPos;
            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();
            CustomBlockModelRenderer.EnumNeighborInfo blockmodelrenderer$enumneighborinfo = CustomBlockModelRenderer.EnumNeighborInfo.getNeighbourInfo(direction);
            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos1 = BlockPos.PooledMutableBlockPos.retain(blockpos).move(blockmodelrenderer$enumneighborinfo.corners[0]);
            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos2 = BlockPos.PooledMutableBlockPos.retain(blockpos).move(blockmodelrenderer$enumneighborinfo.corners[1]);
            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos3 = BlockPos.PooledMutableBlockPos.retain(blockpos).move(blockmodelrenderer$enumneighborinfo.corners[2]);
            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos4 = BlockPos.PooledMutableBlockPos.retain(blockpos).move(blockmodelrenderer$enumneighborinfo.corners[3]);
            int i = state.getPackedLightmapCoords(worldIn, blockpos$pooledmutableblockpos1);
            int j = state.getPackedLightmapCoords(worldIn, blockpos$pooledmutableblockpos2);
            int k = state.getPackedLightmapCoords(worldIn, blockpos$pooledmutableblockpos3);
            int l = state.getPackedLightmapCoords(worldIn, blockpos$pooledmutableblockpos4);
            float f = worldIn.getBlockState(blockpos$pooledmutableblockpos1).getAmbientOcclusionLightValue();
            float f1 = worldIn.getBlockState(blockpos$pooledmutableblockpos2).getAmbientOcclusionLightValue();
            float f2 = worldIn.getBlockState(blockpos$pooledmutableblockpos3).getAmbientOcclusionLightValue();
            float f3 = worldIn.getBlockState(blockpos$pooledmutableblockpos4).getAmbientOcclusionLightValue();
            boolean flag = worldIn.getBlockState(blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos1).move(direction)).isTranslucent();
            boolean flag1 = worldIn.getBlockState(blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos2).move(direction)).isTranslucent();
            boolean flag2 = worldIn.getBlockState(blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos3).move(direction)).isTranslucent();
            boolean flag3 = worldIn.getBlockState(blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos4).move(direction)).isTranslucent();
            float f4;
            int i1;

            if (!flag2 && !flag)
            {
                f4 = f;
                i1 = i;
            }
            else
            {
                BlockPos blockpos1 = blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos1).move(blockmodelrenderer$enumneighborinfo.corners[2]);
                f4 = worldIn.getBlockState(blockpos1).getAmbientOcclusionLightValue();
                i1 = state.getPackedLightmapCoords(worldIn, blockpos1);
            }

            float f5;
            int j1;

            if (!flag3 && !flag)
            {
                f5 = f;
                j1 = i;
            }
            else
            {
                BlockPos blockpos2 = blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos1).move(blockmodelrenderer$enumneighborinfo.corners[3]);
                f5 = worldIn.getBlockState(blockpos2).getAmbientOcclusionLightValue();
                j1 = state.getPackedLightmapCoords(worldIn, blockpos2);
            }

            float f6;
            int k1;

            if (!flag2 && !flag1)
            {
                f6 = f1;
                k1 = j;
            }
            else
            {
                BlockPos blockpos3 = blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos2).move(blockmodelrenderer$enumneighborinfo.corners[2]);
                f6 = worldIn.getBlockState(blockpos3).getAmbientOcclusionLightValue();
                k1 = state.getPackedLightmapCoords(worldIn, blockpos3);
            }

            float f7;
            int l1;

            if (!flag3 && !flag1)
            {
                f7 = f1;
                l1 = j;
            }
            else
            {
                BlockPos blockpos4 = blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos2).move(blockmodelrenderer$enumneighborinfo.corners[3]);
                f7 = worldIn.getBlockState(blockpos4).getAmbientOcclusionLightValue();
                l1 = state.getPackedLightmapCoords(worldIn, blockpos4);
            }

            int i3 = state.getPackedLightmapCoords(worldIn, centerPos);

            if (shapeState.get(0) || !worldIn.getBlockState(centerPos.offset(direction)).isOpaqueCube())
            {
                i3 = state.getPackedLightmapCoords(worldIn, centerPos.offset(direction));
            }

            float f8 = shapeState.get(0) ? worldIn.getBlockState(blockpos).getAmbientOcclusionLightValue() : worldIn.getBlockState(centerPos).getAmbientOcclusionLightValue();
            CustomBlockModelRenderer.VertexTranslations blockmodelrenderer$vertextranslations = CustomBlockModelRenderer.VertexTranslations.getVertexTranslations(direction);
            blockpos$pooledmutableblockpos.release();
            blockpos$pooledmutableblockpos1.release();
            blockpos$pooledmutableblockpos2.release();
            blockpos$pooledmutableblockpos3.release();
            blockpos$pooledmutableblockpos4.release();

            if (shapeState.get(1) && blockmodelrenderer$enumneighborinfo.doNonCubicWeight)
            {
                float f29 = (f3 + f + f5 + f8) * 0.25F;
                float f30 = (f2 + f + f4 + f8) * 0.25F;
                float f31 = (f2 + f1 + f6 + f8) * 0.25F;
                float f32 = (f3 + f1 + f7 + f8) * 0.25F;
                float f13 = faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[0].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[1].shape];
                float f14 = faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[2].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[3].shape];
                float f15 = faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[4].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[5].shape];
                float f16 = faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[6].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[7].shape];
                float f17 = faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[0].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[1].shape];
                float f18 = faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[2].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[3].shape];
                float f19 = faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[4].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[5].shape];
                float f20 = faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[6].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[7].shape];
                float f21 = faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[0].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[1].shape];
                float f22 = faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[2].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[3].shape];
                float f23 = faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[4].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[5].shape];
                float f24 = faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[6].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[7].shape];
                float f25 = faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[0].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[1].shape];
                float f26 = faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[2].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[3].shape];
                float f27 = faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[4].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[5].shape];
                float f28 = faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[6].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[7].shape];
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert0] = f29 * f13 + f30 * f14 + f31 * f15 + f32 * f16;
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert1] = f29 * f17 + f30 * f18 + f31 * f19 + f32 * f20;
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert2] = f29 * f21 + f30 * f22 + f31 * f23 + f32 * f24;
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert3] = f29 * f25 + f30 * f26 + f31 * f27 + f32 * f28;
                int i2 = this.getAoBrightness(l, i, j1, i3);
                int j2 = this.getAoBrightness(k, i, i1, i3);
                int k2 = this.getAoBrightness(k, j, k1, i3);
                int l2 = this.getAoBrightness(l, j, l1, i3);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert0] = this.getVertexBrightness(i2, j2, k2, l2, f13, f14, f15, f16);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert1] = this.getVertexBrightness(i2, j2, k2, l2, f17, f18, f19, f20);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert2] = this.getVertexBrightness(i2, j2, k2, l2, f21, f22, f23, f24);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert3] = this.getVertexBrightness(i2, j2, k2, l2, f25, f26, f27, f28);
            }
            else
            {
                float f9 = (f3 + f + f5 + f8) * 0.25F;
                float f10 = (f2 + f + f4 + f8) * 0.25F;
                float f11 = (f2 + f1 + f6 + f8) * 0.25F;
                float f12 = (f3 + f1 + f7 + f8) * 0.25F;
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert0] = this.getAoBrightness(l, i, j1, i3);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert1] = this.getAoBrightness(k, i, i1, i3);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert2] = this.getAoBrightness(k, j, k1, i3);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert3] = this.getAoBrightness(l, j, l1, i3);
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert0] = f9;
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert1] = f10;
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert2] = f11;
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert3] = f12;
            }
        }

        /**
         * Get ambient occlusion brightness
         */
        private int getAoBrightness(int br1, int br2, int br3, int br4)
        {
            if (br1 == 0)
            {
                br1 = br4;
            }

            if (br2 == 0)
            {
                br2 = br4;
            }

            if (br3 == 0)
            {
                br3 = br4;
            }

            return br1 + br2 + br3 + br4 >> 2 & 16711935;
        }

        private int getVertexBrightness(int p_178203_1_, int p_178203_2_, int p_178203_3_, int p_178203_4_, float p_178203_5_, float p_178203_6_, float p_178203_7_, float p_178203_8_)
        {
            int i = (int)((float)(p_178203_1_ >> 16 & 255) * p_178203_5_ + (float)(p_178203_2_ >> 16 & 255) * p_178203_6_ + (float)(p_178203_3_ >> 16 & 255) * p_178203_7_ + (float)(p_178203_4_ >> 16 & 255) * p_178203_8_) & 255;
            int j = (int)((float)(p_178203_1_ & 255) * p_178203_5_ + (float)(p_178203_2_ & 255) * p_178203_6_ + (float)(p_178203_3_ & 255) * p_178203_7_ + (float)(p_178203_4_ & 255) * p_178203_8_) & 255;
            return i << 16 | j;
        }
    }

    @SideOnly(Side.CLIENT)
    public static enum EnumNeighborInfo
    {
        DOWN(new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH}, 0.5F, true, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.SOUTH, CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.SOUTH}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.NORTH, CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.NORTH}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.NORTH, CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.NORTH}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.SOUTH, CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.SOUTH}),
        UP(new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH}, 1.0F, true, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.SOUTH, CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.SOUTH}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.NORTH, CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.NORTH}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.NORTH, CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.NORTH}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.SOUTH, CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.SOUTH}),
        NORTH(new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST}, 0.8F, true, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.FLIP_WEST}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.FLIP_EAST}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.FLIP_EAST}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.FLIP_WEST}),
        SOUTH(new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.UP}, 0.8F, true, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.WEST}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.FLIP_WEST, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.WEST, CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.WEST}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.EAST}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.FLIP_EAST, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.EAST, CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.EAST}),
        WEST(new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH}, 0.6F, true, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.SOUTH, CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.SOUTH}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.NORTH, CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.NORTH}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.NORTH, CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.NORTH}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.SOUTH, CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.SOUTH}),
        EAST(new EnumFacing[]{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH}, 0.6F, true, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.SOUTH, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.SOUTH}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.NORTH, CustomBlockModelRenderer.Orientation.FLIP_DOWN, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.DOWN, CustomBlockModelRenderer.Orientation.NORTH}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.NORTH, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.FLIP_NORTH, CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.NORTH}, new CustomBlockModelRenderer.Orientation[]{CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.SOUTH, CustomBlockModelRenderer.Orientation.FLIP_UP, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.FLIP_SOUTH, CustomBlockModelRenderer.Orientation.UP, CustomBlockModelRenderer.Orientation.SOUTH});

        private final EnumFacing[] corners;
        private final float shadeWeight;
        private final boolean doNonCubicWeight;
        private final CustomBlockModelRenderer.Orientation[] vert0Weights;
        private final CustomBlockModelRenderer.Orientation[] vert1Weights;
        private final CustomBlockModelRenderer.Orientation[] vert2Weights;
        private final CustomBlockModelRenderer.Orientation[] vert3Weights;
        private static final CustomBlockModelRenderer.EnumNeighborInfo[] VALUES = new CustomBlockModelRenderer.EnumNeighborInfo[6];

        private EnumNeighborInfo(EnumFacing[] p_i46236_3_, float p_i46236_4_, boolean p_i46236_5_, CustomBlockModelRenderer.Orientation[] p_i46236_6_, CustomBlockModelRenderer.Orientation[] p_i46236_7_, CustomBlockModelRenderer.Orientation[] p_i46236_8_, CustomBlockModelRenderer.Orientation[] p_i46236_9_)
        {
            this.corners = p_i46236_3_;
            this.shadeWeight = p_i46236_4_;
            this.doNonCubicWeight = p_i46236_5_;
            this.vert0Weights = p_i46236_6_;
            this.vert1Weights = p_i46236_7_;
            this.vert2Weights = p_i46236_8_;
            this.vert3Weights = p_i46236_9_;
        }

        public static CustomBlockModelRenderer.EnumNeighborInfo getNeighbourInfo(EnumFacing p_178273_0_)
        {
            return VALUES[p_178273_0_.getIndex()];
        }

        static
        {
            VALUES[EnumFacing.DOWN.getIndex()] = DOWN;
            VALUES[EnumFacing.UP.getIndex()] = UP;
            VALUES[EnumFacing.NORTH.getIndex()] = NORTH;
            VALUES[EnumFacing.SOUTH.getIndex()] = SOUTH;
            VALUES[EnumFacing.WEST.getIndex()] = WEST;
            VALUES[EnumFacing.EAST.getIndex()] = EAST;
        }
    }

    @SideOnly(Side.CLIENT)
    public static enum Orientation
    {
        DOWN(EnumFacing.DOWN, false),
        UP(EnumFacing.UP, false),
        NORTH(EnumFacing.NORTH, false),
        SOUTH(EnumFacing.SOUTH, false),
        WEST(EnumFacing.WEST, false),
        EAST(EnumFacing.EAST, false),
        FLIP_DOWN(EnumFacing.DOWN, true),
        FLIP_UP(EnumFacing.UP, true),
        FLIP_NORTH(EnumFacing.NORTH, true),
        FLIP_SOUTH(EnumFacing.SOUTH, true),
        FLIP_WEST(EnumFacing.WEST, true),
        FLIP_EAST(EnumFacing.EAST, true);

        private final int shape;

        private Orientation(EnumFacing p_i46233_3_, boolean p_i46233_4_)
        {
            this.shape = p_i46233_3_.getIndex() + (p_i46233_4_ ? EnumFacing.values().length : 0);
        }
    }

    @SideOnly(Side.CLIENT)
    static enum VertexTranslations
    {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        private final int vert0;
        private final int vert1;
        private final int vert2;
        private final int vert3;
        private static final CustomBlockModelRenderer.VertexTranslations[] VALUES = new CustomBlockModelRenderer.VertexTranslations[6];

        private VertexTranslations(int p_i46234_3_, int p_i46234_4_, int p_i46234_5_, int p_i46234_6_)
        {
            this.vert0 = p_i46234_3_;
            this.vert1 = p_i46234_4_;
            this.vert2 = p_i46234_5_;
            this.vert3 = p_i46234_6_;
        }

        public static CustomBlockModelRenderer.VertexTranslations getVertexTranslations(EnumFacing p_178184_0_)
        {
            return VALUES[p_178184_0_.getIndex()];
        }

        static
        {
            VALUES[EnumFacing.DOWN.getIndex()] = DOWN;
            VALUES[EnumFacing.UP.getIndex()] = UP;
            VALUES[EnumFacing.NORTH.getIndex()] = NORTH;
            VALUES[EnumFacing.SOUTH.getIndex()] = SOUTH;
            VALUES[EnumFacing.WEST.getIndex()] = WEST;
            VALUES[EnumFacing.EAST.getIndex()] = EAST;
        }
    }
}