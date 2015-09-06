/**
 * 
 */
package net.gliby.gman.settings;

import net.minecraftforge.fml.relauncher.Side;

/**
 *
 */
public class StringSetting extends Setting {

	/**
	 * @param setting
	 * @param name
	 */
	StringSetting(String category, String name, String setting, Side side) {
		super(category, name, setting, side);
	}

	@Override
	public void read(INIProperties ini) {
		data = new String(ini.readString(category, name, ((String) data)));
	}

	@Override
	public void write(INIProperties ini) {
		ini.writeString(category, name, ((String) data));
		lastData = data;
	}

	private Object lastData;

	public String getString() {
		return (String) data;
	}

	@Override
	public boolean hasChanged() {
		return data != lastData;
	}

}
