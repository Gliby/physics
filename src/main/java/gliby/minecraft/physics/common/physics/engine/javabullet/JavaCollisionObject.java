package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.dispatch.CollisionObject;
import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.common.physics.engine.ICollisionObject;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
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
}
