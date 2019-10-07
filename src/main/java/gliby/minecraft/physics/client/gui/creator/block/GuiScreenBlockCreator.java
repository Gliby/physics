package gliby.minecraft.physics.client.gui.creator.block;

import com.google.common.collect.EvictingQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.client.gui.creator.GuiScreenCreator;
import gliby.minecraft.physics.common.blocks.IBlockGenerator;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class GuiScreenBlockCreator extends GuiScreenCreator implements GuiYesNoCallback {

    private final int maxBlocks;
    private EvictingQueue messagesList;
    private GuiButton generateButton;
    private Thread thread;
    private Physics physics;
    private int luckyTick;
    private boolean lucky;
    private int blocksBuilt;
    private int line;

    public GuiScreenBlockCreator(GuiScreen parent) {
        super(parent);
        this.physics = Physics.getInstance();
        this.messagesList = EvictingQueue.create(3);
        this.maxBlocks = ForgeRegistries.BLOCKS.getEntries().size();
        thread = new Thread("Block Generator.");
        lucky = new Random().nextInt(20000) == 0;
        blocksBuilt = 0;

        if (!physics.getBlockManager().getBlockGenerators().isEmpty())
            addMessage("Custom block generators found.");
    }

    public void onGuiClosed() {
        messagesList.clear();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void initGui() {
        buttonList.clear();
        buttonList.add(
                generateButton = new GuiButton(0, 130, 7, I18n.format("gui.creator.physicsblockGenerator.generate")));

    }

    @Override
    public String getName() {
        return I18n.format("gui.creator.physicsblockGenerator") + " (" + I18n.format("gui.creator.works") + ")";
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int consoleY = height - 42;
        drawRectangleWithOutline(18, consoleY, width - 30, 32, 1, 0xFF000000, 0xFFFFFFFF);
        for (int i = messagesList.size() - 1; i >= 0; i--) {
            String s = (String) messagesList.toArray()[i];
            drawString(fontRenderer, s, 20, consoleY + 2 + (i * (fontRenderer.FONT_HEIGHT + 1)), -1);
        }

        int modHeight = MathHelper.clamp(height - 55,
                Loader.instance().getActiveModList().size() * fontRenderer.FONT_HEIGHT + 3, Integer.MAX_VALUE);

        drawRectangleWithOutline(18, 8, 89, modHeight, 1, 0xFF000000, 0xFFFFFFFF);
        for (int i = 0; i < Loader.instance().getActiveModList().size(); i++) {
            ModContainer mod = Loader.instance().getActiveModList().get(i);
            if (i < modHeight / fontRenderer.FONT_HEIGHT) {
                drawString(fontRenderer,
                        mod.getName().substring(0, Math.min(mod.getName().length(), 15)), 20,
                        10 + i * fontRenderer.FONT_HEIGHT,
                        physics.getBlockManager().getBlockGenerators().containsKey(mod.getModId()) ? 0x207060 : -1);
            }
        }

        int loadingY = MathHelper.clamp(height - 67, 27, Integer.MAX_VALUE);
        int loadingWidth = width - 125;
        drawRectangleWithOutline(112, loadingY, loadingWidth, 20, 1, 0xFF000000, 0xFFFFFFFF);
        double scalar = ((double) blocksBuilt / (double) maxBlocks);
        if (scalar > 0)
            drawRectangleWithOutline(112, loadingY, (int) (loadingWidth * scalar), 20, 1, 0xFF449044,
                    0xFFFFFFFF);
        drawCenteredString(fontRenderer, (int) (100 * scalar) + "%", 112 + (loadingWidth / 2), loadingY + 6, -1);

        if (lucky) {
            String whatchagonnado = "¯\\_(ツ)_/¯";
            luckyTick += 42 * partialTicks;
            if (luckyTick > width * 4)
                luckyTick = -fontRenderer.getStringWidth(whatchagonnado);
            drawString(fontRenderer, whatchagonnado, luckyTick / 4, height - fontRenderer.FONT_HEIGHT, -1);

        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    Thread getBuildThread() {
        return new Thread(new Runnable() {

            @Override
            public void run() {
                Iterator<Block> itr = ForgeRegistries.BLOCKS.iterator();
                File dir = new File(Physics.getInstance().getSettings().getDirectory(), "custom");
                if (!dir.exists())
                    dir.mkdir();

                Path path = Paths.get(dir.toPath().toString() + "/blocks.zip");
                URI uri = URI.create("jar:" + path.toUri());
                Map<String, String> env = new HashMap<String, String>();
                env.put("create", "true");
                FileSystem fs = null;
                try {
                    fs = FileSystems.newFileSystem(uri, env);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                // TODO improvement: Expand Block Creation Algorithm, and add skip option
                while (itr.hasNext()) {
                    final Block block = itr.next();
                    ResourceLocation resourceLocation = ForgeRegistries.BLOCKS.getKey(block);
                    final String domain = resourceLocation.getResourceDomain();
                    final String blockID = domain + "." + resourceLocation.getResourcePath();

                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    try {
                        IBlockGenerator generator = physics.getBlockManager().getBlockGenerators().get(domain);
                        if (generator == null)
                            generator = physics.getBlockManager().getDefaultBlockGenerator();

                        Path nf = fs.getPath(blockID + ".json");
                        {
                            Writer writer = Files.newBufferedWriter(nf, StandardCharsets.UTF_8,
                                    StandardOpenOption.CREATE);
                            gson.toJson(generator.write(resourceLocation, block), writer);
                            writer.flush();
                            writer.close();
                        }
                        blocksBuilt++;
                    } catch (Exception e) {
                        stoppedBuilding(resourceLocation, e);
                        e.printStackTrace();
                    }

                }
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stoppedBuilding(null, null);
            }
        }, "Physics Block Generator");
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button == generateButton) {
            if (!thread.isAlive()) {
                generateButton.enabled = false;
                blocksBuilt = 0;
                addMessage("#################");
                addMessage("Generating " + maxBlocks + " physics blocks...");
                thread = getBuildThread();
                thread.start();
            }
        }
    }

    public void stoppedBuilding(ResourceLocation id, Exception e) {
        if (e != null) {
            addMessage("Error! Build failed because of: " + e);
            addMessage("Given error block was: " + id.getResourceDomain() + "." + id.getResourcePath());
        } else {
            addMessage("Done! .minecraft/config/glibysphysics/custom/blocks.zip");
            addMessage("Restart game to use.");
            generateButton.enabled = true;
        }
    }

    private void addMessage(String message) {
        messagesList.add("[" + line + "]: " + message);
        line++;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
