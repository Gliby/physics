/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.items.toolgun;

import javax.vecmath.Vector3f;

import net.gliby.physics.common.physics.PhysicsWorld;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 */
public interface IToolGunAction {

	public String getName();

	public boolean use(PhysicsWorld world, EntityPlayerMP player, Vector3f lookAt);
	
	public void stoppedUsing(PhysicsWorld world, EntityPlayerMP player);
}
