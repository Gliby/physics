/**
 * 
 */
package net.gliby.physics.client.render.player;

import javax.vecmath.Matrix4f;

import net.gliby.physics.client.render.Render;
import net.gliby.physics.client.resources.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.Util;

/**
 *
 */
public class RenderAdditionalPlayer {

	@SubscribeEvent
	public void postRender(RenderPlayerEvent.Pre post) {
	}
}
