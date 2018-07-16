package gliby.minecraft.physics.common.entity.mechanics;

import javax.vecmath.Vector3f;

import com.badlogic.gdx.math.Vector3;

import gliby.minecraft.physics.common.entity.EnumRigidBodyProperty;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Used for slime blocks.
 */
public class BounceMechanic extends RigidBodyMechanic {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.gliby.physics.common.physics.entitymechanics.RigidBodyMechanic#update ()
	 */
	// TODO bug: bounce code needs a rework
	@Override
	public void update(IRigidBody rigidBody, PhysicsWorld physicsWorld, Entity entity, Side side) {
		if (rigidBody.isActive()) {
			World world = rigidBody.getOwner().worldObj;
			Vec3 startRay = new Vec3(rigidBody.getOwner().posX, rigidBody.getOwner().posY, rigidBody.getOwner().posZ);
			Vec3 endRay = startRay.subtract(0, startRay.yCoord, 0);
			MovingObjectPosition pos = world.rayTraceBlocks(startRay, endRay, false, true, false);
			if (pos != null) {
				float dist = (float) startRay.distanceTo(pos.hitVec);
				float biggestDistance = 0;
				if (rigidBody.getProperties().containsKey(EnumRigidBodyProperty.BIGGESTDISTANCE.getName()))
					biggestDistance = (Float) rigidBody.getProperties()
							.get(EnumRigidBodyProperty.BIGGESTDISTANCE.getName());
				if (dist >= 1) {
					if (dist > biggestDistance) {
						rigidBody.getProperties().put(EnumRigidBodyProperty.BIGGESTDISTANCE.getName(), dist);
						biggestDistance = (float) dist;
					}
				} else if (biggestDistance > 1 && rigidBody.hasContactResponse()) {
					float mass = rigidBody.getInvMass();
					Vector3 impulse = new Vector3(0, biggestDistance, 0);
					impulse.scl(mass * 300);
					impulse.y = (MathHelper.clamp_float(impulse.y, 0, 300));
					rigidBody.applyCentralImpulse(impulse);
					rigidBody.getProperties().put(EnumRigidBodyProperty.BIGGESTDISTANCE.getName(), 0.0F);
					biggestDistance = 0;
				}
			}
		}
	}

}
