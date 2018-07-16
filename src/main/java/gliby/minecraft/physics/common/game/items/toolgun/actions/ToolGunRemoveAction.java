package gliby.minecraft.physics.common.game.items.toolgun.actions;

import javax.vecmath.Vector3f;

import com.badlogic.gdx.math.Vector3;

import gliby.minecraft.gman.EntityUtility;
import gliby.minecraft.physics.common.entity.EnumRigidBodyProperty;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRayResult;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
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
	public boolean use(PhysicsWorld physicsWorld, EntityPlayerMP player, Vector3 otherLookAt) {
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
		if (ray.hasHit()) {
			IRigidBody body = physicsWorld.upcastRigidBody(ray.getCollisionObject());
			if (body != null) {
				body.getProperties().put(EnumRigidBodyProperty.DEAD.getName(), System.currentTimeMillis());
				return true;
			}
		}
		return false;
	}

	@Override
	public void stoppedUsing(PhysicsWorld world, EntityPlayerMP player) {
	}

}
