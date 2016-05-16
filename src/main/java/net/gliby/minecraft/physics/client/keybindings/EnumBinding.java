package net.gliby.minecraft.physics.client.keybindings;

import org.lwjgl.input.Keyboard;

enum EnumBinding {
	
	FIRE("Fire", Keyboard.KEY_F, false, KeyFireEvent.class);

	public String name;
	public int defaultBind;
	public boolean repeating;
	public Class<KeyEvent> clazz;
	EnumBinding(String name, int defaultBind, boolean repeating, Class clazz) {
		this.name = name;
		this.clazz = clazz;
		this.defaultBind = defaultBind;
		this.repeating = repeating;
	}
}
