/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.game.items.toolgun.actions;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 *
 */
public class ToolGunActionEvent extends Event {

	private Object action;

	public ToolGunActionEvent(Object action) {
		this.action = action;
	}

	public static class Register extends ToolGunActionEvent {

		private boolean isDefault;

		Register(Object action, boolean isDefault) {
			super(action);
			this.isDefault = isDefault;
		}
	}

}
