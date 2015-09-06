/**
 * 
 */
package net.gliby.gman.settings;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

import net.gliby.gman.settings.INIProperties.INIPropertiesReadFailure;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 
 */
public class SettingsHandler {
	private Map<String, Setting> settings;

	public Map<String, Setting> getSettings() {
		return settings;
	}

	private File file;
	private INIProperties properties;

	public SettingsHandler(File file) {
		this.file = file;
		properties = new INIProperties(file);
		settings = new ConcurrentHashMap<String, Setting>();
	}

	public void load() {
		read();
		save();
	}

	public Setting registerInteger(String category, String name, int defaultValue, Side side) {
		Setting setting;
		settings.put(category + "." + name,
				setting = new IntegerSetting(category, name, new Integer(defaultValue), side));
		return setting;
	}

	public Setting registerBoolean(String category, String name, boolean defaultValue, Side side) {
		Setting setting;
		settings.put(category + "." + name,
				setting = new BooleanSetting(category, name, new Boolean(defaultValue), side));
		return setting;
	}

	public Setting registerString(String category, String name, String defaultValue, Side side) {
		Setting setting;
		settings.put(category + "." + name, setting = new StringSetting(category, name, defaultValue, side));
		return setting;
	}

	public Setting registerFloat(String category, String name, float defaultValue, Side side) {
		Setting setting;
		settings.put(category + "." + name, setting = new FloatSetting(category, name, new Float(defaultValue), side));
		return setting;
	}

	public void save() {
		// Executors.newSingleThreadExecutor().execute(new Runnable() {

		// @Override
		// public void run() {
		Iterator it = settings.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Setting> entry = (Map.Entry) it.next();
			Setting setting = entry.getValue();
			// if (FMLCommonHandler.instance().getEffectiveSide() ==
			// settingInfo.getSide()) {
			if (!setting.isHidden() && setting.side == FMLCommonHandler.instance().getEffectiveSide()) {
				for (int i = 0; i < setting.getWriteListeners().size(); i++) {
					setting.getWriteListeners().get(i).listen(properties);
				}
				setting.write(properties);
			}
			// }
		}
		properties.updateFile();
		// }

		// });
	}

	public void read() {
		// Executors.newSingleThreadExecutor().execute(new Runnable() {

		// @Override
		// public void run() {
		Iterator it = settings.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Setting> entry = (Map.Entry) it.next();
			String settingInfo = entry.getKey();
			Setting setting = entry.getValue();
			try {
				for (int i = 0; i < setting.getReadListeners().size(); i++) {
					setting.getReadListeners().get(i).listen(properties);
				}
				setting.read(properties);
			} catch (INIPropertiesReadFailure e) {
				if (!setting.isHidden())
					e.printStackTrace();
			}
		}
		// }

		// });

	}

	public FloatSetting getFloatSetting(String setting) {
		return (FloatSetting) settings.get(setting);
	}

	public BooleanSetting getBooleanSetting(String setting) {
		return (BooleanSetting) settings.get(setting);
	}

	public StringSetting getStringSetting(String setting) {
		return (StringSetting) settings.get(setting);
	}

	public IntegerSetting getIntegerSetting(String setting) {
		return (IntegerSetting) settings.get(setting);
	}

	private static Gson gson;

	public Setting registerObject(String category, String name, Object object, Side side) {
		if (gson == null)
			gson = new Gson();
		Setting setting;
		settings.put(category + "." + name,
				setting = new ObjectSetting(category, name, object, object.getClass(), side));
		return setting;
	}

	public ObjectSetting getObjectSetting(String setting) {
		return (ObjectSetting) settings.get(setting);
	}

}
