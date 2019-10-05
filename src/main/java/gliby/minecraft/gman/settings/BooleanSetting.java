package gliby.minecraft.gman.settings;

import gliby.minecraft.gman.settings.INIProperties.INIPropertiesReadFailure;

/**
 *
 */

/**
 *
 */
public class BooleanSetting extends Setting {

    private Object lastData;

    // this is basically a way to fix a dumb bug.
    private boolean booleanValue;

    /**
     * @param data
     * @param setting
     */
    public BooleanSetting(String category, String name, boolean booleanValue, Side side) {
        super(category, name, booleanValue, side);
        this.booleanValue = booleanValue;
    }

    @Override
    public void read(INIProperties ini) throws INIPropertiesReadFailure {
        booleanValue = ini.readBoolean(section, name, booleanValue);
        data = booleanValue;
    }

    @Override
    public void write(INIProperties ini) {
        ini.writeBoolean(section, name, booleanValue);
        this.lastData = data;
    }

    public boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean value) {
        this.booleanValue = value;
        this.data = new Boolean(booleanValue);
    }

    @Override
    public boolean hasChanged() {
        return lastData != data;
    }

    @Override
    public String toString() {
        return section + "." + name;
    }
}
