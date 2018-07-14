package gliby.minecraft.physics.common.physics.engine.concurrent.javabullet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.bulletphysicsx.collision.broadphase.DbvtBroadphase;
import com.bulletphysicsx.collision.dispatch.CollisionConfiguration;
import com.bulletphysicsx.collision.dispatch.CollisionDispatcher;
import com.bulletphysicsx.collision.dispatch.CollisionFlags;
import com.bulletphysicsx.collision.dispatch.CollisionObject;
import com.bulletphysicsx.collision.dispatch.CollisionWorld;
import com.bulletphysicsx.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysicsx.collision.dispatch.GhostPairCallback;
import com.bulletphysicsx.collision.dispatch.PairCachingGhostObject;
import com.bulletphysicsx.collision.dispatch.CollisionWorld.RayResultCallback;
import com.bulletphysicsx.collision.shapes.BoxShape;
import com.bulletphysicsx.collision.shapes.CollisionShape;
import com.bulletphysicsx.collision.shapes.CompoundShape;
import com.bulletphysicsx.collision.shapes.CompoundShapeChild;
import com.bulletphysicsx.collision.shapes.SphereShape;
import com.bulletphysicsx.collision.shapes.voxel.JBulletVoxelWorldShape;
import com.bulletphysicsx.dynamics.DiscreteDynamicsWorld;
import com.bulletphysicsx.dynamics.RigidBody;
import com.bulletphysicsx.dynamics.RigidBodyConstructionInfo;
import com.bulletphysicsx.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysicsx.dynamics.constraintsolver.Point2PointConstraint;
import com.bulletphysicsx.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysicsx.dynamics.constraintsolver.SliderConstraint;
import com.bulletphysicsx.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysicsx.linearmath.DefaultMotionState;
import com.bulletphysicsx.linearmath.Transform;
import com.google.common.collect.Queues;
import com.google.gson.Gson;

