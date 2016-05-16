package net.gliby.minecraft.gman.settings;

import net.gliby.minecraft.gman.settings.INIProperties.INIPropertiesReadFailure;

/**
 * 
 */

/**
 *
 */
public class BooleanSetting extends Setting {

	private Object lastData;

	/**
	 * @param data
	 * @param setting
	 */
	public BooleanSetting(String category, String name, Boolean data, Side side) {
		super(category, name, data, side);
	}

	@Override
	public void read(INIProperties ini) throws INIPropertiesReadFailure {
		data = new Boolean(ini.readBoolean(category, name, ((Boolean) data).booleanValue()));
	}

	@Override
	public void write(INIProperties ini) {
		ini.writeBoolean(category, name, ((Boolean) data).booleanValue());
		this.lastData = data;
	}

	public boolean getBooleanValue() {
		return ((Boolean) data).booleanValue();
	}

	public void setBooleanValue(boolean value) {
		this.data = new Boolean(value);
	}

	@Override
	public boolean hasChanged() {
		return lastData != data;
	}

	@Override
	public String toString() {
		return category + "." + name;
	}
}
