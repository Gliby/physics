/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.client.gui;

import net.gliby.physics.Physics;
import net.gliby.physics.client.PhysicsClient;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 *
 */
public class GuiDebug {

	@SubscribeEvent
	public void render(RenderGameOverlayEvent.Text text) {
		PhysicsClient mf = Physics.getInstance().getClientProxy();
		if (text.type == ElementType.TEXT) {
			// Moved to server, client doesn't have access to these values
			// anymore :(
			// text.right.add("Physics Updates Per Seconds: " +
			// mf.getPhysicsWorld().getStepSimulator().getStepsPerSecond());
			// text.right.add("RigidBody Count: " +
			// mf.getPhysicsWorld().getStepSimulator()tTYgetRigidBodies().size());
		}
	}

}
