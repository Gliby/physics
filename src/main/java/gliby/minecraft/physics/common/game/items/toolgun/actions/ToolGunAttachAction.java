package gliby.minecraft.physics.common.game.items.toolgun.actions;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.gman.EntityUtility;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintGeneric6Dof;
import gliby.minecraft.physics.common.physics.engine.IRayResult;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 */
public class ToolGunAttachAction implements IToolGunAction {

	private Map<Integer, ToolGunHit> hits = new HashMap<Integer, ToolGunHit>();

	@Override
	public boolean use(PhysicsWorld physicsWorld, EntityPlayerMP player, Vector3 blockLookAt) {
		Vector3 offset = new Vector3(0.5f, 0.5f, 0.5f);
		Vector3 eyePos = EntityUtility.getPositionEyes(player);
		Vector3 eyeLook = EntityUtility.toVector3(player.getLook(1));
		Vector3 lookAt = new Vector3(eyePos);
		eyeLook.scl(64);
		lookAt.add(eyeLook);
		eyePos.sub(offset);
		lookAt.sub(offset);

		IRayResult ray = physicsWorld.createClosestRayResultCallback(eyePos, lookAt);
		physicsWorld.rayTest(eyePos, lookAt, ray);
		if (ray.getCollisionObject() != null && ray.hasHit()) {
			IRigidBody body = physicsWorld.upcastRigidBody(ray.getCollisionObject());
			if (body != null) {
				Matrix4 centerOfMassTransform = body.getCenterOfMassTransform(new Matrix4());
				centerOfMassTransform.inv();
				Vector3 relativePivot = new Vector3(ray.getHitPointWorld());
				// TODO possible bug: possible bug
				centerOfMassTransform.translate(relativePivot);
		
				
				ToolGunHit hit;
				if ((hit = hits.get(player.getEntityId())) != null) {
					Matrix4 transformA = new Matrix4();
					transformA.idt();
					Matrix4 transformB = new Matrix4();
					transformB.idt();
					transformB.setTranslation(hit.getLastHitNormal());

					body.activate();
					hit.getLastBody().activate();
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

	@Override
	public void stoppedUsing(PhysicsWorld world, EntityPlayerMP player) {
		hits.remove(player.getEntityId());
	}

	@Override
	public String getName() {
		return "Attach";
	}
}
