/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.nativebullet;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.bulletphysics.linearmath.Transform;

import net.gliby.physics.common.physics.ICollisionObject;
import net.gliby.physics.common.physics.ICollisionShape;
import net.gliby.physics.common.physics.IRigidBody;
import net.gliby.physics.common.physics.IVector3;
import net.gliby.physics.common.physics.transform.IQuaternion;
import net.minecraft.entity.Entity;

/**
 *
 */
class NativeRigidBody extends NativeCollisionObject implements IRigidBody {

	private Entity entity;
	private btRigidBody rigidBody;

	private ICollisionShape collisionShape;
	private Map<String, Object> properties;

	public NativeRigidBody(btRigidBody rigidBody, Entity entity) {
		super(rigidBody);
		this.rigidBody = rigidBody;
		this.entity = entity;
		this.collisionShape = new NativeCollisionShape(rigidBody.getCollisionShape());
		this.properties = new HashMap<String, Object>();
		this.rotation = new NativeQuaternion(rigidBody.getOrientation());
		this.vectorPosition = new Vector3();
		this.position = new NativeVector3(vectorPosition);
	}

	@Override
	public Entity getOwner() {
		return entity;
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
	public Vector3f getAngularVelocity(Vector3f vector3f) {
		vector3f.set(rigidBody.getAngularVelocity().x, rigidBody.getAngularVelocity().y,
				rigidBody.getAngularVelocity().z);
		return vector3f;
	}

	@Override
	public Vector3f getLinearVelocity(Vector3f vector3f) {
		vector3f.set(rigidBody.getLinearVelocity().x, rigidBody.getLinearVelocity().y, rigidBody.getLinearVelocity().z);
		return vector3f;
	}

	@Override
	public Vector3f getCenterOfMassPosition(Vector3f centerOfMass) {
		centerOfMass.set(rigidBody.getCenterOfMassPosition().x, rigidBody.getCenterOfMassPosition().y,
				rigidBody.getCenterOfMassPosition().z);
		return centerOfMass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.gliby.physics.common.physics.IRigidBody#getWorldTransform(com.
	 * bulletphysics.linearmath.Transform)
	 */
	@Override
	public Transform getWorldTransform(Transform transform) {
		Matrix4 worldMatrix4 = rigidBody.getWorldTransform();
		transform.set(NativePhysicsWorld.toMatrix4f(worldMatrix4));
		return transform;
	}

	public void setWorldTransform(Transform transform) {
		rigidBody.setWorldTransform(NativePhysicsWorld.fromTransformToMatrix4(transform));
	}

	@Override
	public void setGravity(Vector3f acceleration) {
		rigidBody.setGravity(NativePhysicsWorld.toVector3(acceleration));
	}

	@Override
	public void setFriction(float friction) {
		rigidBody.setFriction(friction);
	}

	@Override
	public void setLinearVelocity(Vector3f linearVelocity) {
		rigidBody.setLinearVelocity(NativePhysicsWorld.toVector3(linearVelocity));
	}

	@Override
	public void setAngularVelocity(Vector3f angularVelocity) {
		rigidBody.setAngularVelocity(NativePhysicsWorld.toVector3(angularVelocity));
	}

	@Override
	public void applyCentralImpulse(Vector3f direction) {
		rigidBody.applyCentralImpulse(NativePhysicsWorld.toVector3(direction));
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
	public void getAabb(Vector3f aabbMin, Vector3f aabbMax) {
		Vector3 min = new Vector3(), max = new Vector3();
		rigidBody.getAabb(min, max);
		aabbMin.set(NativePhysicsWorld.toVector3f(min));
		aabbMin.set(NativePhysicsWorld.toVector3f(max));
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
	public void applyCentralForce(Vector3f force) {
		rigidBody.applyCentralForce(NativePhysicsWorld.toVector3(force));
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

}
