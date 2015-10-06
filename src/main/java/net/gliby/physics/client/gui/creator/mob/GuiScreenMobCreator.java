package net.gliby.physics.client.gui.creator.mob;

import net.gliby.physics.client.gui.creator.GuiScreenCreator;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiScreenMobCreator extends GuiScreenCreator {

	public GuiScreenMobCreator(GuiScreen parent) {
		super(parent);
	}

	@Override
	public void updateScreen() {

	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTick) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTick);
	}

	@Override
	public void actionPerformed(GuiButton button) {

	}

	@Override
	public void onGuiClosed() {

	}

	@Override
	public String getName() {
		return I18n.format("gui.creator.physicsMobModelGenerator");
	}
}
