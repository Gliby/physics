/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.client.keybindings;

import net.gliby.physics.Physics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.world.World;

/**
 *
 */
public class KeyFireEvent extends KeyEvent {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.minefortress.client.keybindings.KeyEvent#keyDown(net.minecraft.client
	 * .settings.KeyBinding, boolean, boolean)
	 */
	@Override
	public void keyDown(KeyBinding kb, boolean tickEnd, boolean isRepeat) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.minefortress.client.keybindings.KeyEvent#keyUp(net.minecraft.client
	 * .settings.KeyBinding, boolean)
	 */
	@Override
	public void keyUp(KeyBinding kb, boolean tickEnd) {
		World world = null;
		if ((world = Minecraft.getMinecraft().theWorld) != null) {
			Physics.getInstance().getClientProxy().getPhysicsOverWorld().debugSpawn(world);
		}
	}

	@Override
	public EnumBinding getEnumBinding() {
		return EnumBinding.FIRE;
	}
}
