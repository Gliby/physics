/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.physics.common.game.items.toolgun.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

import net.gliby.minecraft.gman.EntityUtility;
import net.gliby.minecraft.physics.common.entity.EnumRigidBodyProperty;
import net.gliby.minecraft.physics.common.physics.PhysicsWorld;
import net.gliby.minecraft.physics.common.physics.engine.IConstraintGeneric6Dof;
import net.gliby.minecraft.physics.common.physics.engine.IRayResult;
import net.gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 */
public class ToolGunMotorAction implements IToolGunAction, IToolGunTickable {
	private Map<Integer, ToolGunHit> hits = new HashMap<Integer, ToolGunHit>();
	private List<Motor> motors = new ArrayList<Motor>();

	public class Motor {

		private final IRigidBody attached, motorized;

		public IRigidBody getAttached() {
			return attached;
		}

		public IRigidBody getMotorized() {
			return motorized;
		}

		public boolean isReversed() {
			return reversed;
		}

		private final boolean reversed;

		public Motor(IRigidBody attached, IRigidBody motorized, boolean direction) {
			this.attached = attached;
			this.motorized = motorized;
			this.reversed = direction;
		}
	}

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

					// body.getProperties().put("Motorized", direction);
					motors.add(new Motor(hit.getLastBody(), body, false));
					IConstraintGeneric6Dof generic6Dof = physicsWorld.createGeneric6DofConstraint(body,
							hit.getLastBody(), transformA, transformB, true);
					physicsWorld.addConstraint(generic6Dof);
					hits.remove(player.getEntityId());
				} else
					hits.put(player.getEntityId(), new ToolGunHit(relativePivot, body));
			}
		}
		return true;
	}

	// TODO Hit's map should not be tool unique, only one is required per
	// user...

	@Override
	public void stoppedUsing(PhysicsWorld world, EntityPlayerMP player) {
		hits.remove(player.getEntityId());
	}

	@Override
	public String getName() {
		return "Motor";
	}

	@Override
	public void update(PhysicsWorld physicsWorld) {
		for (int i = 0; i < motors.size(); i++) {
			Motor motor = motors.get(i);

			Vector3f forward = new Vector3f(
					motor.getMotorized().getPosition().getX() - motor.getAttached().getPosition().getX(), motor.getMotorized().getPosition().getY() - motor.getAttached().getPosition().getY(), motor.getMotorized().getPosition().getZ() - motor.getAttached().getPosition().getZ());
			forward.normalize();
			

			// First we get normalized gravity.
			Vector3f up = new Vector3f(physicsWorld.getGravityDirection());
			up.scale(-1);
			// Up vector is inverted gravity direction;
			Vector3f direction = new Vector3f();
			direction.cross(forward, up);
			direction.scale(50);
			motor.getMotorized().applyTorqueImpulse(direction);
			
			
		}
	}
}
