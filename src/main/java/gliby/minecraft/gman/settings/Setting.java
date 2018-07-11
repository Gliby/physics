package gliby.minecraft.gman.settings;

import java.util.ArrayList;

import gliby.minecraft.gman.settings.INIProperties.INIPropertiesReadFailure;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

/**
 *
 */
public abstract class Setting {

	public enum Side {
		CLIENT, SERVER, BOTH;

		public static Side getEffectiveSide() {
			return toSide(FMLCommonHandler.instance().getEffectiveSide());
		}

		private static Side toSide(net.minecraftforge.fml.relauncher.Side effectiveSide) {
			return effectiveSide == net.minecraftforge.fml.relauncher.Side.CLIENT ? Side.CLIENT : Side.SERVER;
		}
	}

	public final String name, category;
	public final Side side;
	public Object data;

	/**
	 * @param data
	 * @param name
	 */
	Setting(String category, String name, Object data, Side side) {
		this.category = category;
		this.name = name;
		this.data = data;
		this.side = side;
		this.readListeners = new ArrayList<Setting.Listener>();
		this.writeListeners = new ArrayList<Setting.Listener>();
	}

	public Object getSettingData() {
		return data;
	}

	public abstract boolean hasChanged();

	public abstract void read(final INIProperties ini) throws INIPropertiesReadFailure;

	public abstract void write(final INIProperties ini);

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " - " + ": " + data;
	}

	private boolean hidden;

	public boolean isHidden() {
		return hidden;
	}

	public Setting setHidden(boolean hidden) {
		this.hidden = hidden;
		return this;
	}

	public static interface Listener {

		public void listen(final INIProperties ini);

	}

	private ArrayList<Listener> writeListeners;

	public ArrayList<Listener> getWriteListeners() {
		return writeListeners;
	}

	public Setting addWriteListener(Listener listener) {
		writeListeners.add(listener);
		return this;
	}

	private ArrayList<Listener> readListeners;

	public ArrayList<Listener> getReadListeners() {
		return readListeners;
	}

	public Setting addReadListener(Listener listener) {
		readListeners.add(listener);
		return this;
	}
}
