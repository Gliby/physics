/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.client.render.lighting;

import net.minecraft.entity.Entity;

/**
 *
 */
public interface IDynamicLightHandler {

	public abstract void create(Entity light, int lightValue);
}
