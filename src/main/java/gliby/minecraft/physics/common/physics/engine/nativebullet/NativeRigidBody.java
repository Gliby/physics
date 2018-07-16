package gliby.minecraft.physics.common.physics.engine.nativebullet;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

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
	public boolean hasContactResponse() {
		return rigidBody.hasContactResponse();
	}

	@Override
	public float getInvMass() {
		return rigidBody.getInvMass();
	}

	@Override
	public void activate() {
		rigidBody.activate();
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public Vector3 getAngularVelocity(Vector3 vector) {
		vector.set(this.rigidBody.getAngularVelocity());
		return vector;
	}

	@Override
	public Vector3 getLinearVelocity(Vector3 vector) {
		vector.set(this.rigidBody.getLinearVelocity());
		return vector;
	}

	@Override
	public Vector3 getCenterOfMassPosition(Vector3 vector) {
		vector.set(this.rigidBody.getCenterOfMassPosition());
		return vector;
	}

	@Override
	public Matrix4 getWorldTransform(Matrix4 transform) {
		rigidBody.getWorldTransform(transform);
		return transform;
	}

	@Override
	public void setWorldTransform(Matrix4 transform) {
		rigidBody.setWorldTransform(transform);
	}

	@Override
	public void setGravity(Vector3 vector) {
		rigidBody.setGravity(vector);

	}

	@Override
	public void setFriction(float friction) {
		rigidBody.setFriction(friction);
	}

	@Override
	public void setLinearVelocity(Vector3 linearVelocity) {
		rigidBody.setLinearVelocity(linearVelocity);
	}

	@Override
	public void setAngularVelocity(Vector3 angularVelocity) {
		rigidBody.setAngularVelocity(angularVelocity);

	}

	@Override
	public void applyCentralImpulse(Vector3 direction) {
		rigidBody.applyCentralImpulse(direction);

	}

	@Override
	public void getAabb(Vector3 min, Vector3 max) {
		rigidBody.getAabb(min, max);
	}

	@Override
	public Matrix4 getCenterOfMassTransform(Matrix4 transform) {
		transform.set(rigidBody.getCenterOfMassTransform());
		return transform;
	}

	@Override
	public void applyCentralForce(Vector3 force) {
		rigidBody.applyCentralForce(force);
	}

	@Override
	public Vector3 getGravity(Vector3 gravity) {
		gravity.set(rigidBody.getGravity());
		return gravity;
	}

	@Override
	public void applyTorque(Vector3 torque) {
		rigidBody.applyTorque(torque);
	}

	@Override
	public void applyTorqueImpulse(Vector3 torque) {
		rigidBody.applyTorqueImpulse(torque);
	}
}
