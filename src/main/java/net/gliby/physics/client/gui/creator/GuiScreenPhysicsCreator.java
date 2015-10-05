package net.gliby.physics.client.gui.creator;

import java.util.ArrayList;
import java.util.List;

import net.gliby.physics.client.gui.creator.block.GuiScreenBlockCreator;
import net.gliby.physics.client.gui.creator.block.GuiScreenBlockCreator2;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiScreenPhysicsCreator extends GuiScreen {

	private List<GuiScreenCreator> creatorMenus = new ArrayList<GuiScreenCreator>();

	public GuiScreenPhysicsCreator(GuiScreen modList) {
		creatorMenus.add(new GuiScreenBlockCreator(this));
		creatorMenus.add(new GuiScreenBlockCreator2(this));
	}

	public void initGui() {
		buttonList.clear();
		for (int i = 0; i < creatorMenus.size(); i++) {
			GuiScreenCreator gui = creatorMenus.get(i);
			buttonList.add(new GuiButtonOpenMenu(i + 1, width / 2 - 100, 40 + (i * 25), gui.getName()).setMenu(gui));
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, I18n.format("gui.creator.title"), width / 2, 10, -1);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void updateScreen() {

	}

	@Override
	public void actionPerformed(GuiButton button) {
		if (button instanceof GuiButtonOpenMenu)
			mc.displayGuiScreen(((GuiButtonOpenMenu) button).getMenu());
	}

	private class GuiButtonOpenMenu extends GuiButton {

		private GuiScreen menu;

		public GuiScreen getMenu() {
			return menu;
		}

		public GuiButton setMenu(GuiScreen menu) {
			this.menu = menu;
			return this;
		}

		public GuiButtonOpenMenu(int buttonId, int x, int y, String buttonText) {
			super(buttonId, x, y, buttonText);
		}

		public GuiButtonOpenMenu(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
			super(buttonId, x, y, widthIn, heightIn, buttonText);
		}

	}
}
