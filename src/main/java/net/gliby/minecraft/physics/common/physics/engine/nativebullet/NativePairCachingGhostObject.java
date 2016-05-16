/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.bulletphysics.linearmath.Transform;

import net.gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import net.gliby.minecraft.physics.common.physics.engine.IGhostObject;
import net.minecraft.entity.Entity;

/**
 *
 */
class NativePairCachingGhostObject implements IGhostObject {

	private btPairCachingGhostObject ghostObject;
	Entity owner;
	
	NativePairCachingGhostObject(btPairCachingGhostObject object) {
		this.ghostObject = object;
	}
	
	NativePairCachingGhostObject(Entity owner, btPairCachingGhostObject object) {
		this.ghostObject = object;
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
	public void setWorldTransform(Transform entityTransform) {
		ghostObject.setWorldTransform(NativePhysicsWorld.fromTransformToMatrix4(entityTransform));
	}

	@Override
	public void setCollisionShape(ICollisionShape iCollisionShape) {
		ghostObject.setCollisionShape((btCollisionShape) iCollisionShape.getCollisionShape());
		
	}

	@Override
	public void setCollisionFlags(int characterObject) {
		ghostObject.setCollisionFlags(characterObject);
	}

	@Override
	public void setInterpolationWorldTransform(Transform entityTransform) {
		ghostObject.setInterpolationWorldTransform(NativePhysicsWorld.fromTransformToMatrix4(entityTransform));
	}

	@Override
	public Entity getOwner() {
		return owner;
	}

	@Override
	public void dispose() {
		ghostObject.dispose();
	}

}
