package gliby.minecraft.physics.client.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.VersionChanges;
import gliby.minecraft.physics.client.gui.GuiScreenChangeLog.FormattedChangeLog.Page;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import scala.reflect.api.Internals.ReificationSupportApi.SyntacitcSingletonTypeExtractor;

public class GuiScreenChangeLog extends GuiScreen {

	public static class FormattedChangeLog {
		private ArrayList<Page> pages;
		private VersionChanges changes;

		public FormattedChangeLog(VersionChanges changes, FontRenderer renderer, int wrap) {
			this.pages = new ArrayList<Page>();
			this.changes = changes;
			addPage(changes.getChanges(), renderer, wrap);
		}

		private ArrayList<Page> addPage(List<String> changes, FontRenderer fontRenderer, int wrap) {
			ArrayList<String> texts = new ArrayList<String>();
			for (String s : changes) {
				if (!s.startsWith(PREFIX_TITLE)) {
					s = " * " + s + "";
				}
				texts.addAll(fontRenderer.listFormattedStringToWidth(s, wrap));
				textLength += s.length();
			}
			pages.add(new Page(texts));
			return pages;
		}

		public static class Page {

			List<String> texts;

			public Page(List<String> text) {
				this.texts = text;
			}
		}

		public List<Page> getPages() {
			return pages;
		}

		private int textLength;

		public int getTextLength() {
			return textLength;
		}
	}

	private ArrayList<FormattedChangeLog> formattedLog;
	private ArrayList<VersionChanges> versionChanges;

	private GuiButton nextButton, previousOrSkipButton;

	private final static String PREFIX_TITLE = "!title";

	private static final ResourceLocation BACKGROUND = new ResourceLocation(Physics.MOD_ID,
			"textures/gui/changelog.png");

	private static final ResourceLocation NO_IMAGE = new ResourceLocation(Physics.MOD_ID,
			"textures/gui/defaultchangelog.png");

	private static final int WRAP = 175;

	public GuiScreenChangeLog(ArrayList<VersionChanges> versionChanges) {
		this.versionChanges = versionChanges;
		this.formattedLog = new ArrayList<FormattedChangeLog>();
	}

	@Override
	public void initGui() {
		buttonList.clear();
		formattedLog.clear();
		for (VersionChanges changes : versionChanges) {
			formattedLog.add(new FormattedChangeLog(changes, fontRendererObj, WRAP));
		}
		createButtons();
	}

	private void createButtons() {
		scrollY = 0;
		buttonList.clear();
		buttonList.add(nextButton = new GuiButton(0, width / 2 - 89, height / 2 + 62, 89, 20,
				currentPage == formattedLog.size() - 1 ? I18n.format("gui.done") : I18n.format("gui.changelog.next")));
		buttonList.add(previousOrSkipButton = new GuiButton(1, width / 2 - 177, height / 2 + 62, 87, 20,
				currentPage == 0 ? I18n.format("gui.changelog.skip") : I18n.format("gui.changelog.previous")));
	}

	private static final int DRAW_WIDTH = 412;
	private static final int DRAW_HEIGHT = 412;
	private static final int TEXTURE_WIDTH = 570;
	private static final int TEXTURE_HEIGHT = 382;

	private int currentPage;
	private int scrollY;

	// Image size is 344, 377
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		mc.renderEngine.bindTexture(BACKGROUND);
		drawModalRectWithCustomSizedTexture(width / 2 - 152 - 37, height / 2 - 105, 0, 0, DRAW_WIDTH, DRAW_HEIGHT,
				DRAW_WIDTH + 100, DRAW_HEIGHT);
		FormattedChangeLog changes = getCurrentLog();
		Page page = changes.getPages().get(0);
		/*
		 * drawString(fontRendererObj, EnumChatFormatting.BOLD + page.category,
		 * textX, height / 2 - 97 + ((page.blockSizeSum *
		 * fontRendererObj.FONT_HEIGHT) * pageIndex), -1);
		 */

		if (changes.changes.getVersionImage(mc.getTextureManager()) != null)
			mc.renderEngine.bindTexture(changes.changes.getVersionImage(mc.getTextureManager()));
		else
			mc.renderEngine.bindTexture(NO_IMAGE);
		final float scale = 0.54f;
		final float scaleY = 0.53845f;
		GlStateManager.pushMatrix();
		GlStateManager.translate(width / 2 + 2F, height / 2 - 104F, 0);
		drawModalRectWithCustomSizedTexture(0, 0, 0, 0, (int) (344 * scale), (int) (377 * scaleY), (int) (344 * scale),
				(int) (377 * scaleY));
		GlStateManager.popMatrix();
		int boundX = width / 2 - 184;
		int boundY = height / 2 - 100;
		int boundWidth = boundX + 185;
		int boundHeight = boundY + 154;
		String version = EnumChatFormatting.BOLD + changes.changes.version;
		drawString(fontRendererObj, version, boundWidth - (15 + (fontRendererObj.getStringWidth(version) / 2)),
				boundY + 6, -1);

		for (int textIndex = 0; textIndex < page.texts.size(); textIndex++) {
			String text = page.texts.get(textIndex);
			int textX = width / 2 - 175;
			int textY = height / 2 - 90 + (textIndex * fontRendererObj.FONT_HEIGHT) + scrollY;
			if (inBounds(textX, textY, boundX, boundY, boundWidth, boundHeight)) {
				if (text.startsWith(PREFIX_TITLE)) {
					text = text.substring(PREFIX_TITLE.length(), text.length());
					textX -= 4;
					textY -= 4;
				}

				/*
				 * int textY = height / 2 + ((pageIndex * page.blockSizeSum) *
				 * fontRendererObj.FONT_HEIGHT) + ((text.blockSize * textIndex)
				 * * fontRendererObj.FONT_HEIGHT) - 85;
				 */
				fontRendererObj.drawSplitString(text, textX + 1, textY + 1, WRAP, 0);
				fontRendererObj.drawSplitString(text, textX, textY, WRAP, -1);
			}
		}
		drawCenteredString(fontRendererObj, I18n.format("gui.changelog.title"), width / 2, height / 2 - 120, -1);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == nextButton.id) {
			if (currentPage < formattedLog.size() - 1) {
				currentPage++;
				createButtons();
			} else if (currentPage == formattedLog.size() - 1) {
				mc.displayGuiScreen(null);
			}
		} else if (button.id == previousOrSkipButton.id) {
			if (currentPage == 0) {
				mc.displayGuiScreen(null);
			} else {
				if (currentPage >= 0)
					currentPage--;
				else
					currentPage = 0;
				createButtons();
			}
		}
	}

	private FormattedChangeLog getCurrentLog() {
		return formattedLog.get(currentPage);
	}

	public boolean inBounds(int x, int y, int boundX, int boundY, int boundWidth, int boundHeight) {
		return x >= boundX && y >= boundY && x < boundWidth && y < boundHeight;
	}

	@Override
	public void handleMouseInput() throws IOException {
		int mouseWheelDelta = Mouse.getEventDWheel();
		if (mouseWheelDelta != 0) {
			if (mouseWheelDelta > 0) {
				mouseWheelDelta = -1;
			} else if (mouseWheelDelta < 0) {
				mouseWheelDelta = 1;
			}

			this.scrollY += (float) (mouseWheelDelta * fontRendererObj.FONT_HEIGHT);
		}
		super.handleMouseInput();
	}

}
