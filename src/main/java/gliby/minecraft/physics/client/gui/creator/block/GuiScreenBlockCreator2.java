package gliby.minecraft.physics.client.gui.creator.block;

import com.google.common.base.Strings;
import gliby.minecraft.physics.client.gui.creator.GuiScreenCreator;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Original @author cpw, modified by @author Gliby
 */
public class GuiScreenBlockCreator2 extends GuiScreenCreator {
    boolean editing;
    BlockIdentifier selectedIdentifier;
    int blockSelectedIndex;
    private GuiBlockSlotModList modList;
    private int selected = -1;
    private ModContainer selectedMod;
    private int listWidth;
    private ArrayList<ModContainer> mods;
    private ResourceLocation cachedLogo;
    private Dimension cachedLogoDimensions;
    private int buttonMargin = 1;
    private int numButtons = SortType.values().length;
    private String lastFilterText = "";
    private GuiTextField modSearch;
    private GuiTextField blockSearch;
    private boolean sorted = false;
    private SortType sortType = SortType.NORMAL;
    private ModContainer firstModContainer;
    private Map<ModContainer, ArrayList<BlockIdentifier>> blockRegistry;
    private GuiSlotBlockList blockList;

    /**
     * @param parent
     */
    public GuiScreenBlockCreator2(GuiScreen parent) {
        super(parent);
        this.mods = new ArrayList<ModContainer>();
        FMLClientHandler.instance().addSpecialModEntries(mods);
        // Add child mods to their parent's list
        for (ModContainer mod : Loader.instance().getModList()) {
            if (mod.getMetadata() != null && mod.getMetadata().parentMod == null
                    && !Strings.isNullOrEmpty(mod.getMetadata().parent)) {
                String parentMod = mod.getMetadata().parent;
                ModContainer parentContainer = Loader.instance().getIndexedModList().get(parentMod);
                if (parentContainer != null) {
                    mod.getMetadata().parentMod = parentContainer;
                    parentContainer.getMetadata().childMods.add(mod);
                    continue;
                }
            } else if (mod.getMetadata() != null && mod.getMetadata().parentMod != null) {
                continue;
            }
            if (firstModContainer == null)
                firstModContainer = mod;
            mods.add(mod);
        }

        this.blockRegistry = new HashMap<ModContainer, ArrayList<BlockIdentifier>>();
        Iterator<Block> itr = Block.blockRegistry.iterator();
        while (itr.hasNext()) {
            final Block block = itr.next();
            UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(block);
            ModContainer modContainer = FMLCommonHandler.instance().findContainerFor(block);
            modContainer = modContainer != null ? modContainer : firstModContainer;
            ArrayList<BlockIdentifier> blockList = blockRegistry.get(modContainer) != null
                    ? blockRegistry.get(modContainer) : new ArrayList<BlockIdentifier>();
            blockList.add(new BlockIdentifier(id, block));
            blockRegistry.put(modContainer, blockList);
        }
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        for (ModContainer mod : mods) {
            listWidth = Math.max(listWidth, getFontRenderer().getStringWidth(mod.getName()) + 10);
            listWidth = Math.max(listWidth, getFontRenderer().getStringWidth(mod.getVersion()) + 10);
        }
        listWidth = Math.min(listWidth, 150);
        this.modList = new GuiBlockSlotModList(this, mods, listWidth);
        this.modList.registerScrollButtons(this.buttonList, 7, 8);

        this.blockList = new GuiSlotBlockList(this, mc, 100, 0, 32, height - 85, 140, fontRendererObj.FONT_HEIGHT);
        this.blockList.registerScrollButtons(this.buttonList, 5, 6);

        this.buttonList.add(new GuiButton(6, ((modList.getRight() + this.width) / 2) - 100, this.height - 38,
                I18n.format("gui.done")));

        blockSearch = new GuiTextField(10, getFontRenderer(), 142, blockList.getBottom() + 17,
                blockList.getListWidth() - 4, 14);
        blockSearch.setFocused(true);
        blockSearch.setCanLoseFocus(true);

        modSearch = new GuiTextField(0, getFontRenderer(), 12, modList.getBottom() + 17, modList.getListWidth() - 4,
                14);
        modSearch.setFocused(true);
        modSearch.setCanLoseFocus(true);

        int width = (modList.getListWidth() / numButtons);
        int x = 10, y = 10;
        GuiButton normalSort = new GuiButton(SortType.NORMAL.buttonID, x, y, width - buttonMargin, 20,
                I18n.format("fml.menu.mods.normal"));
        normalSort.enabled = false;
        buttonList.add(normalSort);
        x += width + buttonMargin;
        buttonList.add(new GuiButton(SortType.A_TO_Z.buttonID, x, y, width - buttonMargin, 20, "A-Z"));
        x += width + buttonMargin;
        buttonList.add(new GuiButton(SortType.Z_TO_A.buttonID, x, y, width - buttonMargin, 20, "Z-A"));
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        if (!editing) {
            modSearch.mouseClicked(x, y, button);
            if (button == 1 && x >= modSearch.xPosition && x < modSearch.xPosition + modSearch.width
                    && y >= modSearch.yPosition && y < modSearch.yPosition + modSearch.height) {
                modSearch.setText("");
            }
        } else {
            blockSearch.mouseClicked(x, y, button);
        }
    }

