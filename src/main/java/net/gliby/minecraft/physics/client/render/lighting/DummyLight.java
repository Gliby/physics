/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.physics.client.render.lighting;

import net.minecraft.entity.Entity;

/**
 * No lighting!
 */
public class DummyLight implements IDynamicLightHandler {

	public DummyLight() {
	}

	@Override
	public void create(Entity light, int lightValue) {
	}

}
