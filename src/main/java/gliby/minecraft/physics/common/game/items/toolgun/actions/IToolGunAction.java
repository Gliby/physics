/**
 * Copyright (c) 2015, Mine Fortress.
 */
package gliby.minecraft.physics.common.game.items.toolgun.actions;

import javax.vecmath.Vector3f;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 */
public interface IToolGunAction {

	public String getName();

	public boolean use(PhysicsWorld world, EntityPlayerMP player, Vector3f lookAt);
	
	public void stoppedUsing(PhysicsWorld world, EntityPlayerMP player);
}
