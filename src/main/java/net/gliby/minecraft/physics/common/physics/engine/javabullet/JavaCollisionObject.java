/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.Transform;

import net.gliby.minecraft.physics.common.physics.engine.ICollisionObject;
import net.gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import net.minecraft.entity.Entity;

/**
 *
 */
public class JavaCollisionObject implements ICollisionObject {

	private CollisionObject object;

	JavaCollisionObject(CollisionObject object) {
		this.object = object;
	}
	
	Entity owner;
	
	JavaCollisionObject(Entity owner, CollisionObject object) {
		this.owner = owner;
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

	@Override
	public Entity getOwner() {
		return owner;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
