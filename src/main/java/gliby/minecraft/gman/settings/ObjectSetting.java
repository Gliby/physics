package gliby.minecraft.gman.settings;

import gliby.minecraft.gman.GMan;

/**
 * Use JSON!
 */
public class ObjectSetting extends Setting {
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
        data = GMan.getGSON().fromJson(ini.readString(section, name, GMan.getGSON().toJson(data)), objectClass);
    }

    @Override
    public void write(INIProperties ini) {
        ini.writeString(section, name, GMan.getGSON().toJson(data, objectClass));
        lastData = data;
    }

    @Override
    public boolean hasChanged() {
        return data != lastData;
    }

}
