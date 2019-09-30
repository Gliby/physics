package gliby.minecraft.physics;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class VersionChanges {

    public String version;
    private BufferedImage image;

    private ResourceLocation cachedResource;
    @SerializedName("Changes")
    private ArrayList<String> changes;

    public ResourceLocation getVersionImage(TextureManager manager) {
        if (image != null && cachedResource == null)
            cachedResource = manager.getDynamicTextureLocation(version, new DynamicTexture(image));
        return cachedResource;
    }

    public VersionChanges setImage(BufferedImage image) {
        this.image = image;
        return this;
    }

    public VersionChanges setVersion(String version) {
        this.version = version;
        return this;
    }

    public ArrayList<String> getChanges() {
        return changes;
    }

}
