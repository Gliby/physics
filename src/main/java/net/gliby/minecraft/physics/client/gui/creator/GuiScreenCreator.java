package net.gliby.minecraft.physics.client.gui.creator;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public abstract class GuiScreenCreator extends GuiScreen {

	public GuiScreenCreator(GuiScreen parent) {
		this.parent = parent;
	}

	protected GuiScreen parent;

	public abstract String getName();

	public void drawRectangleWithOutline(int x, int y, int width, int height, int outlineSize, int colorInside,
			int colorOutside) {
		drawRect(x, y, x + width, y + height, colorOutside);
		drawRect(x + outlineSize, y + outlineSize, x + width - outlineSize, y + height - outlineSize, colorInside);
	}

	public boolean inBounds(int x, int y, int boundX, int boundY, int boundWidth, int boundHeight) {
		return x >= boundX && y >= boundY && x < boundWidth && y < boundHeight;
	}

	public static void startGlScissor(Minecraft mc, int x, int y, int width, int height) {
		ScaledResolution scaledRes = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

		double scaleW = (double) mc.displayWidth / scaledRes.getScaledWidth_double();
		double scaleH = (double) mc.displayHeight / scaledRes.getScaledHeight_double();

		GL11.glEnable(GL11.GL_SCISSOR_TEST);

		GL11.glScissor((int) Math.floor((double) x * scaleW),
				(int) Math.floor((double) mc.displayHeight - ((double) (y + height) * scaleH)),
				(int) Math.floor((double) (x + width) * scaleW) - (int) Math.floor((double) x * scaleW),
				(int) Math.floor((double) mc.displayHeight - ((double) y * scaleH))
						- (int) Math.floor((double) mc.displayHeight - ((double) (y + height) * scaleH)));
	}

	public static void endGlScissor() {
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
}
