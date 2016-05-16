/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.Transform;

import net.gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import net.gliby.minecraft.physics.common.physics.engine.IGhostObject;
import net.minecraft.entity.Entity;

/**
 *
 */
public class JavaPairCachingGhostObject implements IGhostObject {

	Entity owner;
	private PairCachingGhostObject object;

	JavaPairCachingGhostObject(PairCachingGhostObject object) {
		this.object = object;
	}
	
	JavaPairCachingGhostObject(Entity entity, PairCachingGhostObject object) {
		this.owner = entity;
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

	@Override
	public Entity getOwner() {
		return owner;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
