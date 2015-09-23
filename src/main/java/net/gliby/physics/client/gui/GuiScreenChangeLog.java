package net.gliby.physics.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.gliby.physics.Physics;
import net.gliby.physics.VersionChanges;
import net.gliby.physics.client.gui.GuiScreenChangeLog.FormattedChangeLog.Page;
import net.gliby.physics.client.gui.GuiScreenChangeLog.FormattedChangeLog.Page.Text;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GuiScreenChangeLog extends GuiScreen {

	public static class FormattedChangeLog {
		private ArrayList<Page> pages;
		private VersionChanges changes;

		public FormattedChangeLog(VersionChanges changes, FontRenderer renderer, int wrap, int blockDivider) {
			this.pages = new ArrayList<Page>();
			this.changes = changes;
			pages.addAll(divideIntoPages("Major Changes", changes.getMajorChanges(), renderer, wrap, blockDivider));
			pages.addAll(divideIntoPages("Minor Changes", changes.getMinorChanges(), renderer, wrap, blockDivider));
		}

		private ArrayList<Page> divideIntoPages(String category, List<String> changes, FontRenderer fontRenderer,
				int wrap, int blockDivider) {
			ArrayList<Page> pages = new ArrayList<Page>();
			ArrayList<Text> texts = new ArrayList<Text>();
			int blockSum = 0;
			for (String s : changes) {
				s = EnumChatFormatting.BOLD + " * " + EnumChatFormatting.RESET + s + "";
				blockSum += fontRenderer.listFormattedStringToWidth(s, wrap).size();
				texts.add(new Text(s));
			}
			pages.add(new Page(category, texts, blockSum));
			// pages.add(new Page(category, changes));
			return pages;
		}

		public static class Page {

			public static class Text {
				String text;

				public Text(String s) {
					this.text = s;
				}
			}

			String category;
			List<Text> texts;

			int blockSum;

			public Page(String category, List<Text> text, int blockSum) {
				this.category = category;
				this.texts = text;
				this.blockSum = blockSum;
			}
		}

		public List<Page> getPages() {
			return pages;
		}
	}

	ArrayList<FormattedChangeLog> formattedLog;

	protected final int PAGE_DIVIDER = 20;
	protected final int WRAP = 175;

	public GuiScreenChangeLog(ArrayList<VersionChanges> versionChanges) {
		this.versionChanges = versionChanges;
		this.formattedLog = new ArrayList<FormattedChangeLog>();
	}

	ArrayList<VersionChanges> versionChanges;

	ResourceLocation backgroundGui = new ResourceLocation(Physics.MOD_ID, "textures/gui/changelog.png");

	GuiButton next, previous, skip;

	@Override
	public void initGui() {
		// buttonList.add(next = new GuiButton(0, x, y, buttonText))
		// if (formattedLog.isEmpty()) {
		formattedLog.clear();
		for (VersionChanges changes : versionChanges) {
			formattedLog.add(new FormattedChangeLog(changes, fontRendererObj, WRAP, 5));
		}
		// }
	}

	int page;

	// texture size: 570, 382
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (Keyboard.isKeyDown(Keyboard.KEY_R))
			initGui();

		drawDefaultBackground();
		mc.renderEngine.bindTexture(backgroundGui);
		int drawWidth = 412;
		int drawHeight = 412;
		int texWidth = 570;
		int texHeight = 382;
		drawModalRectWithCustomSizedTexture(width / 2 - 152 - 37, height / 2 - 105, 0, 0, drawWidth, drawHeight,
				drawWidth + 100, drawHeight);
		FormattedChangeLog changes = formattedLog.get(0);
		for (int pageIndex = 0; pageIndex < changes.pages.size(); pageIndex++) {
			Page page = changes.getPages().get(pageIndex);
			Page prevPage = null;
			int textX = width / 2 - 175;
			drawString(fontRendererObj, EnumChatFormatting.BOLD + page.category, textX,
					height / 2 - 97 + ((page.blockSum * fontRendererObj.FONT_HEIGHT) * pageIndex), -1);

			for (int textIndex = 0; textIndex < page.texts.size(); textIndex++) {
				Text text = page.texts.get(textIndex);
				/*
				 * int textY = height / 2 + ((pageIndex * page.blockSizeSum) *
				 * fontRendererObj.FONT_HEIGHT) + ((text.blockSize * textIndex)
				 * * fontRendererObj.FONT_HEIGHT) - 85;
				 */
				int prevBlockSize = (int) (textIndex > 0
						? fontRendererObj.listFormattedStringToWidth(page.texts.get(textIndex - 1).text, WRAP).size()
						: 0);
				int prevBlockSum = prevPage != null ? prevPage.blockSum : 0;
				int textY = height / 2 - 85 + (fontRendererObj.FONT_HEIGHT * (pageIndex * prevBlockSum))
						+ (fontRendererObj.FONT_HEIGHT * textIndex) + (5) * (textIndex * prevBlockSize);
				fontRendererObj.drawSplitString(text.text, textX + 1, textY + 1, WRAP, 0);
				fontRendererObj.drawSplitString(text.text, textX, textY, WRAP, -1);
			}
			prevPage = page;
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
