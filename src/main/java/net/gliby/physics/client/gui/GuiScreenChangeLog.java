package net.gliby.physics.client.gui;

import java.util.ArrayList;
import java.util.List;

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
			for (String s : changes) {
				int blockSize = fontRenderer.listFormattedStringToWidth(s, wrap).size();
				texts.add(new Text(s, blockSize));
			}
			pages.add(new Page(category, texts));
			// pages.add(new Page(category, changes));
			return pages;
		}

		public static class Page {

			public static class Text {
				String text;
				int blockSize;

				public Text(String s, int blockSize) {
					this.text = s;
					this.blockSize = blockSize;
				}
			}

			String category;
			List<Text> texts;

			public Page(String category, List<Text> text) {
				this.category = category;
				this.texts = text;
			}
		}

		public List<Page> getPages() {
			return pages;
		}
	}

	ArrayList<FormattedChangeLog> formattedLog;

	protected final int PAGE_DIVIDER = 20;

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
			formattedLog.add(new FormattedChangeLog(changes, fontRendererObj, 175, 5));
		}
		// }
	}

	int page;

	// texture size: 570, 382
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		mc.renderEngine.bindTexture(backgroundGui);
		int drawWidth = 412;
		int drawHeight = 412;
		int texWidth = 570;
		int texHeight = 382;
		drawModalRectWithCustomSizedTexture(width / 2 - 152 - 37, height / 2 - 105, 0, 0, drawWidth, drawHeight,
				drawWidth + 100, drawHeight);
		FormattedChangeLog changes = formattedLog.get(0);
		for (int pageIndex = 0; pageIndex < changes.getPages().size(); pageIndex++) {
			Page page = changes.getPages().get(pageIndex);
			for (int textIndex = 0; textIndex < page.texts.size(); textIndex++) {
				Text text = page.texts.get(textIndex);
				String textString = EnumChatFormatting.BOLD + "* " + EnumChatFormatting.RESET + text.text + "";
				int textY = height / 2 + ((pageIndex * textIndex) * text.blockSize * fontRendererObj.FONT_HEIGHT) - 90;
				int textX = width / 2 - 175;

				fontRendererObj.drawSplitString(textString, textX + 1, textY + 1, 175, 0);
				fontRendererObj.drawSplitString(textString, textX, textY, 175, -1);
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