import gliby.minecraft.gman.WorldUtility;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.physics.PhysicsOverworld;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.PhysicsOverworld.IPhysicsWorldConfiguration;
import gliby.minecraft.physics.common.physics.engine.ICollisionObject;
import gliby.minecraft.physics.common.physics.engine.ICollisionShape;
import gliby.minecraft.physics.common.physics.engine.IConstraint;
import gliby.minecraft.physics.common.physics.engine.IConstraintGeneric6Dof;
import gliby.minecraft.physics.common.physics.engine.IConstraintPoint2Point;
import gliby.minecraft.physics.common.physics.engine.IConstraintSlider;
import gliby.minecraft.physics.common.physics.engine.IGhostObject;
import gliby.minecraft.physics.common.physics.engine.IRayResult;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import gliby.minecraft.physics.common.physics.engine.IRope;
import gliby.minecraft.physics.common.physics.engine.concurrent.ConcurrentPhysicsWorld;
import gliby.minecraft.physics.common.physics.mechanics.PhysicsMechanic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class JavaPhysicsWorld extends ConcurrentPhysicsWorld {

	private List<IRigidBody> rigidBodies;
	private List<IConstraint> constraints;
	private DiscreteDynamicsWorld dynamicsWorld;
	private PhysicsOverworld physicsOverworld;
	private Physics physics;

	/**
	 * @param ticksPerSecond
	 */
	public JavaPhysicsWorld(Physics physics, PhysicsOverworld physicsOverworld,
			IPhysicsWorldConfiguration physicsConfig) {
		super(physicsConfig);
		this.physics = physics;
		this.physicsOverworld = physicsOverworld;
	}

	@Override
	public void tick() {
		getDelta();
		lastFPS = getTime();
		while (enabled) {
			synchronized (this) {
				try {
					wait(1000 / getPhysicsConfiguration().getTicksPerSecond());
				} catch (InterruptedException e) {
					Physics.getLogger().catching(e);
				}
				if (getPhysicsConfiguration().shouldSimulate(getPhysicsConfiguration().getWorld(), this))
					simulate();
				updateFPS();
			}
		}
	}

	@Override
	public void init() {
		ropes = new ArrayList<IRope>();
		rigidBodies = new ArrayList<IRigidBody>();
		constraints = new ArrayList<IConstraint>();
		final DbvtBroadphase broadphase = new DbvtBroadphase();
		broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		final CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		final CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

		this.dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, new SequentialImpulseConstraintSolver(),
				collisionConfiguration);
		this.dynamicsWorld.setGravity(getPhysicsConfiguration().getRegularGravity());

		Matrix3f rot = new Matrix3f();
		rot.setIdentity();
		Transform identityTransform = new Transform(new Matrix4f(rot, new Vector3f(0, 0, 0), 1.0f));

		// Create block collision connection to bullet.
		JBulletVoxelWorldShape blockCollisionHandler = new JBulletVoxelWorldShape(
				new JavaVoxelProvider(getPhysicsConfiguration().getWorld(), physics, this));
		blockCollisionHandler.calculateLocalInertia(0, new Vector3f());
		RigidBodyConstructionInfo blockConsInf = new RigidBodyConstructionInfo(0,
				new DefaultMotionState(identityTransform), blockCollisionHandler, new Vector3f());
		RigidBody blockCollisionBody = new RigidBody(blockConsInf);
		blockCollisionBody.setCollisionFlags(CollisionFlags.STATIC_OBJECT | blockCollisionBody.getCollisionFlags());
		dynamicsWorld.addRigidBody(blockCollisionBody);
		super.init();
	}

	@Override
	protected synchronized void simulate() {
		float delta = getDelta();
		if (dynamicsWorld != null)
			dynamicsWorld.stepSimulation(1, Math.round(delta / 7));
		super.simulate();
	}

	@Override
	public void addRigidBody(final IRigidBody body) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.addRigidBody((RigidBody) body.getBody());
				rigidBodies.add(body);
			}
		});
	}

	@Override
	public void addRigidBody(final IRigidBody body, final short collisionFilterGroup, final short collisionFilterMask) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.addRigidBody((RigidBody) body.getBody(), collisionFilterGroup, collisionFilterMask);
				rigidBodies.add(body);

			}
		});
	}

	@Override
	public void removeRigidBody(final IRigidBody body) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.removeRigidBody((RigidBody) body.getBody());
				rigidBodies.remove(body);
			}
		});
	}

	@Override
	public void awakenArea(final Vector3f min, final Vector3f max) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				if (dynamicsWorld != null)
					dynamicsWorld.awakenRigidBodiesInArea(min, max);
			}
		});

	}

	@Override
	public void rayTest(final Vector3f rayFromWorld, final Vector3f rayToWorld, final IRayResult resultCallback) {
		synchronized (this) {
			dynamicsWorld.rayTest(rayFromWorld, rayToWorld, (RayResultCallback) resultCallback.getRayResultCallback());
		}
	}

	@Override
	public void removeCollisionObject(final ICollisionObject collisionObject) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.removeCollisionObject((CollisionObject) collisionObject.getCollisionObject());
			}
		});
	}

	@Override
	public void setGravity(final Vector3f newGravity) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.setGravity(newGravity);
			}
		});
	}

	@Override
	public void addCollisionObject(final ICollisionObject object) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.addCollisionObject((CollisionObject) object.getCollisionObject());
			}
		});
	}

	@Override
	public void addCollisionObject(final ICollisionObject object, final short collisionFilterGroup,
			final short collisionFilterMask) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.addCollisionObject((CollisionObject) object.getCollisionObject(), collisionFilterGroup,
						collisionFilterMask);
			}
		});
	}

	@Override
	public List<IRigidBody> getRigidBodies() {
		return rigidBodies;
	}

	@Override
	public void dispose() {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < dynamicsWorld.getNumCollisionObjects(); i++) {
					CollisionObject object = dynamicsWorld.getCollisionObjectArray().get(i);
					dynamicsWorld.removeCollisionObject(object);
				}

				dynamicsWorld.destroy();
				rigidBodies.clear();
				constraints.clear();
			}
		});
		super.dispose();
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
		synchronized (this) {
			Vector3f localInertia = new Vector3f(0, 0, 0);
			if (mass != 0) {
				shape.calculateLocalInertia(mass, localInertia);
			}
			DefaultMotionState motionState = new DefaultMotionState(transform);
			RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(mass, motionState,
					(CollisionShape) shape.getCollisionShape(), localInertia);
			RigidBody body = new RigidBody(constructionInfo);
			return new JavaRigidBody(this, body, owner);
		}
	}

	@Override
	public ICollisionShape createBoxShape(final Vector3f extents) {
		return new gliby.minecraft.physics.common.physics.engine.concurrent.javabullet.JavaCollisionShape(this,
				new BoxShape(extents));
	}

	@Override
	public IRayResult createClosestRayResultCallback(final Vector3f rayFromWorld, final Vector3f rayToWorld) {
		return new JavaClosestRayResultCallback(new CollisionWorld.ClosestRayResultCallback(rayFromWorld, rayToWorld));
	}

	public IGhostObject createPairCachingGhostObject() {
		return new JavaPairCachingGhostObject(this, new PairCachingGhostObject());
	}

	@Override
	public IRigidBody upcastRigidBody(Object collisionObject) {
		synchronized (this) {
			for (int i = 0; i < rigidBodies.size(); i++) {
				IRigidBody body = rigidBodies.get(i);
				if (body.getBody() == RigidBody.upcast((CollisionObject) collisionObject)) {
					return body;
				} else
					continue;
			}
			return null;
		}
	}

	@Override
	public IConstraintPoint2Point createPoint2PointConstraint(IRigidBody rigidBody, Vector3f relativePivot) {
		return new JavaConstraintPoint2Point(this,
				new Point2PointConstraint((RigidBody) rigidBody.getBody(), relativePivot));
	}

	@Override
	public void addConstraint(final IConstraint constraint) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.addConstraint((TypedConstraint) constraint.getConstraint());
				constraints.add(constraint);
			}
		});
	}

	@Override
	public void removeConstraint(final IConstraint constraint) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.removeConstraint((TypedConstraint) constraint.getConstraint());
				constraints.remove(constraint);
			}
		});
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
		return new JavaCollisionShape(this, shape);
	}

	@Override
	public List<IConstraint> getConstraints() {
		return constraints;
	}

	@Override
	public IConstraintGeneric6Dof createGeneric6DofConstraint(IRigidBody rbA, IRigidBody rbB, Transform frameInA,
			Transform frameInB, boolean useLinearReferenceFrameA) {
		return new JavaConstraintGeneric6Dof(this, new Generic6DofConstraint((RigidBody) rbA.getBody(),
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
		return getClass().getSimpleName() + "[" + this.rigidBodies.size() + " rigid bodies" + "]";
	}

	@Override
	public ICollisionShape createSphereShape(float radius) {
		return new JavaCollisionShape(this, new SphereShape(radius));
	}

	@Override
	public IRigidBody createInertiallessRigidBody(Entity owner, Transform transform, float mass,
			ICollisionShape shape) {
		DefaultMotionState motionState = new DefaultMotionState(transform);
		RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(mass, motionState,
				(CollisionShape) shape.getCollisionShape());
		RigidBody body = new RigidBody(constructionInfo);
		return new JavaRigidBody(this, body, owner);
	}

	@Override
	public IConstraintSlider createSliderConstraint(IRigidBody rbA, IRigidBody rbB, Transform frameInA,
			Transform frameInB, boolean useLinearReferenceFrameA) {
		return new JavaConstraintSlider(new SliderConstraint((RigidBody) rbA.getBody(), (RigidBody) rbB.getBody(),
				frameInA, frameInB, useLinearReferenceFrameA));
	}

	private static Transform transform = new Transform();

	private static Transform getTransform() {
		return transform;
	}

	@Override
	public ICollisionShape buildCollisionShape(List<AxisAlignedBB> bbs, Vector3f offset) {
		CompoundShape compoundShape = new CompoundShape();
		for (AxisAlignedBB bb : bbs) {
			AxisAlignedBB relativeBB = AxisAlignedBB.fromBounds((bb.minX - offset.getX()) * 0.5f,
					(bb.minY - offset.getY()) * 0.5f, (bb.minZ - offset.getZ()) * 0.5f,
					(bb.maxX - offset.getX()) * 0.5f, (bb.maxY - offset.getY()) * 0.5f,
					(bb.maxZ - offset.getZ()) * 0.5f);
			Vector3f extents = new Vector3f((float) relativeBB.maxX - (float) relativeBB.minX,
					(float) relativeBB.maxY - (float) relativeBB.minY,
					(float) relativeBB.maxZ - (float) relativeBB.minZ);
			transform.setIdentity();
			transform.origin.set((float) relativeBB.minX + (float) relativeBB.maxX - 0.5f,
					(float) relativeBB.minY + (float) relativeBB.maxY - 0.5f,
					(float) relativeBB.minZ + (float) relativeBB.maxZ - 0.5f);
			compoundShape.addChildShape(transform, new BoxShape(extents));
		}
		return new JavaCollisionShape(this, compoundShape);
	}
}
