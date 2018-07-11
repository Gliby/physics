package gliby.minecraft.physics.common.physics.engine.nativebullet;

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.common.physics.engine.ICollisionObject;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import net.minecraft.entity.Entity;

/**
 *
 */
class NativeCollisionObject implements ICollisionObject {

	private btCollisionObject object;

	NativeCollisionObject(btCollisionObject object) {
		this.object = object;
	}
	
	Entity owner;

	NativeCollisionObject(Entity owner, btCollisionObject object) {
		this.owner = owner;
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

	@Override
	public Entity getOwner() {
		return owner;
	}


}
