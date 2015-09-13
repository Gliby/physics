/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.game.items.toolgun.actions;

import javax.vecmath.Vector3f;

import net.gliby.gman.EntityUtility;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.IRayResult;
import net.gliby.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 */
public class ToolGunRemoveAction implements IToolGunAction {

	@Override
	public String getName() {
		return "Remove";
	}

	@Override
	public boolean use(PhysicsWorld physicsWorld, EntityPlayerMP player, Vector3f otherLookAt) {
		Vector3f offset = new Vector3f(0.5f, 0.5f, 0.5f);
		Vector3f eyePos = EntityUtility.getPositionEyes(player);
		Vector3f eyeLook = EntityUtility.toVector3f(player.getLook(1));
		Vector3f lookAt = new Vector3f(eyePos);
		eyeLook.scale(64);
		lookAt.add(eyeLook);
		eyePos.sub(offset);
		lookAt.sub(offset);

		IRayResult ray = physicsWorld.createClosestRayResultCallback(eyePos, lookAt);
		physicsWorld.rayTest(eyePos, lookAt, ray);
		if (ray.hasHit()) {
			IRigidBody body = physicsWorld.upcastRigidBody(ray.getCollisionObject());
			if (body != null) {
				body.getProperties().put("Dead", System.currentTimeMillis());
				return true;
			}
		}
		return false;
	}

	@Override
	public void stoppedUsing(PhysicsWorld world, EntityPlayerMP player) {
	}

}
