package gliby.minecraft.physics.client.render.lighting;

import net.minecraft.entity.Entity;

/**
 * No lighting!
 */
public class NullLight implements IDynamicLightHandler {

    public NullLight() {
    }

    @Override
    public void create(Entity light, int lightValue) {
    }

}
