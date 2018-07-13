package gliby.minecraft.physics.common.physics.engine.javabullet;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.dynamics.RigidBody;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.IQuaternion;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import gliby.minecraft.physics.common.physics.engine.IVector3;
import net.minecraft.entity.Entity;

/**
 *
 */
class JavaRigidBody extends JavaCollisionObject implements IRigidBody {

	private RigidBody rigidBody;

	private ICollisionShape collisionShape;

	private Map<String, Object> properties;

	public JavaRigidBody(PhysicsWorld physicsWorld, RigidBody body, Entity owner) {
		super(physicsWorld, owner, body);
		this.rigidBody = body;
		this.owner = owner;
		this.collisionShape = new JavaCollisionShape(physicsWorld, body.getCollisionShape());
		this.properties = new HashMap<String, Object>();
		this.quatRotation = new Quat4f();
		this.rotation = new JavaQuaternion(quatRotation);
		this.worldTransform = new Transform();
		this.position = new JavaVector3(worldTransform.origin);
	}

	@Override
	public Object getBody() {
		return rigidBody;
	}

	@Override
	public ICollisionShape getCollisionShape() {
		return collisionShape;
	}

	@Override
	public boolean isActive() {
		return rigidBody.isActive();
	}

	@Override
	public Vector3f getAngularVelocity(Vector3f out) {
		return rigidBody.getAngularVelocity(out);
	}

	@Override
	public Vector3f getLinearVelocity(Vector3f out) {
		return rigidBody.getLinearVelocity(out);
	}

	@Override
	public Vector3f getCenterOfMassPosition(Vector3f out) {
		return rigidBody.getCenterOfMassPosition(out);
	}

	@Override
	public Transform getWorldTransform(Transform transform) {
		return rigidBody.getWorldTransform(transform);
	}

	@Override
	public void setWorldTransform(final Transform transform) {
		this.getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				rigidBody.setWorldTransform(transform);
			}
		});
	}

	@Override
	public void setGravity(final Vector3f acceleration) {
		this.getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				rigidBody.setGravity(acceleration);
			}
		});
	}

	@Override
	public void setFriction(final float friction) {
		this.getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				rigidBody.setFriction(friction);
			}
		});
	}

	@Override
	public void setLinearVelocity(final Vector3f linearVelocity) {
		this.getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				rigidBody.setLinearVelocity(linearVelocity);
			}
		});
	}

	@Override
	public void setAngularVelocity(final Vector3f angularVelocity) {
		this.getPhysicsWorld().physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				rigidBody.setAngularVelocity(angularVelocity);
			}
		});
	}

	@Override
	public void applyCentralImpulse(final Vector3f direction) {
		synchronized (physicsWorld) {
			rigidBody.applyCentralImpulse(direction);
		}
	}

	@Override
	public boolean hasContactResponse() {
		synchronized (physicsWorld) {
			return rigidBody.hasContactResponse();
		}
	}

	@Override
	public float getInvMass() {
		return rigidBody.getInvMass();
	}

	@Override
	public void activate() {
		synchronized (physicsWorld) {
			rigidBody.activate();
		}
	}

	@Override
	public void getAabb(Vector3f aabbMin, Vector3f aabbMax) {
		synchronized (physicsWorld) {
			rigidBody.getAabb(aabbMin, aabbMax);
		}
	}

	@Override
	public Transform getCenterOfMassTransform(Transform transform) {
		synchronized (physicsWorld) {
			return rigidBody.getCenterOfMassTransform(transform);
		}
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public void applyCentralForce(final Vector3f force) {
		synchronized (physicsWorld) {
			rigidBody.applyCentralForce(force);
		}
	}

	@Override
	public Vector3f getGravity(Vector3f vector3f) {
		synchronized (physicsWorld) {
			return rigidBody.getGravity(vector3f);
		}
	}

	private Vector3f centerOfMass;

	@Override
	public Vector3f getCenterOfMassPosition() {
		synchronized (physicsWorld) {
			if (centerOfMass == null)
				centerOfMass = new Vector3f();
			this.rigidBody.getCenterOfMassPosition(centerOfMass);
			return centerOfMass;
		}
	}

	private Quat4f quatRotation;
	private JavaQuaternion rotation;

	@Override
	public IQuaternion getRotation() {
		synchronized (physicsWorld) {
			return rotation.set(rigidBody.getOrientation(quatRotation));
		}
	}

	private Transform worldTransform;
	private JavaVector3 position;

	@Override
	public IVector3 getPosition() {
		synchronized (physicsWorld) {
			return position.set(rigidBody.getWorldTransform(worldTransform).origin);
		}
	}

	@Override
	public void applyTorque(final Vector3f vector) {
		synchronized (physicsWorld) {
			rigidBody.applyTorque(vector);
		}
	}

	@Override
	public void applyTorqueImpulse(final Vector3f vector) {
		synchronized (physicsWorld) {
			rigidBody.applyTorqueImpulse(vector);
		}
	}

}
