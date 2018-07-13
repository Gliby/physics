package gliby.minecraft.physics.client.gui;

import gliby.minecraft.physics.Physics;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class GuiScreenWelcome extends GuiScreen {

	ResourceLocation backgroundGui = new ResourceLocation(Physics.ID, "textures/gui/welcome_gui.png");

	public void initGui() {
	}

	// texture size: 570, 382
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		mc.renderEngine.bindTexture(backgroundGui);
		drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 0, 0, 256, 256);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
