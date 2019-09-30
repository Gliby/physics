package gliby.minecraft.gman.settings;

import com.google.gson.Gson;

/**
 * Use JSON!
 */
public class ObjectSetting extends Setting {
    private static Gson gson = new Gson();
    private Class objectClass;
    private Object lastData;

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
