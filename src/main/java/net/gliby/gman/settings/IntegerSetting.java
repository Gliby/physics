/**
 * 
 */
package net.gliby.gman.settings;

import net.gliby.gman.settings.INIProperties.INIPropertiesReadFailure;
import net.minecraftforge.fml.relauncher.Side;

/**
 *
 */
public class IntegerSetting extends Setting {

	/**
	 * @param setting
	 * @param name
	 */
	public IntegerSetting(String category, String name, Integer setting, Side side) {
		super(category, name, setting, side);
	}

	@Override
	public void read(INIProperties ini) throws INIPropertiesReadFailure {
		data = new Integer(
				ini.readInteger(category, name, ((Integer)data).intValue()));
	}

	@Override
	public void write(INIProperties ini) {
		ini.writeInteger(category, name, ((Integer) data).intValue());
		lastData = data;
	}

	public int getIntValue() {
		return ((Integer) data).intValue();
	}

	public void setIntValue(int value) {
		this.data = new Integer(value);
	}

	private Object lastData;

	@Override
	public boolean hasChanged() {
		return data != lastData;
	}
}
