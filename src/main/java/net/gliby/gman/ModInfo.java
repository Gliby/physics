package net.gliby.gman;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ModInfo {

	@SerializedName("DonateURL")
	public String donateURL;

	@SerializedName("UpdateURL")
	public String updateURL;

	@SerializedName("Versions")
	public List<String> versions;

	private boolean updated = true;

	public final String modId;

	private String latestVersion;

	public ModInfo() {
		this.modId = "NULL";
	}

	public ModInfo(String modId, String updateURL) {
		this.updateURL = updateURL;
		this.donateURL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=PBXHJ67N62ZRW";
		this.modId = modId;
	}

	ModInfo determineUpdate(String currentModVersion, String currentMinecraftVersion) {
		for (final String s : versions) {
			if (s.startsWith(currentMinecraftVersion)) {
				this.latestVersion = s.split(":")[1].trim();
				updated = latestVersion.equals(currentModVersion);
				break;
			}
		}
		return this;
	}

	public final String getUpdateSite() {
		return updateURL;
	}

	public final boolean isUpdated() {
		return updated;
	}

	@Override
	public String toString() {
		return "[" + modId + "]" + "; Up to date? " + (isUpdated() ? "Yes" : "No");

	}

	public final boolean updateNeeded() {
		return !updated;
	}
	
	public String getLatestVersion() {
		return latestVersion;
	}
}
