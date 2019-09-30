package gliby.minecraft.gman.settings;

import gliby.minecraft.gman.settings.INIProperties.INIPropertiesReadFailure;

/**
 *
 */
public class IntegerSetting extends Setting {

    private Object lastData;

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
                ini.readInteger(category, name, ((Integer) data).intValue()));
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

    @Override
    public boolean hasChanged() {
        return data != lastData;
    }
}
