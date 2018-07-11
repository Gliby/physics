package gliby.minecraft.physics.common.game.items.toolgun.actions;

import javax.vecmath.Vector3f;

import gliby.minecraft.gman.EntityUtility;
import gliby.minecraft.physics.common.entity.EntityPhysicsBlock;
import gliby.minecraft.physics.common.entity.EnumRigidBodyProperty;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRayResult;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;

/**
 *
 */
public class ToolGunAlignAction implements IToolGunAction {

	@Override
	public String getName() {
		return "Align";
	}

	@Override
	public boolean use(PhysicsWorld physicsWorld, EntityPlayerMP player, Vector3f otherlookAt) {
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
			if (body != null && body.getOwner() instanceof EntityPhysicsBlock) {
				player.worldObj.setBlockState(new BlockPos(body.getOwner().posX, body.getOwner().posY + 0.5F, body.getOwner().posZ), ((EntityPhysicsBlock)body.getOwner()).getBlockState());
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
