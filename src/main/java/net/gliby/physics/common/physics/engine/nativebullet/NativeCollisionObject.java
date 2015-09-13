/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.bulletphysics.linearmath.Transform;

import net.gliby.physics.common.physics.engine.ICollisionObject;
import net.gliby.physics.common.physics.engine.ICollisionShape;

/**
 *
 */
class NativeCollisionObject implements ICollisionObject {

	private btCollisionObject object;

	NativeCollisionObject(btCollisionObject object) {
		this.object = object;
	}

	@Override
	public Object getCollisionObject() {
		return object;
	}

	@Override
	public void setWorldTransform(Transform transform) {
		object.setWorldTransform(NativePhysicsWorld.fromTransformToMatrix4(transform));
	}

	@Override
	public void setCollisionShape(ICollisionShape shape) {
		object.setCollisionShape((btCollisionShape) shape.getCollisionShape());
	}

	@Override
	public void setCollisionFlags(int characterObject) {
		object.setCollisionFlags(characterObject);
	}

	@Override
	public void setInterpolationWorldTransform(Transform transform) {
		object.setInterpolationWorldTransform(NativePhysicsWorld.fromTransformToMatrix4(transform));
	}

}
