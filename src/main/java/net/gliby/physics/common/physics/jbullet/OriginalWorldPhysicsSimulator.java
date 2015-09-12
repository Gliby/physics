package net.gliby.physics.common.physics.jbullet;
/**
 * Copyright (c) 2015, Mine Fortress.
 *//*
package net.gliby.physics.common.physics.jbullet;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import net.gliby.physics.common.entity.EntityPhysicsBase;
import net.gliby.physics.common.physics.PhysicsOverworld;
import net.gliby.physics.common.physics.worldmechanics.EntityCollisionResponseMechanic;
import net.gliby.physics.common.physics.worldmechanics.PhysicsMechanic;
import net.gliby.physics.common.physics.worldmechanics.gravitymagnets.GravityMagnetMechanic;
import net.gliby.physics.common.physics.worldmechanics.physicsgun.PickUpMechanic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.RayResultCallback;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.voxel.VoxelWorldShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

*//**
 *
 *//*
public class OldWorldPhysicsSimulator implements Runnable {

	public final Vector3f waterGravity = new Vector3f(0.1f, -2.9f, 0.1f), gravity = new Vector3f(0, -9.8f, 0);
	private boolean ticking = true;

	private World world;
	protected DiscreteDynamicsWorld dynamicsWorld;
	protected PhysicsOverworld physicsOverworld;

	// TODO: Settings!
	protected int ticksPerSecond = 66;

	public GravityMagnetMechanic gravityMechanic;
	public PickUpMechanic pickUpMechanic;
	public EntityCollisionResponseMechanic entityResponse;

	public OldWorldPhysicsSimulator(PhysicsOverworld physicsOverworld, World world) {
		this.physicsOverworld = physicsOverworld;
		this.world = world;
		worldMechanics = new ArrayList<PhysicsMechanic>();
		worldMechanics.add(gravityMechanic = new GravityMagnetMechanic(this, this.ticksPerSecond));
		worldMechanics.add(pickUpMechanic = new PickUpMechanic(this, this.ticksPerSecond));
		worldMechanics.add(entityResponse = new EntityCollisionResponseMechanic(this.world, this, 20));
	}

	@Override
	public void run() {
		init();
		tick();
	}

	*//**
	 * 
	 *//*
	private void tick() {
		while (ticking) {
			synchronized (this) {
				try {
					wait(1000 / ticksPerSecond);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				float delta = getDelta();
				if (!world.playerEntities.isEmpty()) {
					dynamicsWorld.stepSimulation(1, Math.round(delta / 7));
				}
				updateFPS();
			}
			// }
			// }
		}
	}

	*//**
	 * 
	 *//*
	private void init() {
		rigidBodies = new ArrayList<RigidBody>();
		DbvtBroadphase broadphase = new DbvtBroadphase();
		broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		CollisionConfiguration defaultCollisionConfiguration = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(defaultCollisionConfiguration);
		this.dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, new SequentialImpulseConstraintSolver(), defaultCollisionConfiguration);
		this.dynamicsWorld.setGravity(gravity);

		Matrix3f rot = new Matrix3f();
		rot.setIdentity();
		Transform identityTransform = new Transform(new Matrix4f(rot, new Vector3f(0, 0, 0), 1.0f));

		// Create block collision connection to bullet.
		VoxelWorldShape blockCollisionHandler = new VoxelWorldShape(new BlockPhysicsConnection(physicsOverworld, world));
		blockCollisionHandler.calculateLocalInertia(0, new Vector3f());
		RigidBodyConstructionInfo blockConsInf = new RigidBodyConstructionInfo(0, new DefaultMotionState(identityTransform), blockCollisionHandler, new Vector3f());
		RigidBody blockCollisionBody = new RigidBody(blockConsInf);
		blockCollisionBody.setCollisionFlags(CollisionFlags.STATIC_OBJECT | blockCollisionBody.getCollisionFlags());
		dynamicsWorld.addRigidBody(blockCollisionBody);

		getDelta();
		lastFPS = getTime();
		for (int i = 0; i < worldMechanics.size(); i++) {
			PhysicsMechanic mechanic = worldMechanics.get(i);
			mechanic.init();
			new Thread(mechanic, mechanic.getName()).start();
		}

	}

	ArrayList<PhysicsMechanic> worldMechanics;

	*//**
	 * Adds rigid body to physics simulator(discrete dynamic world).
	 *//*
	public void addRigidBody(RigidBody body) {
		synchronized (this) {
			this.dynamicsWorld.addRigidBody(body);
			this.rigidBodies.add(body);
		}
	}

	*//**
	 * @param rigidBody
	 *//*
	public void removeRigidBody(RigidBody rigidBody) {
		synchronized (this) {
			this.dynamicsWorld.removeRigidBody(rigidBody);
			this.rigidBodies.remove(rigidBody);
		}
	}

	*//**
	 * Wakes up area defined by absolute min, max.
	 *//*
	public void awakenArea(final Vector3f min, final Vector3f max) {
		synchronized (this) {
			dynamicsWorld.awakenRigidBodiesInArea(min, max);
		}
	}

	public synchronized void rayTest(Vector3f rayFromWorld, Vector3f rayToWorld, RayResultCallback resultCallback) {
		this.dynamicsWorld.rayTest(rayFromWorld, rayToWorld, resultCallback);
	}

	public synchronized void removeCollisionObject(CollisionObject collisionObject) {
		this.dynamicsWorld.removeCollisionObject(collisionObject);
	}

	public void setGravity(final Vector3f newGravity) {
		synchronized (this) {
			dynamicsWorld.setGravity(newGravity);
		}
	}

	*//** time at last frame *//*
	long lastFrame;

	*//** frames per second *//*
	int fps;
	*//** last fps time *//*
	long lastFPS;

	protected float getDelta() {
		long time = getTime();
		int delta = (int) (time - lastFrame);
		lastFrame = time;
		return delta;
	}

	protected long getTime() {
		return System.currentTimeMillis();
	}

	float stepsPerSecond;
	private List<RigidBody> rigidBodies;

	protected void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			stepsPerSecond = fps;
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

	*//**
	 * @return
	 *//*
	public float getStepsPerSecond() {
		return stepsPerSecond;
	}

	*//**
	 * @return
	 *//*
	public synchronized DynamicsWorld getDiscreteDynamicWorld() {
		return dynamicsWorld;
	}

	public synchronized void addCollisionObject(CollisionObject object) {
		dynamicsWorld.addCollisionObject(object);
	}

	public synchronized void addCollisionObject(CollisionObject collisionObject, short collisionFilterGroup, short collisionFilterMask) {
		dynamicsWorld.addCollisionObject(collisionObject, collisionFilterGroup, collisionFilterMask);
	}

	public static Vector3f calculateRay(Entity base, float distance, Vector3f offset) {
		Vec3 vec3 = base.getPositionVector().subtract(offset.x, offset.y, offset.z);
		Vec3 vec31 = base.getLook(1);
		Vec3 vec32 = vec3.addVector(vec31.xCoord * distance, vec31.yCoord * distance, vec31.zCoord * distance);
		return new Vector3f((float) vec32.xCoord, (float) vec32.yCoord, (float) vec32.zCoord);
	}

	public void dispose() {
		for (int i = 0; i < worldMechanics.size(); i++) {
			PhysicsMechanic mechanic = worldMechanics.get(i);
			mechanic.setEnabled(false);
		}
		rigidBodies.clear();
		this.ticking = false;
	}

	*//**
	 * @return
	 *//*
	public List<RigidBody> getRigidBodies() {
		return rigidBodies;
	}

	public static OwnedRigidBody constructRigidBody(EntityPhysicsBase owner, float mass, Transform startTransform, CollisionShape shape) {
		boolean isDynamic = (mass != 0f);
		Vector3f localInertia = new Vector3f(0f, 0f, 0f);
		if (isDynamic) shape.calculateLocalInertia(mass, localInertia);
		DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
		RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(mass, myMotionState, shape, localInertia);
		OwnedRigidBody body = new OwnedRigidBody(owner, cInfo);
		return body;
	}

	public static CollisionShape constructEntityShape(World world, Vec3 pos, AxisAlignedBB bb) {
		CompoundShape compoundShape = new CompoundShape();
		AxisAlignedBB relativeBB = AxisAlignedBB.fromBounds((bb.minX - pos.xCoord * 0.5f), (bb.minY - pos.yCoord * 0.5f), (bb.minZ - pos.zCoord * 0.5f), (bb.maxX - pos.xCoord * 0.5f), (bb.maxY - pos.yCoord * 0.5f), (bb.maxZ - pos.zCoord * 0.5f));
		Vector3f extents = new Vector3f((float) relativeBB.maxX - (float) relativeBB.minX, (float) relativeBB.maxY - (float) relativeBB.minY, (float) relativeBB.maxZ - (float) relativeBB.minZ);
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set((float) relativeBB.minX + (float) relativeBB.maxX - 0.5f, (float) relativeBB.minY + (float) relativeBB.maxY - 0.5f, (float) relativeBB.minZ + (float) relativeBB.maxZ - 0.5f);
		compoundShape.addChildShape(transform, new BoxShape(extents));
		return compoundShape;
	}

	public static CollisionShape constructBlockShape(World world, BlockPos blockPos, IBlockState state) {
		if (!state.getBlock().isNormalCube()) {
			List<AxisAlignedBB> boundingBoxes = new ArrayList<AxisAlignedBB>();
			state.getBlock().addCollisionBoxesToList(
					world,
					blockPos,
					state,
					AxisAlignedBB.fromBounds((double) blockPos.getX() + state.getBlock().getBlockBoundsMinX(), (double) blockPos.getY() + state.getBlock().getBlockBoundsMinY(), (double) blockPos.getZ() + state.getBlock().getBlockBoundsMinZ(), (double) blockPos.getX() + state.getBlock().getBlockBoundsMaxX(), (double) blockPos.getY() + +state.getBlock().getBlockBoundsMaxY(),
							(double) blockPos.getZ() + +state.getBlock().getBlockBoundsMaxZ()), boundingBoxes, null);
			CompoundShape compoundShape = new CompoundShape();
			for (AxisAlignedBB bb : boundingBoxes) {
				AxisAlignedBB relativeBB = AxisAlignedBB.fromBounds((bb.minX - blockPos.getX()) * 0.5f, (bb.minY - blockPos.getY()) * 0.5f, (bb.minZ - blockPos.getZ()) * 0.5f, (bb.maxX - blockPos.getX()) * 0.5f, (bb.maxY - blockPos.getY()) * 0.5f, (bb.maxZ - blockPos.getZ()) * 0.5f);
				Vector3f extents = new Vector3f((float) relativeBB.maxX - (float) relativeBB.minX, (float) relativeBB.maxY - (float) relativeBB.minY, (float) relativeBB.maxZ - (float) relativeBB.minZ);
				Transform transform = new Transform();
				transform.setIdentity();
				transform.origin.set((float) relativeBB.minX + (float) relativeBB.maxX - 0.5f, (float) relativeBB.minY + (float) relativeBB.maxY - 0.5f, (float) relativeBB.minZ + (float) relativeBB.maxZ - 0.5f);
				compoundShape.addChildShape(transform, new BoxShape(extents));
			}
			if (!compoundShape.getChildList().isEmpty()) return compoundShape;
		}
		Vector3f blockPosition = new Vector3f((float) state.getBlock().getBlockBoundsMaxX(), (float) state.getBlock().getBlockBoundsMaxY(), (float) state.getBlock().getBlockBoundsMaxZ());
		blockPosition.scale(0.5f);
		return new BoxShape(blockPosition);
	}

	public static List<CombinedShape> generateCombinedShapesFromBlock(World world, BlockPos blockPos, IBlockState state) {
		List<AxisAlignedBB> boundingBoxes = new ArrayList<AxisAlignedBB>();
		state.getBlock().addCollisionBoxesToList(
				world,
				blockPos,
				state,
				AxisAlignedBB.fromBounds((double) blockPos.getX() + state.getBlock().getBlockBoundsMinX(), (double) blockPos.getY() + state.getBlock().getBlockBoundsMinY(), (double) blockPos.getZ() + state.getBlock().getBlockBoundsMinZ(), (double) blockPos.getX() + state.getBlock().getBlockBoundsMaxX(), (double) blockPos.getY() + +state.getBlock().getBlockBoundsMaxY(), (double) blockPos.getZ()
						+ +state.getBlock().getBlockBoundsMaxZ()), boundingBoxes, null);
		List<CombinedShape> shapes = new ArrayList<CombinedShape>();
		for (AxisAlignedBB bb : boundingBoxes) {
			AxisAlignedBB relativeBB = AxisAlignedBB.fromBounds((bb.minX - blockPos.getX()) * 0.5f, (bb.minY - blockPos.getY()) * 0.5f, (bb.minZ - blockPos.getZ()) * 0.5f, (bb.maxX - blockPos.getX()) * 0.5f, (bb.maxY - blockPos.getY()) * 0.5f, (bb.maxZ - blockPos.getZ()) * 0.5f);
			Vector3f extents = new Vector3f((float) relativeBB.maxX - (float) relativeBB.minX, (float) relativeBB.maxY - (float) relativeBB.minY, (float) relativeBB.maxZ - (float) relativeBB.minZ);
			Transform transform = new Transform();
			transform.setIdentity();
			transform.origin.set((float) relativeBB.minX + (float) relativeBB.maxX - 0.5f, (float) relativeBB.minY + (float) relativeBB.maxY - 0.5f, (float) relativeBB.minZ + (float) relativeBB.maxZ - 0.5f);
			BoxShape shape = new BoxShape(extents);
			shapes.add(new CombinedShape(new Vector3f(), transform, shape));
		}
		return shapes;
	}

	*//**
	 * @return
	 *//*
	public PhysicsOverworld getOverworld() {
		return physicsOverworld;
	}

	*//**
	 * @return
	 *//*
	public final Vector3f getGravity() {
		return gravity;
	}

	*//**
	 * @param mechanic
	 *//*
	public void restartMechanic(PhysicsMechanic mechanic) {
		new Thread(mechanic, mechanic.getName()).start();
	}

}
*/