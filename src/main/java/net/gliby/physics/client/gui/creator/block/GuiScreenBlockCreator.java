package net.gliby.physics.client.gui.creator.block;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.EvictingQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.gliby.physics.Physics;
import net.gliby.physics.client.gui.creator.GuiScreenCreator;
import net.gliby.physics.common.blocks.IBlockGenerator;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;

public class GuiScreenBlockCreator extends GuiScreenCreator {

	private EvictingQueue messagesList;
	private GuiButton generateButton;
	private Thread thread;

	public void onGuiClosed() {
		messagesList.clear();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private final int maxBlocks;
	private Physics physics;

	public GuiScreenBlockCreator(GuiScreen parent) {
		super(parent);
		this.physics = Physics.getInstance();
		this.messagesList = EvictingQueue.create(3);
		this.maxBlocks = Block.blockRegistry.getKeys().size();
		blocksBuilt = 0;
		thread = new Thread();
		lucky = new Random().nextInt(20000) == 0;
	}

	public void initGui() {
		messagesList.clear();
		buttonList.clear();
		buttonList.add(
				generateButton = new GuiButton(0, 130, 7, I18n.format("gui.creator.physicsblockGenerator.generate")));
		if (!physics.getBlockManager().getBlockGenerators().isEmpty())
			addMessage("Custom block generators found.");

	}

	@Override
	public String getName() {
		return I18n.format("gui.creator.physicsblockGenerator") + " (" + I18n.format("gui.creator.works") + ")";
	}

	private int luckyTick;
	private boolean lucky;

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawRectangleWithOutline(18, 213, width - 30, 32, 1, 0xFF000000, 0xFFFFFFFF);
		for (int i = messagesList.size() - 1; i >= 0; i--) {
			String s = (String) messagesList.toArray()[i];
			drawString(fontRendererObj, s, 20, 215 + (i * (fontRendererObj.FONT_HEIGHT + 1)), -1);
		}

		drawRectangleWithOutline(18, 8, 89, 200, 1, 0xFF000000, 0xFFFFFFFF);
		for (int i = 0; i < Loader.instance().getActiveModList().size(); i++) {
			ModContainer mod = Loader.instance().getActiveModList().get(i);
			if (i < 200 / fontRendererObj.FONT_HEIGHT) {
				drawString(fontRendererObj,
						mod.getName().substring(0, mod.getName().length() > 15 ? 15 : mod.getName().length()), 20,
						10 + i * fontRendererObj.FONT_HEIGHT,
						physics.getBlockManager().getBlockGenerators().containsKey(mod.getModId()) ? 0x207060 : -1);
			}
		}
		drawRectangleWithOutline(112, 188, width - 125, 20, 1, 0xFF000000, 0xFFFFFFFF);
		double scalar = ((double) blocksBuilt / (double) maxBlocks);
		if (scalar > 0)
			drawRectangleWithOutline(112, 188, (int) ((width - 125) * (double) scalar), 20, 1, 0xFF449044, 0xFFFFFFFF);
		drawString(fontRendererObj, (int) (100 * scalar) + "%", 275, 194, -1);
		if (lucky) {
			String whatchagonnado = "¯\\_(ツ)_/¯";
			luckyTick += 88 * partialTicks;
			if (luckyTick > width * 4)
				luckyTick = -fontRendererObj.getStringWidth(whatchagonnado);
			drawString(fontRendererObj, whatchagonnado, luckyTick / 4, height - fontRendererObj.FONT_HEIGHT, -1);

		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private int blocksBuilt;

	Thread getBuildThread() {
		return new Thread(new Runnable() {

			@Override
			public void run() {
				Iterator<Block> itr = Block.blockRegistry.iterator();
				File dir = new File(Physics.getInstance().getSettings().getDirectory(), "custom");
				if (!dir.exists())
					dir.mkdir();

				Path path = Paths.get(dir.toPath().toString() + "/blocks.zip");
				URI uri = URI.create("jar:" + path.toUri());
				Map<String, String> env = new HashMap<>();
				env.put("create", "true");
				FileSystem fs = null;
				try {
					fs = FileSystems.newFileSystem(uri, env);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				// TODO Expand
				while (itr.hasNext()) {
					final Block block = itr.next();
					UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(block);
					final String blockID = id.modId + "." + id.name;

					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					try {
						IBlockGenerator generator = physics.getBlockManager().getBlockGenerators().get(id.modId);
						if (generator == null)
							generator = physics.getBlockManager().getDefaultBlockGenerator();
						Path nf = fs.getPath(blockID + ".json");
						{
							Writer writer = Files.newBufferedWriter(nf, StandardCharsets.UTF_8,
									StandardOpenOption.CREATE);
							gson.toJson(generator.write(id, block), writer);
							writer.flush();
							writer.close();
						}
						blocksBuilt++;
					} catch (Exception e) {
						stoppedBuilding(id, e);
						e.printStackTrace();
					}

				}
				stoppedBuilding(null, null);

				try {
					fs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
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

	public synchronized void stoppedBuilding(UniqueIdentifier id, Exception e) {
		if (e != null) {
			addMessage("Error! Build failed because of: " + e);
			addMessage("Given error block was: " + id.modId + "." + id.name);
		} else {
			addMessage("Done! .minecraft/config/glibysphysics/custom/blocks.zip");
			addMessage("Restart game to use.");
			generateButton.enabled = true;
		}
	}

	private int line;

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
