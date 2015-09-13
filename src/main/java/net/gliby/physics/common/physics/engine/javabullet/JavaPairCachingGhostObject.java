/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.engine.javabullet;

import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.Transform;

import net.gliby.physics.common.physics.engine.ICollisionShape;
import net.gliby.physics.common.physics.engine.IGhostObject;

/**
 *
 */
public class JavaPairCachingGhostObject implements IGhostObject {

	private PairCachingGhostObject object;

	JavaPairCachingGhostObject(PairCachingGhostObject object) {
		this.object = object;
	}

	@Override
	public Object getGhostObject() {
		return object;
	}

	@Override
	public Object getCollisionObject() {
		return object;
	}

	@Override
	public void setWorldTransform(Transform entityTransform) {
		object.setWorldTransform(entityTransform);
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
	public void setInterpolationWorldTransform(Transform entityTransform) {
		object.setInterpolationWorldTransform(entityTransform);
	}

}
