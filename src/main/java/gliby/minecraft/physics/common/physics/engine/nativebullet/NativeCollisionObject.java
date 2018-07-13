package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionObject;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import net.minecraft.entity.Entity;

/**
 *
 */
class NativeCollisionObject implements ICollisionObject {

	private btCollisionObject object;

	protected NativePhysicsWorld physicsWorld;

	Entity owner;

	NativeCollisionObject(PhysicsWorld physicsWorld, btCollisionObject object) {
		this.physicsWorld = (NativePhysicsWorld) physicsWorld;
		this.object = object;
	}

	NativeCollisionObject(PhysicsWorld physicsWorld, Entity owner, btCollisionObject object) {
		this(physicsWorld, object);
		this.owner = owner;
	}

	@Override
	public Object getCollisionObject() {
		return object;
	}

	@Override
	public void setWorldTransform(final Transform transform) {
		synchronized(physicsWorld) {
			object.setWorldTransform(physicsWorld.fromTransformToMatrix4(transform));
		}
	}

	@Override
	public void setCollisionShape(final ICollisionShape shape) {
		synchronized(physicsWorld) {
			object.setCollisionShape((btCollisionShape) shape.getCollisionShape());
		}
	}

	@Override
	public void setCollisionFlags(final int characterObject) {
		synchronized(physicsWorld) {
			object.setCollisionFlags(characterObject);
		}
	}

	@Override
	public void setInterpolationWorldTransform(final Transform transform) {
		synchronized(physicsWorld) {
			object.setInterpolationWorldTransform(physicsWorld.fromTransformToMatrix4(transform));
		}
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
