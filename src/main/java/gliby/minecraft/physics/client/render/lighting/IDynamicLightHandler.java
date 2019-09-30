package gliby.minecraft.physics.client.render.lighting;

import net.minecraft.entity.Entity;

/**
 *
 */
public interface IDynamicLightHandler {

    void create(Entity light, int lightValue);
}
