package net.gliby.gman;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

public class GMan {

	public static void launchMod(final Logger logger, ModInfo modInfo, final String minecraftVersion, final String modVersion) {
		StringBuilder builder = new StringBuilder();
		builder.append("https://raw.githubusercontent.com/Gliby/Mod-Information-Storage/master/");
		builder.append(modInfo.modId);
		builder.append(".json");
		final Gson gson = new Gson();
		Reader reader = null;
		try {
			reader = new InputStreamReader(new URL(builder.toString()).openStream());
		} catch (final MalformedURLException e) {
			e.printStackTrace();
			return;
		} catch (final IOException e) {
			logger.info("Failed to retrieve mod info, either mod doesn't exist or host(" + builder.toString() +") is down?");
			return;
		}

		final ModInfo externalInfo = gson.fromJson(reader, ModInfo.class);
		modInfo.donateURL = externalInfo.donateURL;
		modInfo.updateURL = externalInfo.updateURL;
		modInfo.versions = externalInfo.versions;
		modInfo.determineUpdate(modVersion, minecraftVersion);
		logger.info(modInfo.isUpdated() ? "Mod is up-to-date." : "Mod is outdated, download latest at " + modInfo.updateURL);
	}
}
