package gliby.minecraft.physics.common.game.items.toolgun.actions;


import com.badlogic.gdx.math.Vector3;

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
	public boolean use(PhysicsWorld physicsWorld, EntityPlayerMP player, Vector3 otherlookAt) {
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
