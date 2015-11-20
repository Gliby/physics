/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.client.render.blocks;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

/**
 *
 */
public class CustomModelRenderer {

	public void renderModel(IBlockAccess world, IBlockState blockAccess, BlockPos blockPos, IBakedModel model,
			Tessellator tessellator, WorldRenderer worldRenderer, int tintIndex, float red, float green, float blue) {
		worldRenderer.startDrawingQuads();
		worldRenderer.setVertexFormat(DefaultVertexFormats.ITEM);
		EnumFacing[] aenumfacing = EnumFacing.values();
		int i = aenumfacing.length;
		for (int j = 0; j < i; ++j) {
			EnumFacing enumfacing = aenumfacing[j];
			List faceQuads = model.getFaceQuads(enumfacing);
			if (!faceQuads.isEmpty()) {
				this.renderQuads(world, blockAccess, blockPos, worldRenderer, faceQuads, tintIndex, red, green, blue);
			}
		}

		List generalQuads = model.getGeneralQuads();
		if (!generalQuads.isEmpty()) {
			this.renderQuads(world, blockAccess, blockPos, worldRenderer, generalQuads, tintIndex, red, green, blue);
		}

		tessellator.draw();
	}

	private void renderQuads(IBlockAccess world, IBlockState blockState, BlockPos blockPos, WorldRenderer worldRenderer,
			List listQuadsIn, int tintIndex, float red, float green, float blue) {
		Iterator iterator = listQuadsIn.iterator();
		while (iterator.hasNext()) {
			BakedQuad bakedquad = (BakedQuad) iterator.next();
			worldRenderer.addVertexData(bakedquad.getVertexData());
			if (bakedquad.hasTintIndex()) {
				int hexColor = bakedquad.getTintIndex();
				if (EntityRenderer.anaglyphEnable) {
					tintIndex = TextureUtil.anaglyphColor(tintIndex);
				}

				float f = (float) (tintIndex >> 16 & 255) / 255.0F;
				float f1 = (float) (tintIndex >> 8 & 255) / 255.0F;
				float f2 = (float) (tintIndex & 255) / 255.0F;
				worldRenderer.putColorMultiplier(f, f1, f2, 4);
				worldRenderer.putColorMultiplier(f, f1, f2, 3);
				worldRenderer.putColorMultiplier(f, f1, f2, 2);
				worldRenderer.putColorMultiplier(f, f1, f2, 1);
			} else
				worldRenderer.putColorRGB_F4(red, green, blue);
			worldRenderer.putNormal(bakedquad.getFace().getDirectionVec().getX(),
					bakedquad.getFace().getDirectionVec().getY(), bakedquad.getFace().getDirectionVec().getZ());
		}
	}

}