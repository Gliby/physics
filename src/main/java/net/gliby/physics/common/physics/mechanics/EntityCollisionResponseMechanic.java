/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.mechanics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.linearmath.Transform;

import net.gliby.physics.Physics;
import net.gliby.physics.common.entity.IEntityPhysics;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.IGhostObject;
import net.gliby.physics.common.physics.engine.IRigidBody;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/**
 *
 */
public class EntityCollisionResponseMechanic extends PhysicsMechanic {

	private World world;

	/**
	 * @param physicsWorld
	 * @param ticksPerSecond
	 */
	public EntityCollisionResponseMechanic(World world, PhysicsWorld physicsWorld, boolean threaded,
			int ticksPerSecond) {
		super(physicsWorld, threaded, ticksPerSecond);
		this.world = world;
	}

	private Map<Integer, Long> timeAdded = new HashMap<Integer, Long>();
	private Map<Integer, IGhostObject> ghostObjects = new HashMap<Integer, IGhostObject>();

	private Transform entityTransform = new Transform();

	@Override
	public void update() {
		for (int i = 0; i < physicsWorld.getRigidBodies().size(); i++) {
			IRigidBody rigidBody = physicsWorld.getRigidBodies().get(i);
			Entity entityBody = rigidBody.getOwner();

			Vector3f minBB, maxBB;
			// Get rigidBody BB.
			rigidBody.getAabb(minBB = new Vector3f(), maxBB = new Vector3f());
			// Create AABB, and offset.
			AxisAlignedBB axisAlignedBB = AxisAlignedBB.fromBounds(minBB.x, minBB.y, minBB.z, maxBB.x, maxBB.y, maxBB.z)
					.offset(0.5f, 0.5f, 0.5f);
			List<Entity> intersectingEntites;
			MinecraftServer server = MinecraftServer.getServer();
			synchronized (server) {
				intersectingEntites = world.getEntitiesWithinAABB(Entity.class, axisAlignedBB,
						IEntityPhysics.NOT_PHYSICS_OBJECT);
			}

			for (Entity entity : intersectingEntites) {
				entityTransform.setIdentity();
				entityTransform.origin.set(new Vector3f((float) entity.posX - 0.5f, (float) entity.posY + 0.25f,
						(float) entity.posZ - 0.5f));

				if (entity instanceof IProjectile) {
					Vector3f direction = new Vector3f();
					direction.setX((float) (entity.motionX));
					direction.setY((float) (entity.motionY));
					direction.setZ((float) (entity.motionZ));
					boolean moving = direction.length() > 0;
					if (moving) {
						direction.scale(Physics.getInstance().getSettings().getFloatSetting("ProjectileImpulseForce")
								.getFloatValue());
						rigidBody.applyCentralImpulse(direction);
						rigidBody.activate();
					}
					continue;
				}

				IGhostObject ghostObject;
				if ((ghostObject = ghostObjects.get(entity.getEntityId())) != null) {

					// Update
					ghostObject.setWorldTransform(entityTransform);
					timeAdded.put(entity.getEntityId(), System.currentTimeMillis());
				} else {
					ghostObject = physicsWorld.createPairCachingGhostObject();
					ghostObject.setInterpolationWorldTransform(entityTransform);
					ghostObject.setWorldTransform(entityTransform);
					// Create ghost object
					Vector3f bb = new Vector3f(
							(float) entity.getEntityBoundingBox().maxX - (float) entity.getEntityBoundingBox().minX,
							(float) entity.getEntityBoundingBox().maxY - (float) entity.getEntityBoundingBox().minY,
							(float) entity.getEntityBoundingBox().maxZ - (float) entity.getEntityBoundingBox().minZ);
					bb.scale(0.5f);

					ghostObject.setCollisionShape(physicsWorld.createBoxShape(bb));
					ghostObject.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);
					physicsWorld.addCollisionObject(ghostObject, CollisionFilterGroups.CHARACTER_FILTER,
							(short) (CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));
					ghostObjects.put(entity.getEntityId(), ghostObject);
					timeAdded.put(entity.getEntityId(), System.currentTimeMillis());
				}
			}
		}

		Iterator<Map.Entry<Integer, Long>> iterator = timeAdded.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Long> entry = iterator.next();
			int entityId = entry.getKey();
			long timeAdded = entry.getValue();
			// TODO Settings value MS
			if ((System.currentTimeMillis() - timeAdded) / 1000 > Physics.getInstance().getSettings()
					.getFloatSetting("PhysicsEntities.EntityColliderCleanupTime").getFloatValue()) {
				physicsWorld.removeCollisionObject(ghostObjects.get(entityId));
				ghostObjects.remove(entityId);
				iterator.remove();
			}
		}
	}

	@Override
	public String getName() {
		return "EntityResponseMechanic";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.gliby.physics.common.physics.worldmechanics.PhysicsMechanic#init()
	 */
	@Override
	public void init() {
	}

}
