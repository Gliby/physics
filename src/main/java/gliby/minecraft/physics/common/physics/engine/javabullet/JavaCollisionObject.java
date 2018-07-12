package gliby.minecraft.physics.common.physics.engine.javabullet;

import com.bulletphysicsx.collision.dispatch.CollisionObject;
import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionObject;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import net.minecraft.entity.Entity;

/**
 *
 */
public class JavaCollisionObject implements ICollisionObject {

	private CollisionObject object;
	
	protected PhysicsWorld physicsWorld;
	
	JavaCollisionObject(PhysicsWorld physicsWorld, CollisionObject object) {
		this.physicsWorld = physicsWorld;
		this.object = object;
	}
	
	Entity owner;
	
	JavaCollisionObject(PhysicsWorld physicsWorld, Entity owner, CollisionObject object) {
		this(physicsWorld, object);
		this.owner = owner;
	}

	@Override
	public Object getCollisionObject() {
		return object;
	}

	@Override
	public void setWorldTransform(final Transform transform) {
		this.getPhysicsWorld().scheduledTasks.add(new Runnable() {
			
			@Override
			public void run() {
				object.setWorldTransform(transform);
			}
		});
	}

	@Override
	public void setCollisionShape(final ICollisionShape iCollisionShape) {
		this.getPhysicsWorld().scheduledTasks.add(new Runnable() {
			
			@Override
			public void run() {
				object.setCollisionShape((CollisionShape) iCollisionShape.getCollisionShape());
			}
		});
	}

	@Override
	public void setCollisionFlags(final int characterObject) {
		this.getPhysicsWorld().scheduledTasks.add(new Runnable() {
			
			@Override
			public void run() {
				object.setCollisionFlags(characterObject);
			}
		});
	}

	@Override
	public void setInterpolationWorldTransform(final Transform transform) {
		this.getPhysicsWorld().scheduledTasks.add(new Runnable() {
			
			@Override
			public void run() {
				object.setInterpolationWorldTransform(transform);
			}
		});
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
