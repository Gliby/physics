package gliby.minecraft.physics.common.physics.engine.concurrent.nativebullet;

import java.lang.annotation.Native;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.IGhostObject;
import gliby.minecraft.physics.common.physics.engine.concurrent.ConcurrentPhysicsWorld;
import net.minecraft.entity.Entity;

/**
 *
 */
class NativePairCachingGhostObject implements IGhostObject {

	private btPairCachingGhostObject ghostObject;

	protected ConcurrentPhysicsWorld physicsWorld;

	Entity owner;

	NativePairCachingGhostObject(ConcurrentPhysicsWorld physicsWorld, btPairCachingGhostObject object) {
		this.physicsWorld = (ConcurrentPhysicsWorld) physicsWorld;
		this.ghostObject = object;
	}

	NativePairCachingGhostObject(ConcurrentPhysicsWorld physicsWorld, Entity owner, btPairCachingGhostObject object) {
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
		getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				ghostObject.setWorldTransform(NativePhysicsWorld.fromTransformToMatrix4(entityTransform));
			}
		});
	}

	@Override
	public void setCollisionShape(final ICollisionShape collisionShape) {
		getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				ghostObject.setCollisionShape((btCollisionShape) collisionShape.getCollisionShape());
			}
		});
	}

	@Override
	public void setCollisionFlags(final int characterObject) {
		getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				ghostObject.setCollisionFlags(characterObject);
			}
		});
	}

	@Override
	public void setInterpolationWorldTransform(final Transform entityTransform) {
		getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				ghostObject.setInterpolationWorldTransform(NativePhysicsWorld.fromTransformToMatrix4(entityTransform));
			}
		});
	}

	@Override
	public Entity getOwner() {
		return owner;
	}

	@Override
	public ConcurrentPhysicsWorld getPhysicsWorld() {
		return physicsWorld;
	}

}
