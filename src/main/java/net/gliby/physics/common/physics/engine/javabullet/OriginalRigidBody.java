package net.gliby.physics.common.physics.engine.javabullet;

/**
 * Copyright (c) 2015, Mine Fortress.
 *//*
package net.gliby.physics.common.physics.jbullet;

import javax.vecmath.Vector3f;

import net.gliby.physics.common.entity.EntityPhysicsBase;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;

*//**
 *
 *//*
public class OwnedRigidBody extends RigidBody {

	private final EntityPhysicsBase owner;

	*//**
	 * @param mass
	 * @param motionState
	 * @param collisionShape
	 * @param localInertia
	 *//*
	public OwnedRigidBody(EntityPhysicsBase owner, float mass, MotionState motionState, CollisionShape collisionShape, Vector3f localInertia) {
		super(mass, motionState, collisionShape, localInertia);
		this.owner = owner;
	}

	*//**
	 * @param mass
	 * @param motionState
	 * @param collisionShape
	 *//*
	public OwnedRigidBody(EntityPhysicsBase owner, float mass, MotionState motionState, CollisionShape collisionShape) {
		super(mass, motionState, collisionShape);
		this.owner = owner;
	}

	*//**
	 * @param constructionInfo
	 *//*
	public OwnedRigidBody(EntityPhysicsBase owner, RigidBodyConstructionInfo constructionInfo) {
		super(constructionInfo);
		this.owner = owner;
	}

	public EntityPhysicsBase getOwner() {
		return owner;
	}

}
*/