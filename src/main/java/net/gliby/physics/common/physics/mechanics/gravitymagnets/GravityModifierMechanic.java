/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.mechanics.gravitymagnets;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import net.gliby.physics.common.entity.EnumRigidBodyProperty;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.IRigidBody;
import net.gliby.physics.common.physics.mechanics.PhysicsMechanic;

/**
 *
 */
public class GravityModifierMechanic extends PhysicsMechanic {

	/**
	 * @param physicsWorld
	 * @param ticksPerSecond
	 */
	public GravityModifierMechanic(PhysicsWorld physicsWorld, boolean threaded, int ticksPerSecond) {
		super(physicsWorld, threaded, ticksPerSecond);
		gravityMagnets = new ArrayList<GravityMagnet>();
	}

	@Override
	public void update() {
		for (int i = 0; i < gravityMagnets.size(); i++) {
			GravityMagnet magnet = (GravityMagnet) gravityMagnets.get(i);
			Vector3f magnetPosition = magnet.getPosition();
			for (int j = 0; j < physicsWorld.getRigidBodies().size(); j++) {
				IRigidBody body = physicsWorld.getRigidBodies().get(j);
				Vector3f centerOfMassPosition = body.getCenterOfMassPosition();
				Vector3f distance = new Vector3f();
				distance.sub(centerOfMassPosition, magnetPosition);
				float dist = distance.length();
				if (distance.length() <= magnet.getAttractionDistance()) {
					if (magnet.onlyChangeDirection) {
						Vector3f direction = new Vector3f(magnet.gravityDirection);
						direction.scale(magnet.getAttractionPower());
						if (!body.isActive())
							body.activate();
						body.setGravity(direction);
					} else {
						Vector3f direction = new Vector3f();
						direction.sub(magnetPosition, centerOfMassPosition);
						direction.normalize();
						direction.scale(magnet.getAttractionPower());
						if (!body.isActive())
							body.activate();
						body.setGravity(direction);
					}
					body.getProperties().put(EnumRigidBodyProperty.MAGNET.getName(), magnet);
				}
			}
		}
	}

	private ArrayList<GravityMagnet> gravityMagnets;

	/**
	 * @param player
	 */
	public synchronized GravityMagnet addGravityMagnet(GravityMagnet entity) {
		gravityMagnets.add(entity);
		return entity;
	}

	public synchronized void removeGravityMagnet(GravityMagnet magnet) {
		gravityMagnets.remove(magnet);
		for (int i = 0; i < physicsWorld.getRigidBodies().size(); i++) {
			IRigidBody body = physicsWorld.getRigidBodies().get(i);
			if (body.getProperties().get(EnumRigidBodyProperty.MAGNET.getName()) == magnet) {
				body.setGravity(physicsWorld.getPhysicsConfiguration().getRegularGravity());
				body.getProperties().remove(EnumRigidBodyProperty.MAGNET.getName());
			}
		}
	}

	public synchronized boolean gravityMagnetExists(GravityMagnet entity) {
		return gravityMagnets.contains(entity);
	}

	@Override
	public String getName() {
		return "GravityMagnetMechanic";
	}

	@Override
	public void init() {
	}

	public static class GravityMagnet {

		private boolean onlyChangeDirection;

		/**
		 * @return the onlyChangeDirection
		 */
		public boolean isOnlyChangeDirection() {
			return onlyChangeDirection;
		}

		/**
		 * @param onlyChangeDirection
		 *            the onlyChangeDirection to set
		 */
		public void setOnlyChangeDirection(boolean onlyChangeDirection) {
			this.onlyChangeDirection = onlyChangeDirection;
		}

		/**
		 * @param position
		 * @param attractionDistance
		 * @param attractionPower
		 */
		public GravityMagnet(Vector3f position, int attractionDistance, float attractionPower) {
			this.position = position;
			this.attractionDistance = attractionDistance;
			this.attractionPower = attractionPower;
		}

		private Vector3f gravityDirection;

		public GravityMagnet(Vector3f position, Vector3f gravity, int attractionDistance, float attractionPower) {
			this.position = position;
			this.gravityDirection = gravity;
			this.attractionDistance = attractionDistance;
			this.attractionPower = attractionPower;
			this.onlyChangeDirection = true;
		}

		private Vector3f position;
		private int attractionDistance;

		/**
		 * @return the position
		 */
		public Vector3f getPosition() {
			return position;
		}

		/**
		 * @param position
		 *            the position to set
		 */
		public void setPosition(Vector3f position) {
			this.position = position;
		}

		/**
		 * @return the attractionDistance
		 */
		public int getAttractionDistance() {
			return attractionDistance;
		}

		/**
		 * @param attractionDistance
		 *            the attractionDistance to set
		 */
		public void setAttractionDistance(int attractionDistance) {
			this.attractionDistance = attractionDistance;
		}

		/**
		 * @return the attractionPower
		 */
		public float getAttractionPower() {
			return attractionPower;
		}

		/**
		 * @param attractionPower
		 *            the attractionPower to set
		 */
		public void setAttractionPower(float attractionPower) {
			this.attractionPower = attractionPower;
		}

		private float attractionPower;

	}

}
