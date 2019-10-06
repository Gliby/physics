package gliby.minecraft.gman;

import com.google.gson.annotations.SerializedName;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.ModContainer;

import java.util.List;

public class ModInfo {

    public final String modId;
    @SerializedName("DonateURL")
    public String donateURL;
    @SerializedName("UpdateURL")
    public String updateURL;
    @SerializedName("Versions")

    public List<String> versions;
    protected ForgeVersion.Status status = ForgeVersion.Status.PENDING;
    private String latestVersion;

    public ModInfo() {
        this.modId = "NULL";
    }

    public ModInfo(String modId, String updateURL) {
        this.updateURL = updateURL;
        this.donateURL = "";
        this.modId = modId;
    }

    public ForgeVersion.Status getStatus() {
        return status;
    }

    public void setStatus(ForgeVersion.Status status) {
        this.status = status;
    }

    ModInfo determineUpdate(String currentModVersion, String currentMinecraftVersion) {
        for (final String s : versions) {
            if (s.startsWith(currentMinecraftVersion)) {
                this.latestVersion = s.split(":")[1].trim();
                status = latestVersion.equals(currentModVersion) ? ForgeVersion.Status.UP_TO_DATE : ForgeVersion.Status.OUTDATED;
                break;
            }
        }
        return this;
    }

    public void applyToMod(ModContainer container) {

    }

    public final String getUpdateSite() {
        return updateURL;
    }

    public final boolean isUpdated() {
        return status == ForgeVersion.Status.UP_TO_DATE;
    }

    @Override
    public String toString() {
        return "[" + modId + "]" + "; Up to date? " + (isUpdated() ? "Yes" : "No");

    }

    public final boolean updateNeeded() {
        return status == ForgeVersion.Status.OUTDATED;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
