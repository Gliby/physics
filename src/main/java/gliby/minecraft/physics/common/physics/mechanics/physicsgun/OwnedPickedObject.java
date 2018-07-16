package gliby.minecraft.physics.common.physics.mechanics.physicsgun;

import com.badlogic.gdx.math.Vector3;

import gliby.minecraft.physics.common.physics.engine.IRayResult;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.Entity;

/**
 *
 */
public class OwnedPickedObject extends PickedObject {

	private final Entity owner;

	/**
	 * @return the owner
	 */
	public Entity getOwner() {
		return owner;
	}

	/**
	 * @param rayCallback
	 * @param rayFromWorld
	 * @param rayToWorld
	 */
	public OwnedPickedObject(IRigidBody body, Entity owner, IRayResult rayCallback, Vector3 rayFromWorld, Vector3 rayToWorld) {
		super(body, rayCallback, rayFromWorld, rayToWorld);
		this.owner = owner;
	}

}