    /**
     * Fired when a key is typed (except F11 who toggle full screen). This is
     * the equivalent of KeyListener.keyTyped(KeyEvent e). Args : character
     * (character on the key), keyCode (lwjgl Keyboard key code)
     */
    @Override
    protected void keyTyped(char c, int keyCode) throws IOException {
        super.keyTyped(c, keyCode);
        modSearch.textboxKeyTyped(c, keyCode);
        if (editing)
            blockSearch.textboxKeyTyped(c, keyCode);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen() {
        super.updateScreen();
        modSearch.updateCursorCounter();
        if (editing)
            blockSearch.updateCursorCounter();
        if (!modSearch.getText().equals(lastFilterText)) {
            reloadMods();
            sorted = false;
        }

        if (!sorted) {
            reloadMods();
            Collections.sort(mods, sortType);
            selected = modList.setSelectedIndex(mods.indexOf(selectedMod));
            sorted = true;
        }

        this.modList.setCanMove(!editing);
    }

    private void reloadMods() {
        ArrayList<ModContainer> mods = modList.getMods();
        mods.clear();
        for (ModContainer m : Loader.instance().getActiveModList()) {
            // If it passes the filter, and is not a child mod
            if (m.getName().toLowerCase().contains(modSearch.getText().toLowerCase())
                    && m.getMetadata().parentMod == null) {
                mods.add(m);
            }
        }
        this.mods = mods;
        lastFilterText = modSearch.getText();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            SortType type = SortType.getTypeForButton(button);

            if (type != null) {
                for (GuiButton b : (List<GuiButton>) buttonList) {
                    if (SortType.getTypeForButton(b) != null) {
                        b.enabled = true;
                    }
                }
                button.enabled = false;
                sorted = false;
                sortType = type;
                this.mods = modList.getMods();
            } else {
                switch (button.id) {
                    case 6:
                        this.mc.displayGuiScreen(parent);
                        return;
                    case 20:
                        try {
                            IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(selectedMod);
                            GuiScreen newScreen = guiFactory.mainConfigGuiClass().getConstructor(GuiScreen.class)
                                    .newInstance(this);
                            this.mc.displayGuiScreen(newScreen);
                        } catch (Exception e) {
                            FMLLog.log(Level.ERROR, e, "There was a critical issue trying to build the config GUI for %s",
                                    selectedMod.getModId());
                        }
                        return;
                }
            }
        }
        super.actionPerformed(button);
    }

