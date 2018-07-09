/**
 * 
 */
package gliby.minecraft.gman.settings;

import com.google.gson.Gson;

import net.minecraftforge.fml.relauncher.Side;

/**
 * Use JSON!
 */
public class ObjectSetting extends Setting {
	private Class objectClass;
	private static Gson gson = new Gson();

	/**
	 * @param setting
	 * @param name
	 * @param objectClass
	 */
	ObjectSetting(String category, String name, Object setting, Class objectClass, Side side) {
		super(category, name, setting, side);
		this.objectClass = objectClass;
	}

	@Override
	public void read(INIProperties ini) {
		data = gson.fromJson(ini.readString(category, name, gson.toJson(data)), objectClass);
	}

	private Object lastData;

	@Override
	public void write(INIProperties ini) {
		ini.writeString(category, name, gson.toJson(data, objectClass));
		lastData = data;
	}

	@Override
	public boolean hasChanged() {
		return data != lastData;
	}

}
