package net.gliby.physics.client.keybindings;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class KeyEvent {

	public KeyBinding forgeKeyBinding;
	public EnumBinding keyBind;
	public int keyID = -1;
	public boolean repeating;

	public KeyEvent() {
		this.keyBind = getEnumBinding();
		this.keyID = getEnumBinding().defaultBind;
		this.repeating = getEnumBinding().repeating;
	}

	public abstract void keyDown(KeyBinding kb, boolean tickEnd, boolean isRepeat);

	public abstract void keyUp(KeyBinding kb, boolean tickEnd);

	
	public abstract EnumBinding getEnumBinding();
}
