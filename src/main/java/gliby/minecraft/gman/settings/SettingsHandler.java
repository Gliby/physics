package gliby.minecraft.gman.settings;

import com.google.gson.Gson;
import gliby.minecraft.gman.settings.INIProperties.INIPropertiesReadFailure;
import gliby.minecraft.gman.settings.Setting.Side;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// TODO improvement replace with better INI handler.
// TODO better null key support.

/**
 * INI based, key->value mutable settings handler.
 */
// TODO (0.6.0) Switch to Forge net.minecraftforge.common.config.Configuration;
public class SettingsHandler {

    private static Gson gson;
    private final File file;
    private final File directory;
    private boolean firstTime;
    private Map<String, Setting> settings;
    private INIProperties properties;

    public SettingsHandler(File directory, File settingsFile) {
        this.directory = directory;
        this.file = settingsFile;
        properties = new INIProperties(settingsFile);
        settings = new HashMap<String, Setting>();
    }

    public Map<String, Setting> getSettings() {
        return settings;
    }

    public File getDirectory() {
        return directory;
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public void load() {
        read();
        save();
    }

    public Setting registerInteger(String category, String name, int defaultValue, Setting.Side side) {
        Setting setting;
        settings.put(category + "." + name,
                setting = new IntegerSetting(category, name, new Integer(defaultValue), side));
        return setting;
    }

    public Setting registerBoolean(String category, String name, boolean defaultValue, Setting.Side side) {
        Setting setting;
        settings.put(category + "." + name,
                setting = new BooleanSetting(category, name, defaultValue, side));
        return setting;
    }

    public Setting registerString(String category, String name, String defaultValue, Setting.Side side) {
        Setting setting;
        settings.put(category + "." + name, setting = new StringSetting(category, name, defaultValue, side));
        return setting;
    }

    public Setting registerFloat(String category, String name, float defaultValue, Setting.Side side) {
        Setting setting;
        settings.put(category + "." + name, setting = new FloatSetting(category, name, new Float(defaultValue), side));
        return setting;
    }

    // TODO code improvement: replace this hacky IO threading

    // TODO improvement: run in separate worker thread
    public void save() {
        new Thread(new Runnable() {
            public void run() {
                firstTime = !file.exists();
                Iterator it = settings.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Setting> entry = (Map.Entry) it.next();
                    Setting setting = entry.getValue();
                    // if (FMLCommonHandler.instance().getEffectiveSide() ==
                    // settingInfo.getSide()) {
                    if (!setting.isHidden()
                            && (setting.side == Setting.Side.getEffectiveSide() || setting.side == Side.BOTH)) {
                        for (int i = 0; i < setting.getWriteListeners().size(); i++) {
                            setting.getWriteListeners().get(i).listen(properties);
                        }
                        setting.write(properties);
                        setting.writeComment(properties);
                    }
                    // }
                }

                properties.updateFile();
            }
        }).start();
    }

    public void read() {
        new Thread(new Runnable() {
            public void run() {
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
                        // if (!setting.isHidden())
                        // e.printStackTrace();
                    }
                }
            }
        }).start();
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

    public Setting registerObject(String category, String name, Object object, Setting.Side side) {
        Setting setting;
        settings.put(category + "." + name,
                setting = new ObjectSetting(category, name, object, object.getClass(), side));
        return setting;
    }

    public ObjectSetting getObjectSetting(String setting) {
        return (ObjectSetting) settings.get(setting);
    }

}
