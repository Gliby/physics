/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.jbullet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.RayResultCallback;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.voxel.JBulletVoxelWorldShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysics.dynamics.constraintsolver.Point2PointConstraint;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SliderConstraint;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.google.gson.Gson;

import net.gliby.physics.Physics;
import net.gliby.physics.common.physics.ICollisionObject;
import net.gliby.physics.common.physics.ICollisionShape;
import net.gliby.physics.common.physics.IConstraint;
import net.gliby.physics.common.physics.IConstraintGeneric6Dof;
import net.gliby.physics.common.physics.IConstraintPoint2Point;
import net.gliby.physics.common.physics.IConstraintSlider;
import net.gliby.physics.common.physics.IGhostObject;
import net.gliby.physics.common.physics.IRayResult;
import net.gliby.physics.common.physics.IRigidBody;
import net.gliby.physics.common.physics.IRope;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.worldmechanics.PhysicsMechanic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 *
 */
public abstract class JavaPhysicsWorld extends PhysicsWorld {

	private List<IRigidBody> rigidBodies;
	private List<IConstraint> constraints;
	private DiscreteDynamicsWorld dynamicsWorld;

	/**
	 * @param ticksPerSecond
	 */
	public JavaPhysicsWorld(World world, int ticksPerSecond, Vector3f gravity) {
		super(world, ticksPerSecond, gravity);
	}

	@Override
	public void run() {
		getDelta();
		lastFPS = getTime();
		while (running) {
			synchronized (this) {
				try {
					wait(1000 / getTicksPerSecond());
				} catch (InterruptedException e) {
					Physics.getLogger().catching(e);
				}
				if (shouldSimulate())
					update();
				updateFPS();
			}
		}
	}

	@Override
	public void create() {
		ropes = new ArrayList<IRope>();
		rigidBodies = new ArrayList<IRigidBody>();
		constraints = new ArrayList<IConstraint>();
		final DbvtBroadphase broadphase = new DbvtBroadphase();
		broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		final CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		final CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

		this.dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, new SequentialImpulseConstraintSolver(),
				collisionConfiguration);
		this.dynamicsWorld.setGravity(gravity);

		Matrix3f rot = new Matrix3f();
		rot.setIdentity();
		Transform identityTransform = new Transform(new Matrix4f(rot, new Vector3f(0, 0, 0), 1.0f));

