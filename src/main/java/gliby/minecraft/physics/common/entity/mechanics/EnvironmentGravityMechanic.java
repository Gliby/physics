package gliby.minecraft.physics.common.entity.mechanics;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;

/**
 *
 */
public class EnvironmentGravityMechanic extends RigidBodyMechanic {

	@Override
	public void update(IRigidBody rigidBody, PhysicsWorld physicsWorld, Entity entity, Side side) {
		// this.rigidBody.getProperties().put("magnetised", true);
		// rigidBody.activate();
		// if (rigidBody.getOwner().isInWater()) {
		// Vector3f modifiedGravity = new Vector3f(new Vector3f(0, -4, 0));
		// rigidBody.setGravity(rigidBody.isActive() ? modifiedGravity :new
		// Vector3f(0, -9.8f, 0));
		// } else rigidBody.setGravity(new Vector3f(0, -9.8F, 0));
	}
}