    public int drawLine(String line, int offset, int shifty) {
        this.fontRendererObj.drawString(line, offset, shifty, 0xd7edea);
        return shifty + 10;
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY,
     * renderPartialTicks
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        super.drawScreen(mouseX, mouseY, partialTick);
        this.modList.drawScreen(mouseX, mouseY, partialTick);
        String text = I18n.format("fml.menu.mods.search");
        int x = ((10 + modList.getRight()) / 2) - (getFontRenderer().getStringWidth(text) / 2);
        getFontRenderer().drawString(text, x, modList.getBottom() + 5, 0xFFFFFF);
        modSearch.drawTextBox();

        this.drawCenteredString(this.fontRendererObj, "Mod List", this.width / 2, 16, 0xFFFFFF);
        int offset = this.listWidth + 20;
        if (selectedMod != null) {
            ArrayList<BlockIdentifier> blocks = blockRegistry.get(selectedMod);
            if (blocks != null) {
                editing = true;
                this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
                int xx = 140;
                int yy = 32;
                int xWidth = xx + 100;
                int yHeight = (height - 85);
                if (blockList.getBlockList() != blocks) {
                    blockSelectedIndex = 0;
                    blockList.setBlockList(blocks);
                }
                startGlScissor(mc, xx, yy, xWidth, yHeight - 32);

                blockList.drawScreen(mouseX, mouseY, partialTick);
                endGlScissor();

                int xSearch = ((250 + modList.getRight()) / 2) - (getFontRenderer().getStringWidth(text) / 2);
                getFontRenderer().drawString(text, xSearch, modList.getBottom() + 5, 0xFFFFFF);
                blockSearch.drawTextBox();
                /*
                 * drawRectangleWithOutline(140, 32, 100, 139, 1, 0xFF000000,
                 * 0xFFFFFFFF); for (int i = 0; i < blocks.size(); i++) {
                 * BlockIdentifier block = blocks.get(i); if (i < (140 /
                 * fontRendererObj.FONT_HEIGHT)) { drawString(fontRendererObj,
                 * block.id.name, 142, 34 + (i * fontRendererObj.FONT_HEIGHT),
                 * -1); } }
                 */
            } else {
                editing = false;
                drawString(fontRendererObj, "No blocks found :(", 140, 33, -1);
            }
        } else {
        }
    }

    Minecraft getMinecraftInstance() {
        /** Reference to the Minecraft object. */
        return mc;
    }

    FontRenderer getFontRenderer() {
        /** The FontRenderer used by GuiScreen */
        return fontRendererObj;
    }

    public void selectModIndex(int index) {
        this.selected = index;
        this.selectedMod = (index >= 0 && index <= mods.size()) ? mods.get(selected) : null;
        cachedLogo = null;
    }

    public boolean modIndexSelected(int index) {
        return index == selected;
    }

    @Override
    public String getName() {
        return I18n.format("gui.creator.physicsblockGenerator") + " (" + I18n.format("gui.creator.unfinished") + ")";
    }

    public void blockSelected(int index) {
        this.blockSelectedIndex = index;
    }

    public boolean isBlockSelected(int index) {
        return index == blockSelectedIndex;
    }

    public ArrayList<BlockIdentifier> getBlockIdentifiers(ModContainer mc) {
        return blockRegistry.get(mc);
    }

    private enum SortType implements Comparator<ModContainer> {
        NORMAL(24), A_TO_Z(25) {
            @Override
            protected int compare(String name1, String name2) {
                return name1.compareTo(name2);
            }
        },
        Z_TO_A(26) {
            @Override
            protected int compare(String name1, String name2) {
                return name2.compareTo(name1);
            }
        };

        private int buttonID;

        SortType(int buttonID) {
            this.buttonID = buttonID;
        }

        public static SortType getTypeForButton(GuiButton button) {
            for (SortType t : values()) {
                if (t.buttonID == button.id) {
                    return t;
                }
            }
            return null;
        }

        protected int compare(String name1, String name2) {
            return 0;
        }

        @Override
        public int compare(ModContainer o1, ModContainer o2) {
            String name1 = StringUtils.stripControlCodes(o1.getName()).toLowerCase();
            String name2 = StringUtils.stripControlCodes(o2.getName()).toLowerCase();
            return compare(name1, name2);
        }
    }

    public class BlockIdentifier {
        public UniqueIdentifier id;
        public Block block;

        public BlockIdentifier(UniqueIdentifier id, Block block) {
            super();
            this.id = id;
            this.block = block;
        }

        public UniqueIdentifier getId() {
            return id;
        }

        public void setId(UniqueIdentifier id) {
            this.id = id;
        }

        public Block getBlock() {
            return block;
        }

        public void setBlock(Block block) {
            this.block = block;
        }
    }
}