		// Create block collision connection to bullet.
		JBulletVoxelWorldShape blockCollisionHandler = new JBulletVoxelWorldShape(
				new JavaBlockCollision(this, world));
		blockCollisionHandler.calculateLocalInertia(0, new Vector3f());
		RigidBodyConstructionInfo blockConsInf = new RigidBodyConstructionInfo(0,
				new DefaultMotionState(identityTransform), blockCollisionHandler, new Vector3f());
		RigidBody blockCollisionBody = new RigidBody(blockConsInf);
		blockCollisionBody.setCollisionFlags(CollisionFlags.STATIC_OBJECT | blockCollisionBody.getCollisionFlags());
		dynamicsWorld.addRigidBody(blockCollisionBody);
		super.create();
	}

	@Override
	public void update() {
		float delta = getDelta();
		if (!world.playerEntities.isEmpty()) {
			if (dynamicsWorld != null)
				dynamicsWorld.stepSimulation(1, Math.round(delta / 7));
		}
		super.update();
	}

	@Override
	public void addRigidBody(IRigidBody body) {
		synchronized (this) {
			this.dynamicsWorld.addRigidBody((RigidBody) body.getBody());
			this.rigidBodies.add(body);
		}
	}

	@Override
	public void addRigidBody(IRigidBody body, short collisionFilterGroup, short collisionFilterMask) {
		synchronized (this) {
			this.dynamicsWorld.addRigidBody((RigidBody) body.getBody(), collisionFilterGroup, collisionFilterMask);
			this.rigidBodies.add(body);
		}
	}

	@Override
	public void removeRigidBody(IRigidBody body) {
		synchronized (this) {
			this.dynamicsWorld.removeRigidBody((RigidBody) body.getBody());
			this.rigidBodies.remove(body);
		}
	}

	@Override
	public void awakenArea(Vector3f min, Vector3f max) {
		synchronized (this) {
			if (dynamicsWorld != null)
				this.dynamicsWorld.awakenRigidBodiesInArea(min, max);
		}
	}

	@Override
	public void rayTest(Vector3f rayFromWorld, Vector3f rayToWorld, IRayResult resultCallback) {
		synchronized (this) {
			this.dynamicsWorld.rayTest(rayFromWorld, rayToWorld,
					(RayResultCallback) resultCallback.getRayResultCallback());
		}
	}

	@Override
	public void removeCollisionObject(ICollisionObject collisionObject) {
		synchronized (this) {
			this.dynamicsWorld.removeCollisionObject((CollisionObject) collisionObject.getCollisionObject());
		}
	}

	@Override
	public void setGravity(Vector3f newGravity) {
		synchronized (this) {
			this.dynamicsWorld.setGravity(newGravity);
		}
	}

	@Override
	public void addCollisionObject(ICollisionObject object) {
		synchronized (this) {
			this.dynamicsWorld.addCollisionObject((CollisionObject) object.getCollisionObject());
		}
	}

	@Override
	public void addCollisionObject(ICollisionObject object, short collisionFilterGroup, short collisionFilterMask) {
		synchronized (this) {
			this.dynamicsWorld.addCollisionObject((CollisionObject) object.getCollisionObject(), collisionFilterGroup,
					collisionFilterMask);
		}
	}

	@Override
	public List<IRigidBody> getRigidBodies() {
		return rigidBodies;
	}

	@Override
	public void dispose() {
		Iterator it = physicsMechanics.entrySet().iterator();
		while (it.hasNext()) {
			PhysicsMechanic mechanic = ((Map.Entry<String, PhysicsMechanic>) it.next()).getValue();
			mechanic.setEnabled(false);
		}
		dynamicsWorld = null;
		rigidBodies.clear();
		this.running = false;
	}

	private long lastFrame;
	private int fps;
	private long lastFPS;

	protected float getDelta() {
		long time = getTime();
		int delta = (int) (time - lastFrame);
		lastFrame = time;
		return delta;
	}

	private float stepsPerSecond;

	protected void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			stepsPerSecond = fps;
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

	protected long getTime() {
		return System.currentTimeMillis();
	}

	@Override
	public IRigidBody createRigidBody(final Entity owner, Transform transform, float mass, ICollisionShape shape) {
		Vector3f localInertia = new Vector3f(0, 0, 0);
		if (mass != 0) {
			shape.calculateLocalInertia(mass, localInertia);
		}
		DefaultMotionState motionState = new DefaultMotionState(transform);
		RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(mass, motionState,
				(CollisionShape) shape.getCollisionShape(), localInertia);
		RigidBody body = new RigidBody(constructionInfo);
		return new JavaRigidBody(body, owner);
	}

	@Override
	public ICollisionShape createBoxShape(final Vector3f extents) {
		return new net.gliby.physics.common.physics.jbullet.JavaCollisionShape(new BoxShape(extents));
	}

	@Override
	public IRayResult createClosestRayResultCallback(final Vector3f rayFromWorld, final Vector3f rayToWorld) {
		return new JavaClosestRayResultCallback(
				new CollisionWorld.ClosestRayResultCallback(rayFromWorld, rayToWorld));
	}

	@Override
	public ICollisionShape createBlockShape(World worldObj, BlockPos blockPos, IBlockState blockState) {
		if (!blockState.getBlock().isNormalCube()) {
			List<AxisAlignedBB> boundingBoxes = new ArrayList<AxisAlignedBB>();
			blockState.getBlock().addCollisionBoxesToList(world, blockPos, blockState, MAX_BB, boundingBoxes, null);
			CompoundShape compoundShape = new CompoundShape();
			for (AxisAlignedBB bb : boundingBoxes) {
				AxisAlignedBB relativeBB = AxisAlignedBB.fromBounds((bb.minX - blockPos.getX()) * 0.5f,
						(bb.minY - blockPos.getY()) * 0.5f, (bb.minZ - blockPos.getZ()) * 0.5f,
						(bb.maxX - blockPos.getX()) * 0.5f, (bb.maxY - blockPos.getY()) * 0.5f,
						(bb.maxZ - blockPos.getZ()) * 0.5f);
				Vector3f extents = new Vector3f((float) relativeBB.maxX - (float) relativeBB.minX,
						(float) relativeBB.maxY - (float) relativeBB.minY,
						(float) relativeBB.maxZ - (float) relativeBB.minZ);
				Transform transform = new Transform();
				transform.setIdentity();
				transform.origin.set((float) relativeBB.minX + (float) relativeBB.maxX - 0.5f,
						(float) relativeBB.minY + (float) relativeBB.maxY - 0.5f,
						(float) relativeBB.minZ + (float) relativeBB.maxZ - 0.5f);
				compoundShape.addChildShape(transform, new BoxShape(extents));
			}
			if (!compoundShape.getChildList().isEmpty())
				return new JavaCollisionShape(compoundShape);
		}
		Vector3f blockPosition = new Vector3f((float) blockState.getBlock().getBlockBoundsMaxX(),
				(float) blockState.getBlock().getBlockBoundsMaxY(), (float) blockState.getBlock().getBlockBoundsMaxZ());
		blockPosition.scale(0.5f);
		return new JavaCollisionShape(new BoxShape(blockPosition));
	}

	@Override
	public IGhostObject createPairCachingGhostObject() {
		return new JavaPairCachingGhostObject(new PairCachingGhostObject());
	}

	@Override
	public IRigidBody upcastRigidBody(Object collisionObject) {
		for (int i = 0; i < rigidBodies.size(); i++) {
			IRigidBody body = rigidBodies.get(i);
			if (body.getBody() == RigidBody.upcast((CollisionObject) collisionObject)) {
				return body;
			} else
				continue;
		}
		return null;
	}

	@Override
	public IConstraintPoint2Point createPoint2PointConstraint(IRigidBody rigidBody, Vector3f relativePivot) {
		return new JavaConstraintPoint2Point(
				new Point2PointConstraint((RigidBody) rigidBody.getBody(), relativePivot));
	}

	@Override
	public void addConstraint(IConstraint constraint) {
		synchronized (this) {
			dynamicsWorld.addConstraint((TypedConstraint) constraint.getConstraint());
			constraints.add(constraint);
		}
	}

	@Override
	public void removeConstraint(IConstraint constraint) {
		synchronized (this) {
			dynamicsWorld.removeConstraint((TypedConstraint) constraint.getConstraint());
			constraints.remove(constraint);
		}
	}

	public String writeBlockCollisionShape(ICollisionShape collisionShape) {
		Gson gson = new Gson();
		ArrayList<CollisionPart> collisionParts = new ArrayList<CollisionPart>();
		if (collisionShape.isBoxShape()) {
			collisionParts.add(new CollisionPart(false, null,
					((BoxShape) collisionShape.getCollisionShape()).getOriginalExtent()));
		} else if (collisionShape.isCompoundShape()) {
			CompoundShape compoundShape = (CompoundShape) collisionShape.getCollisionShape();
			for (int i = 0; i < compoundShape.getChildList().size(); i++) {
				CompoundShapeChild child = compoundShape.getChildList().get(i);
				collisionParts.add(
						new CollisionPart(true, child.transform, ((BoxShape) child.childShape).getOriginalExtent()));
			}
		}
		return gson.toJson(collisionParts.toArray(), CollisionPart[].class);
	}

	public ICollisionShape readBlockCollisionShape(String json) {
		Gson gson = new Gson();
		List collisionParts = Arrays.asList(gson.fromJson(json, CollisionPart[].class));
		CompoundShape shape = new CompoundShape();
		if (collisionParts.size() == 1) {
			CollisionPart part = (CollisionPart) collisionParts.get(0);
			if (!part.compoundShape)
				return this.createBoxShape(part.extent);
		}

		for (int i = 0; i < collisionParts.size(); i++) {
			CollisionPart part = (CollisionPart) collisionParts.get(i);
			shape.addChildShape(part.transform, new BoxShape(part.extent));
		}
		return new JavaCollisionShape(shape);
	}

	@Override
	public List<IConstraint> getConstraints() {
		return constraints;
	}

	@Override
	public IConstraintGeneric6Dof createGeneric6DofConstraint(IRigidBody rbA, IRigidBody rbB, Transform frameInA,
			Transform frameInB, boolean useLinearReferenceFrameA) {
		return new JavaConstraintGeneric6Dof(new Generic6DofConstraint((RigidBody) rbA.getBody(),
				(RigidBody) rbB.getBody(), frameInA, frameInB, useLinearReferenceFrameA));
	}

	@Override
	public IRope createRope(Vector3f startPos, Vector3f endPos, int detail) {
		return new JavaRope(startPos, endPos, detail);
	}

	@Override
	public void addRope(IRope rope) {
		rope.create(this);
		ropes.add(rope);
	}

	private List<IRope> ropes;

	@Override
	public List<IRope> getRopes() {
		return ropes;
	}

	@Override
	public void removeRope(IRope rope) {
		rope.dispose(this);
		ropes.remove(rope);
	}

	@Override
	public String toString() {
		return "JBulletPhysicsWorld[ " + this.rigidBodies.size() + " rigid bodies" + "]";
	}

	@Override
	public ICollisionShape createSphereShape(float radius) {
		return new JavaCollisionShape(new SphereShape(radius));
	}

	@Override
	public IRigidBody createInertiallessRigidBody(Entity owner, Transform transform, float mass,
			ICollisionShape shape) {
		DefaultMotionState motionState = new DefaultMotionState(transform);
		RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(mass, motionState,
				(CollisionShape) shape.getCollisionShape());
		RigidBody body = new RigidBody(constructionInfo);
		return new JavaRigidBody(body, owner);
	}

	@Override
	public IConstraintSlider createSliderConstraint(IRigidBody rbA, IRigidBody rbB, Transform frameInA,
			Transform frameInB, boolean useLinearReferenceFrameA) {
		return new JavaConstraintSlider(new SliderConstraint((RigidBody) rbA.getBody(), (RigidBody) rbB.getBody(),
				frameInA, frameInB, useLinearReferenceFrameA));
	}
}
