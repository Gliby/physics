package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.IGhostObject;
import net.minecraft.entity.Entity;

/**
 *
 */
class NativePairCachingGhostObject implements IGhostObject {

	private btPairCachingGhostObject ghostObject;

	protected NativePhysicsWorld physicsWorld;

	Entity owner;

	NativePairCachingGhostObject(PhysicsWorld physicsWorld, btPairCachingGhostObject object) {
		this.physicsWorld = (NativePhysicsWorld) physicsWorld;
		this.ghostObject = object;
	}

	NativePairCachingGhostObject(PhysicsWorld physicsWorld, Entity owner, btPairCachingGhostObject object) {
		this(physicsWorld, object);
		this.owner = owner;
	}

	@Override
	public Object getGhostObject() {
		return ghostObject;
	}

	@Override
	public Object getCollisionObject() {
		return ghostObject;
	}

	@Override
	public void setWorldTransform(final Transform entityTransform) {
		ghostObject.setWorldTransform(physicsWorld.fromTransformToMatrix4(entityTransform));
	}

	@Override
	public void setCollisionShape(final ICollisionShape collisionShape) {
		ghostObject.setCollisionShape((btCollisionShape) collisionShape.getCollisionShape());
	}

	@Override
	public void setCollisionFlags(final int characterObject) {
		ghostObject.setCollisionFlags(characterObject);
	}

	@Override
	public void setInterpolationWorldTransform(final Transform entityTransform) {
		ghostObject.setInterpolationWorldTransform(physicsWorld.fromTransformToMatrix4(entityTransform));
	}

	@Override
	public Entity getOwner() {
		return owner;
	}

	@Override
	public PhysicsWorld getPhysicsWorld() {
		return physicsWorld;
	}

}
