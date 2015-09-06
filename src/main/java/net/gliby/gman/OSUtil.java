package net.gliby.gman;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OSUtil {

	private static final String __OBFID = "CL_00001633";

	public static OSUtil.EnumOS getOSType() {
		String s = System.getProperty("os.name").toLowerCase();
		return s.contains(
				"win") ? OSUtil.EnumOS.WINDOWS
						: (s.contains(
								"mac") ? OSUtil.EnumOS.OSX
										: (s.contains(
												"solaris")
														? OSUtil.EnumOS.SOLARIS
														: (s.contains(
																"sunos") ? OSUtil.EnumOS.SOLARIS
																		: (s.contains("linux") ? OSUtil.EnumOS.LINUX
																				: (s.contains("unix")
																						? OSUtil.EnumOS.LINUX
																						: OSUtil.EnumOS.UNKNOWN)))));
	}

	public static enum EnumOS {
		LINUX, SOLARIS, WINDOWS, OSX, UNKNOWN;

		private static final String __OBFID = "CL_00001660";
	}
}