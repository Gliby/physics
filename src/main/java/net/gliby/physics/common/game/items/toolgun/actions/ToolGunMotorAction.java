/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.game.items.toolgun.actions;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

import net.gliby.gman.EntityUtility;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.IConstraintGeneric6Dof;
import net.gliby.physics.common.physics.engine.IRayResult;
import net.gliby.physics.common.physics.engine.IRigidBody;
import net.gliby.physics.common.physics.mechanics.ToolMechanic;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 */
public class ToolGunMotorAction implements IToolGunAction {
	private Map<Integer, ToolGunHit> hits = new HashMap<Integer, ToolGunHit>();

	@Override
	public boolean use(PhysicsWorld physicsWorld, EntityPlayerMP player, Vector3f blockLookAt) {
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
		if (ray.getCollisionObject() != null && ray.hasHit()) {
			IRigidBody body = physicsWorld.upcastRigidBody(ray.getCollisionObject());
			if (body != null) {
				Transform centerOfMassTransform = body.getCenterOfMassTransform(new Transform());
				Vector3f comNormal = new Vector3f(centerOfMassTransform.origin);
				comNormal.normalize();

				centerOfMassTransform.inverse();
				Vector3f relativePivot = new Vector3f(ray.getHitPointWorld());
				centerOfMassTransform.transform(relativePivot);

				ToolGunHit hit;
				if ((hit = hits.get(player.getEntityId())) != null) {
					Transform transformA = new Transform();
					transformA.setIdentity();
					transformA.origin.set(relativePivot);
					Transform transformB = new Transform();
					transformB.setIdentity();
					transformB.origin.set(hit.getLastHitNormal());

					body.activate();
					hit.getLastBody().activate();

					Vector3f direction = new Vector3f();
					Vector3f up = new Vector3f(physicsWorld.getGravityDirection());
					// TODO Up vector is inverted gravity direction;
					up.scale(-1);
					direction.cross(comNormal, up);

					body.getProperties().put("Motorized", direction);
					ToolMechanic toolMechanic = (ToolMechanic) physicsWorld.getMechanics().get("ToolMan");
					toolMechanic.getBodies().add(body);

					IConstraintGeneric6Dof generic6Dof = physicsWorld.createGeneric6DofConstraint(body,
							hit.getLastBody(), transformA, transformB, true);
					hits.put(player.getEntityId(), null);
				} else
					hits.put(player.getEntityId(), new ToolGunHit(relativePivot, body));
			}
		}
		return true;
	}

	@Override
	public void stoppedUsing(PhysicsWorld world, EntityPlayerMP player) {
		hits.remove(player.getEntityId());
	}

	@Override
	public String getName() {
		return "Motor";
	}
}
