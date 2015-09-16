package net.gliby.physics;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

public class VersionChanges {

	public String version;

	public VersionChanges setVersion(String version) {
		this.version = version;
		return this;
	}

	@SerializedName("MajorChanges")
	private ArrayList<String> majorChanges;
	@SerializedName("MinorChanges")
	private ArrayList<String> minorChanges;

	public ArrayList<String> getMajorChanges() {
		return majorChanges;
	}

	public ArrayList<String> getMinorChanges() {
		return minorChanges;
	}

}
