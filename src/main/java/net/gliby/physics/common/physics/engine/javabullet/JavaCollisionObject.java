/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.engine.javabullet;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.Transform;

import net.gliby.physics.common.physics.engine.ICollisionObject;
import net.gliby.physics.common.physics.engine.ICollisionShape;

/**
 *
 */
public class JavaCollisionObject implements ICollisionObject {

	private CollisionObject object;

	JavaCollisionObject(CollisionObject object) {
		this.object = object;
	}

	@Override
	public Object getCollisionObject() {
		return object;
	}

	@Override
	public void setWorldTransform(Transform transform) {
		object.setWorldTransform(transform);
	}

	@Override
	public void setCollisionShape(ICollisionShape iCollisionShape) {
		object.setCollisionShape((CollisionShape) iCollisionShape.getCollisionShape());
	}

	@Override
	public void setCollisionFlags(int characterObject) {
		object.setCollisionFlags(characterObject);
	}

	@Override
	public void setInterpolationWorldTransform(Transform transform) {
		object.setInterpolationWorldTransform(transform);
	}

}
