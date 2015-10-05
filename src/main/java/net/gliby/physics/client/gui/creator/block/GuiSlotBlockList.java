package net.gliby.physics.client.gui.creator.block;

import java.util.ArrayList;

import net.gliby.physics.client.gui.creator.block.GuiScreenBlockCreator2.BlockIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

public class GuiSlotBlockList extends GuiScrollingList {

	ArrayList<BlockIdentifier> blocks;

	GuiScreenBlockCreator2 parent;

	public GuiSlotBlockList(GuiScreenBlockCreator2 parent, Minecraft client, int width, int height, int top, int bottom,
			int left, int entryHeight) {
		super(client, width, height, top, bottom, left, entryHeight);
		this.parent = parent;
		blocks = new ArrayList<BlockIdentifier>();
	}

	public GuiSlotBlockList setBlockList(ArrayList<BlockIdentifier> blockIdentifiers) {
		this.blocks = blockIdentifiers;
		return this;
	}

	@Override
	protected int getSize() {
		return blocks.size();
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick) {
		parent.blockSelected(index);
	}

	@Override
	protected boolean isSelected(int index) {
		return parent.isBlockSelected(index);
	}

	@Override
	protected int getContentHeight() {
		return slotHeight * getSize();
	}

	@Override
	protected void drawBackground() {
	}

	@Override
	protected void drawSlot(int index, int var2, int var3, int var4, Tessellator var5) {
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.disableLighting(); // Forge: Make sure Lighting is
											// disabled. Fixes MC-33065

		BlockIdentifier blockId = blocks.get(index);

		this.parent.getFontRenderer().drawString(
				this.parent.getFontRenderer().trimStringToWidth(blockId.id.name, listWidth - 10), this.left + 3,
				var3 - 1, 0xFFFFFF);
	}

	public ArrayList<BlockIdentifier> getBlockList() {
		return blocks;
	}

	public int getBottom() {
		return bottom;
	}

	public int getListWidth() {
		return listWidth;
	}

}
