/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.client.render;

import java.util.HashMap;
import java.util.Map;

import net.gliby.gman.client.render.ItemRendererManager;
import net.gliby.physics.Physics;
import net.gliby.physics.client.render.entity.RenderPhysicsBlock;
import net.gliby.physics.client.render.entity.RenderPhysicsModelPart;
import net.gliby.physics.client.render.entity.RenderToolGunBeam;
import net.gliby.physics.client.render.items.RenderItemPhysicsGun;
import net.gliby.physics.client.render.items.RenderItemToolGun;
import net.gliby.physics.client.render.lighting.AtomicStrykerLight;
import net.gliby.physics.client.render.lighting.DummyLight;
import net.gliby.physics.client.render.lighting.IDynamicLightHandler;
import net.gliby.physics.client.render.player.RenderAdditionalPlayer;
import net.gliby.physics.client.render.world.RenderAdditionalWorld;
import net.gliby.physics.common.entity.EntityPhysicsBlock;
import net.gliby.physics.common.entity.EntityPhysicsModelPart;
import net.gliby.physics.common.entity.EntityToolGunBeam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 *
 */
public class Render {

	private static Map<String, Integer> physicsGunColors = new HashMap<String, Integer>();

	{
		physicsGunColors.put("a6a677aa-2589-4988-b686-59afd7c170f2", 0xFF87FF77);
		physicsGunColors.put("c4924b85-b249-4264-9d93-b54b1137d629", 0xFFF81A1A);
	}

	/**
	 * @return the physicsGunColors
	 */
	public static Map<String, Integer> getPhysicsGunColors() {
		return physicsGunColors;
	}

	private static IDynamicLightHandler lightHandler;

	public Render(boolean dynamicLightsPresent) {
		//TODO Re-implement some day
		if (dynamicLightsPresent) {
//			lightHandler = new AtomicStrykerLight();
		}else
			lightHandler = new DummyLight();
	}

	/**
	 * @return the lightHandler
	 */
	public static IDynamicLightHandler getLightHandler() {
		return lightHandler;
	}

	/**
	 * Called on Forge's init event, found inside @Mod interface.
	 * 
	 * @param event
	 */
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new RenderAdditionalPlayer());
		MinecraftForge.EVENT_BUS.register(new RenderAdditionalWorld());
		Minecraft mc = Minecraft.getMinecraft();

		RenderingRegistry.registerEntityRenderingHandler(EntityPhysicsBlock.class,
				new RenderPhysicsBlock(mc.getRenderManager()));
		// RenderingRegistry.registerEntityRenderingHandler(EntityPhysicsRagdoll.class,
		// new RenderPhysicsRagdoll(mc.getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityPhysicsModelPart.class,
				new RenderPhysicsModelPart(mc.getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityToolGunBeam.class, new RenderToolGunBeam(mc));
		ItemRendererManager itemRenderManager = ItemRendererManager.getInstance();

		// Create fake-model resource, doesn't need any file to function.
		// Create actual RawItemRenderer instance, simply a class that
		// "extends RawItemRenderer".
		// Register and bind to item!
		itemRenderManager.registerItemRenderer(Physics.itemPhysicsGun,
				new RenderItemPhysicsGun(new ModelResourceLocation(Physics.MOD_ID, "physicsgun")));
		itemRenderManager.registerItemRenderer(Physics.itemToolgun,
				new RenderItemToolGun(new ModelResourceLocation(Physics.MOD_ID, "toolgun")));

	}
}
