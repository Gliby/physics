package net.gliby.physics.client.gui.creator.mob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.EvictingQueue;

import net.gliby.physics.Physics;
import net.gliby.physics.client.gui.creator.GuiScreenCreator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;


//Builds full mob model, also, uses part blacklist.
public class GuiScreenMobCreator extends GuiScreenCreator {

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

	private Physics physics;

	private List<EntityRenderInformation> mobModels;

	private class EntityRenderInformation {
		private final Class entityClass;
		private final RendererLivingEntity renderer;

		public EntityRenderInformation(Class entityClass, RendererLivingEntity renderer) {
			this.entityClass = entityClass;
			this.renderer = renderer;
		}

		public Class getEntityClass() {
			return entityClass;
		}

		public RendererLivingEntity getRenderer() {
			return renderer;
		}

	}

	public GuiScreenMobCreator(GuiScreen parent) {
		super(parent);
		this.physics = Physics.getInstance();
		this.messagesList = EvictingQueue.create(3);
		this.mobModels = new ArrayList<EntityRenderInformation>();
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		for (Object obj : renderManager.entityRenderMap.entrySet()) {
			Map.Entry<Class<? extends Entity>, RenderLiving> entry = (Map.Entry<Class<? extends Entity>, RenderLiving>) obj;
			Class<? extends Entity> entityClass = entry.getKey();
			Object renderObj = entry.getValue();
			if (renderObj instanceof RendererLivingEntity)
				mobModels.add(new EntityRenderInformation(entityClass, (RendererLivingEntity) renderObj));
		}

		thread = new Thread("Mob Generator");
		lucky = new Random().nextInt(20000) == 0;
		mobsBuilt = 0;

		if (!physics.getBlockManager().getBlockGenerators().isEmpty())
			addMessage("Custom mob generators found.");
	}

	public void initGui() {
		buttonList.clear();
		buttonList.add(generateButton = new GuiButton(0, 130, 7, I18n.format("gui.creator.mobgenerator.generate")));

	}

	private int luckyTick;
	private boolean lucky;

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		int consoleY = height - 42;
		drawRectangleWithOutline(18, consoleY, width - 30, 32, 1, 0xFF000000, 0xFFFFFFFF);
		for (int i = messagesList.size() - 1; i >= 0; i--) {
			String s = (String) messagesList.toArray()[i];
			drawString(fontRendererObj, s, 20, consoleY + 2 + (i * (fontRendererObj.FONT_HEIGHT + 1)), -1);
		}

		int modHeight = MathHelper.clamp_int(height - 55,
				Loader.instance().getActiveModList().size() * fontRendererObj.FONT_HEIGHT + 3, Integer.MAX_VALUE);

		drawRectangleWithOutline(18, 8, 89, modHeight, 1, 0xFF000000, 0xFFFFFFFF);
		for (int i = 0; i < Loader.instance().getActiveModList().size(); i++) {
			ModContainer mod = Loader.instance().getActiveModList().get(i);
			if (i < modHeight / fontRendererObj.FONT_HEIGHT) {
				drawString(fontRendererObj,
						mod.getName().substring(0, mod.getName().length() > 15 ? 15 : mod.getName().length()), 20,
						10 + i * fontRendererObj.FONT_HEIGHT,
						physics.getBlockManager().getBlockGenerators().containsKey(mod.getModId()) ? 0x207060 : -1);
			}
		}

		int loadingY = MathHelper.clamp_int(height - 67, 27, Integer.MAX_VALUE);
		int loadingWidth = width - 125;
		drawRectangleWithOutline(112, loadingY, loadingWidth, 20, 1, 0xFF000000, 0xFFFFFFFF);
		double scalar = ((double) mobsBuilt / (double) mobModels.size());
		if (scalar > 0)
			drawRectangleWithOutline(112, loadingY, (int) (loadingWidth * (double) scalar), 20, 1, 0xFF449044,
					0xFFFFFFFF);
		drawCenteredString(fontRendererObj, (int) (100 * scalar) + "%", 112 + (loadingWidth / 2), loadingY + 6, -1);

		if (lucky) {
			String whatchagonnado = "¯\\_(ツ)_/¯";
			luckyTick += 88 * partialTicks;
			if (luckyTick > width * 4)
				luckyTick = -fontRendererObj.getStringWidth(whatchagonnado);
			drawString(fontRendererObj, whatchagonnado, luckyTick / 4, height - fontRendererObj.FONT_HEIGHT, -1);

		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private int mobsBuilt;

	
	
	//TODO Use entity unique id for identification.
	
	
	Thread getBuildThread() {
		return new Thread(new Runnable() {

			@Override
			public void run() {
				stoppedBuilding(null);
			}
		}, "Mob Generator");
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if (button == generateButton) {
			if (!thread.isAlive()) {
				generateButton.enabled = false;
				mobsBuilt = 0;
				addMessage("#################");
				addMessage("Generating " + mobModels.size() + " mobs...");
				thread = getBuildThread();
				thread.start();
			}
		}
	}

	public void stoppedBuilding(Exception e) {
		if (e != null) {
			addMessage("Error! Build failed because of: " + e);
		} else {
			addMessage("Done! .minecraft/config/glibysphysics/custom/mobs.zip");
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

	@Override
	public String getName() {
		return I18n.format("gui.creator.physicsMobModelGenerator");
	}
}
