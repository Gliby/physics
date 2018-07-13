package gliby.minecraft.physics.common.physics.engine.nativebullet;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
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
class NativeRigidBody extends NativeCollisionObject implements IRigidBody {

	private btRigidBody rigidBody;

	private ICollisionShape collisionShape;
	private Map<String, Object> properties;

	public NativeRigidBody(PhysicsWorld physicsWorld, btRigidBody rigidBody, Entity entity) {
		super(physicsWorld, entity, rigidBody);
		this.physicsWorld = (NativePhysicsWorld) physicsWorld;
		this.rigidBody = rigidBody;
		this.collisionShape = new NativeCollisionShape(physicsWorld, rigidBody.getCollisionShape());
		this.properties = new HashMap<String, Object>();
		this.rotation = new NativeQuaternion(rigidBody.getOrientation());
		this.vectorPosition = new Vector3();
		this.position = new NativeVector3(vectorPosition);
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
		synchronized (physicsWorld) {
			return rigidBody.isActive();
		}
	}

	@Override
	public Vector3f getAngularVelocity(Vector3f vector3f) {
		synchronized (physicsWorld) {
			vector3f.set(rigidBody.getAngularVelocity().x, rigidBody.getAngularVelocity().y,
					rigidBody.getAngularVelocity().z);
		}
		return vector3f;
	}

	@Override
	public Vector3f getLinearVelocity(Vector3f vector3f) {
		synchronized (physicsWorld) {
			vector3f.set(rigidBody.getLinearVelocity().x, rigidBody.getLinearVelocity().y,
					rigidBody.getLinearVelocity().z);
			return vector3f;
		}
	}

	@Override
	public Vector3f getCenterOfMassPosition(Vector3f centerOfMass) {
		synchronized (physicsWorld) {
			centerOfMass.set(rigidBody.getCenterOfMassPosition().x, rigidBody.getCenterOfMassPosition().y,
					rigidBody.getCenterOfMassPosition().z);
			return centerOfMass;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.gliby.physics.common.physics.IRigidBody#getWorldTransform(com.
	 * bulletphysics.linearmath.Transform)
	 */
	@Override
	public Transform getWorldTransform(Transform transform) {
		synchronized (physicsWorld) {
			Matrix4 worldMatrix4 = rigidBody.getWorldTransform();
			transform.set(NativePhysicsWorld.toMatrix4f(worldMatrix4));
			return transform;
		}
	}

	public void setWorldTransform(final Transform transform) {
		synchronized (this) {
			rigidBody.setWorldTransform(physicsWorld.fromTransformToMatrix4(transform));
		}
	}

	@Override
	public void setGravity(final Vector3f acceleration) {
		synchronized (this) {
			rigidBody.setGravity(NativePhysicsWorld.toVector3(acceleration));
		}

	}

	@Override
	public void setFriction(final float friction) {
		synchronized (this) {
			rigidBody.setFriction(friction);
		}
	}

	@Override
	public void setLinearVelocity(final Vector3f linearVelocity) {
		synchronized (this) {
			rigidBody.setLinearVelocity(NativePhysicsWorld.toVector3(linearVelocity));
		}
	}

	@Override
	public void setAngularVelocity(final Vector3f angularVelocity) {
		synchronized (this) {
			rigidBody.setAngularVelocity(NativePhysicsWorld.toVector3(angularVelocity));
		}
	}

	@Override
	public void applyCentralImpulse(final Vector3f direction) {
		synchronized (this) {
			rigidBody.applyCentralImpulse(NativePhysicsWorld.toVector3(direction));
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
			Vector3 min = new Vector3(), max = new Vector3();
			rigidBody.getAabb(min, max);
			aabbMin.set(NativePhysicsWorld.toVector3f(min));
			aabbMin.set(NativePhysicsWorld.toVector3f(max));
		}
	}

	@Override
	public Transform getCenterOfMassTransform(Transform transform) {
		transform.set(NativePhysicsWorld.toMatrix4f(rigidBody.getCenterOfMassTransform()));
		return transform;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public void applyCentralForce(final Vector3f force) {
		synchronized (physicsWorld) {
			rigidBody.applyCentralForce(NativePhysicsWorld.toVector3(force));
		}
	}

	@Override
	public Vector3f getGravity(Vector3f vector3f) {
		vector3f.set(NativePhysicsWorld.toVector3f(rigidBody.getGravity()));
		return vector3f;
	}

	private Vector3 centerOfMass;

	@Override
	public Vector3f getCenterOfMassPosition() {
		if (centerOfMass == null)
			centerOfMass = new Vector3();
		centerOfMass.set(rigidBody.getCenterOfMassPosition());
		return NativePhysicsWorld.toStaticVector3f(centerOfMass);
	}

	private NativeQuaternion rotation;

	@Override
	public IQuaternion getRotation() {
		return rotation.set(rigidBody.getOrientation());
	}

	private NativeVector3 position;
	private Vector3 vectorPosition;

	@Override
	public IVector3 getPosition() {
		return position.set(rigidBody.getWorldTransform().getTranslation(vectorPosition));
	}

	@Override
	public void applyTorque(final Vector3f vector) {
		synchronized (physicsWorld) {
			rigidBody.applyTorque(NativePhysicsWorld.toVector3(vector));
		}
	}

	@Override
	public void applyTorqueImpulse(final Vector3f vector) {
		synchronized (physicsWorld) {
			rigidBody.applyTorqueImpulse(NativePhysicsWorld.toVector3(vector));
		}
	}
}
