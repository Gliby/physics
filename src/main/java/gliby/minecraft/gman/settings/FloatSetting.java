package gliby.minecraft.gman.settings;

import gliby.minecraft.gman.settings.INIProperties.INIPropertiesReadFailure;

/**
 *
 */
public class FloatSetting extends Setting {

	/**
	 * @param setting
	 * @param name
	 */
	public FloatSetting(String category, String name, Float setting, Side side) {
		super(category, name, setting, side);
	}

	@Override
	public void read(INIProperties ini) throws INIPropertiesReadFailure {
		data = new Float(ini.readFloat(category, name, (Float) data));
	}

	@Override
	public void write(INIProperties ini) {
		ini.writeFloat(category, name, ((Float) data).floatValue());
		this.lastData = data;
	}

	public float getFloatValue() {
		return ((Float) data).floatValue();
	}

	public void setFloatValue(float value) {
		this.data = new Float(value);
	}

	private Object lastData;

	@Override
	public boolean hasChanged() {
		return data != lastData;
	}
}
