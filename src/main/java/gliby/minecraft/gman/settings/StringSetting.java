package gliby.minecraft.gman.settings;

/**
 *
 */
public class StringSetting extends Setting {

    private Object lastData;

    /**
     * @param setting
     * @param name
     */
    StringSetting(String category, String name, String setting, Side side) {
        super(category, name, setting, side);
    }

    @Override
    public void read(INIProperties ini) {
        data = ini.readString(category, name, ((String) data));
    }

    @Override
    public void write(INIProperties ini) {
        ini.writeString(category, name, ((String) data));
        lastData = data;
    }

    public String getString() {
        return (String) data;
    }

    public void setString(String s) {
        this.data = s;
    }

    @Override
    public boolean hasChanged() {
        return data != lastData;
    }

}
