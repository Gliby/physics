package gliby.minecraft.physics.common.physics.engine.nativebullet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.RayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.collision.btVoxelShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btGeneric6DofConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.Disposable;

import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.physics.PhysicsOverworld;
import gliby.minecraft.physics.common.physics.PhysicsOverworld.IPhysicsWorldConfiguration;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
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
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

/**
 *
 */

// TODO NativePhysicsWorld: Start disposing of every, and I mean EVERY native
// element.
// TODO NativePhysicsWorld: dispose on remove from world too.
// FIXME NativePhysicsWorld: We need to care of memory!
// FIXME NativePhysicsWorld: Stop using Vector/Matrix/Transform conversions. Use
// IVector and
// IQuaternion, IMatrix, replace with custom vector stuff or MC Vec3.
// TODO NativePhysicsWorld: Add ability to run in non-thread.
public class NativePhysicsWorld extends PhysicsWorld {

	static {
		Bullet.init();
	}

	private btDiscreteDynamicsWorld dynamicsWorld;

	private List<IConstraint> constraints;
	private List<IRigidBody> rigidBodies;
	private PhysicsOverworld physicsOverworld;
	private Physics physics;

	/**
	 * @param overWorld
	 * @param world
	 * @param ticksPerSecond
	 * @param gravity
	 */
	public NativePhysicsWorld(Physics physics, PhysicsOverworld physicsOverworld,
			IPhysicsWorldConfiguration physicsConfig) {
		super(physicsConfig);
		this.physics = physics;
		this.physicsOverworld = physicsOverworld;
	}

	private boolean shutdown = false;

	@Override
	public void tick() {
		getDelta();
		lastFPS = getTime();
		if (getPhysicsConfiguration().shouldSimulate(getPhysicsConfiguration().getWorld(), this))
			simulate();
		updateFPS();
	}

	private btDbvtBroadphase broadphase;
	private btCollisionConfiguration collisionConfiguration;
	private btCollisionDispatcher collisionDispatcher;
	private btVoxelShape voxelShape;

	private btCollisionObject voxelBody;
	private NativeVoxelProvider voxelProvider;

	private btSequentialImpulseConstraintSolver sequentialSolver;

	@Override
	public void init() {
		collisionObjects = new CopyOnWriteArrayList<ICollisionObject>();
		rigidBodies = new CopyOnWriteArrayList<IRigidBody>();
		constraints = new CopyOnWriteArrayList<IConstraint>();
		disposables = new ArrayList<Disposable>();

		broadphase = new btDbvtBroadphase();
		collisionConfiguration = new btDefaultCollisionConfiguration();
		collisionDispatcher = new btCollisionDispatcher(collisionConfiguration);

		dynamicsWorld = new btDiscreteDynamicsWorld(collisionDispatcher, broadphase,
				sequentialSolver = new btSequentialImpulseConstraintSolver(), collisionConfiguration);
		dynamicsWorld.setGravity(getPhysicsConfiguration().getRegularGravity());

		voxelShape = new btVoxelShape(
				voxelProvider = new NativeVoxelProvider(getPhysicsConfiguration().getWorld(), this, physics),
				new Vector3(-Integer.MAX_VALUE, -Integer.MAX_VALUE, -Integer.MAX_VALUE),
				new Vector3(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
		voxelBody = new btCollisionObject();
		voxelBody.setCollisionShape(voxelShape);
		voxelBody.setCollisionFlags(btCollisionObject.CollisionFlags.CF_STATIC_OBJECT | voxelBody.getCollisionFlags());
		dynamicsWorld.addCollisionObject(voxelBody);
		super.init();
	}

	@Override
	protected void simulate() {
		float delta = getDelta();
		if (dynamicsWorld != null) {
			super.simulate();
			dynamicsWorld.stepSimulation(1, Math.round(delta / 7));
		}
	}

	@Override
	public IRigidBody createRigidBody(Entity owner, Matrix4 transform, float mass, ICollisionShape shape) {
		Vector3 localInertia = new Vector3();
		if (mass != 0) {
			shape.calculateLocalInertia(mass, localInertia);
		}
		btDefaultMotionState motionState = new btDefaultMotionState(transform);
		btRigidBodyConstructionInfo constructionInfo = new btRigidBodyConstructionInfo(mass, motionState,
				(btCollisionShape) shape.getCollisionShape(), localInertia);
		NativeRigidBody rigidBody = new NativeRigidBody(this, new btRigidBody(constructionInfo), owner);

		return rigidBody;
	}

	@Override
	public IRigidBody createInertiallessRigidBody(Entity owner, Matrix4 transform, float mass, ICollisionShape shape) {
		btDefaultMotionState motionState = new btDefaultMotionState(transform);
		btRigidBodyConstructionInfo constructionInfo = new btRigidBodyConstructionInfo(mass, motionState,
				(btCollisionShape) shape.getCollisionShape());
		NativeRigidBody rigidBody = new NativeRigidBody(this, new btRigidBody(constructionInfo), owner);
		return rigidBody;
	}

	@Override
	public ICollisionShape createBoxShape(Vector3 extents) {
		NativeCollisionShape shape = new NativeCollisionShape(this, new btBoxShape(extents));
		return shape;
	}

	/**
	 * Tracks disposable objects that fall under miscellaneous.
	 */

	private List<Disposable> disposables;

	protected List<ICollisionObject> collisionObjects;

	@Override
	public IRayResult createClosestRayResultCallback(Vector3 rayFromWorld, Vector3 rayToWorld) {
		ClosestRayResultCallback nativeCallback;
		NativeClosestRayResultCallback callback = new NativeClosestRayResultCallback(
				nativeCallback = new ClosestRayResultCallback(rayFromWorld, rayToWorld));
		disposables.add(nativeCallback);
		return callback;
	}

	@Override
	public void addRigidBody(final IRigidBody body) {
		dynamicsWorld.addRigidBody((btRigidBody) body.getBody());
		rigidBodies.add(body);
	}

	@Override
	public void addRigidBody(final IRigidBody body, final short collisionFilterGroup, final short collisionFilterMask) {
		dynamicsWorld.addRigidBody((btRigidBody) body.getBody(), collisionFilterGroup, collisionFilterMask);
		rigidBodies.add(body);
	}

	@Override
	public void addConstraint(final IConstraint p2p) {
		dynamicsWorld.addConstraint((btTypedConstraint) p2p.getConstraint());
		constraints.add(p2p);
	}

	// TODO NativePhysicsWorld: Dispose of object on remove.
	@Override
	public void removeRigidBody(final IRigidBody body) {
		btRigidBody nativeBody;
		dynamicsWorld.removeRigidBody(nativeBody = (btRigidBody) body.getBody());
		rigidBodies.remove(body);
		nativeBody.dispose();
	}

	@Override
	public void awakenArea(Vector3 min, Vector3 max) {
		final AxisAlignedBB bb = AxisAlignedBB.fromBounds(min.x, min.y, min.z, max.x, max.y, max.z);
		for (int i = 0; i < rigidBodies.size(); i++) {
			IRigidBody body = rigidBodies.get(i);
			Vector3 vec3 = body.getCenterOfMassPosition(new Vector3());
			Vec3 centerOfMass = new Vec3(vec3.x, vec3.y, vec3.z);
			if (bb.isVecInside(centerOfMass)) {
				body.activate();
			}
		}
	}

	@Override
	public void rayTest(final Vector3 rayFromWorld, final Vector3 rayToWorld, final IRayResult resultCallback) {
		/*
		 * physicsTasks.add(new Runnable() {
		 * 
		 * @Override public void run() { } });
		 * dynamicsWorld.rayTest(toVector3(rayFromWorld), toVector3(rayToWorld),
		 * (RayResultCallback) resultCallback.getRayResultCallback());
		 */
		dynamicsWorld.rayTest(rayFromWorld, rayToWorld, (RayResultCallback) resultCallback.getRayResultCallback());

	}

	@Override
	public void removeCollisionObject(final ICollisionObject collisionObject) {
		btCollisionObject nativeCollsionObject;
		dynamicsWorld
				.removeCollisionObject(nativeCollsionObject = (btCollisionObject) collisionObject.getCollisionObject());
		collisionObjects.remove(collisionObject);
		nativeCollsionObject.dispose();

	}

	@Override
	public void setGravity(final Vector3 newGravity) {
		dynamicsWorld.setGravity(newGravity);
	}

	@Override
	public void addCollisionObject(final ICollisionObject object) {
		dynamicsWorld.addCollisionObject((btCollisionObject) object.getCollisionObject());
		collisionObjects.add(object);
	}

	@Override
	public void addCollisionObject(final ICollisionObject object, final short collisionFilterGroup,
			final short collisionFilterMask) {
		dynamicsWorld.addCollisionObject((btCollisionObject) object.getCollisionObject(), collisionFilterGroup,
				collisionFilterMask);
		collisionObjects.add(object);
	}

	@Override
	public List<IRigidBody> getRigidBodies() {
		return rigidBodies;
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

	// float stepsPerSecond;

	protected void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			// stepsPerSecond = fps;
			System.out.println("update fps: " + fps);
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

	protected long getTime() {
		return System.currentTimeMillis();
	}

	@Override
	public IGhostObject createPairCachingGhostObject() {
		btPairCachingGhostObject nativePair;
		NativePairCachingGhostObject pairCache = new NativePairCachingGhostObject(this,
				nativePair = new btPairCachingGhostObject());
		disposables.add(nativePair);
		return pairCache;
	}

	@Override
	public IRigidBody upcastRigidBody(Object collisionObject) {
		for (int i = 0; i < rigidBodies.size(); i++) {
			IRigidBody body = rigidBodies.get(i);
			if (body.getBody() == collisionObject) {
				return body;
			} else
				continue;
		}
		return null;
	}

	@Override
	public IConstraintPoint2Point createPoint2PointConstraint(IRigidBody rigidBody, Vector3 relativePivot) {
		btPoint2PointConstraint nativeConstraint;
		NativePoint2PointConstraint p2p = new NativePoint2PointConstraint(this,
				nativeConstraint = new btPoint2PointConstraint((btRigidBody) rigidBody.getBody(), relativePivot));
		// NativePhysicsWorld: constraints, should add
		return p2p;
	}

	@Override
	public void removeConstraint(final IConstraint constraint) {
		btTypedConstraint nativeConstraint = (btTypedConstraint) constraint.getConstraint();
		dynamicsWorld.removeConstraint((btTypedConstraint) constraint.getConstraint());
		constraints.remove(constraint);
		nativeConstraint.dispose();
	}

	@Override
	public ICollisionShape readBlockCollisionShape(String json) {
		return null;
	}

	@Override
	public String writeBlockCollisionShape(ICollisionShape shape) {
		return null;
	}

	@Override
	public List<IConstraint> getConstraints() {
		return constraints;
	}

	@Override
	public IConstraintGeneric6Dof createGeneric6DofConstraint(IRigidBody rbA, IRigidBody rbB, Matrix4 frameInA,
			Matrix4 frameInB, boolean useLinearReferenceFrameA) {
		NativeConstraintGeneric6Dof constraint = new NativeConstraintGeneric6Dof(this,
				new btGeneric6DofConstraint((btRigidBody) rbA.getBody(), (btRigidBody) rbB.getBody(), frameInA,
						frameInB, useLinearReferenceFrameA));
		return constraint;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + this.rigidBodies.size() + " rigid bodies" + "]";
	}

	// TODO NativePhysicsWorld: Add rope support

	@Override
	public void addRope(IRope object) {
		// TODO NativePhysicsWorld: rope feature
	}

	@Override
	public List<IRope> getRopes() {
		// TODO NativePhysicsWorld: rope feature
		return null;
	}

	@Override
	public void removeRope(IRope rope) {
		// TODO NativePhysicsWorld: rope feature

	}

	@Override
	public IRope createRope(Vector3 startPos, Vector3 endPos, int detail) {
		// TODO NativePhysicsWorld: rope feature
		return null;
	}

	@Override
	public ICollisionShape createSphereShape(float radius) {
		btSphereShape nativeSphere;
		NativeCollisionShape shape = new NativeCollisionShape(this, nativeSphere = new btSphereShape(radius));
		disposables.add(nativeSphere);
		return shape;
	}

	// TODO NativePhysicsWorld: Add slider constraint
	@Override
	public IConstraintSlider createSliderConstraint(IRigidBody rbA, IRigidBody rbB, Matrix4 frameInA, Matrix4 frameInB,
			boolean useLinearReferenceFrameA) {
		return null;
	}

	public ICollisionShape buildCollisionShape(List<AxisAlignedBB> bbs, Vector3 offset) {
		btCompoundShape compoundShape = new btCompoundShape();
		for (AxisAlignedBB bb : bbs) {
			AxisAlignedBB relativeBB = AxisAlignedBB.fromBounds((bb.minX - offset.x) * 0.5f,
					(bb.minY - offset.y) * 0.5f, (bb.minZ - offset.z) * 0.5f, (bb.maxX - offset.x) * 0.5f,
					(bb.maxY - offset.y) * 0.5f, (bb.maxZ - offset.z) * 0.5f);
			Vector3 extents = new Vector3((float) relativeBB.maxX - (float) relativeBB.minX,
					(float) relativeBB.maxY - (float) relativeBB.minY,
					(float) relativeBB.maxZ - (float) relativeBB.minZ);
			Matrix4 transform = new Matrix4();
			transform.idt();
			transform.setTranslation((float) relativeBB.minX + (float) relativeBB.maxX - 0.5f,
					(float) relativeBB.minY + (float) relativeBB.maxY - 0.5f,
					(float) relativeBB.minZ + (float) relativeBB.maxZ - 0.5f);
			compoundShape.addChildShape(transform, new btBoxShape(extents));
		}
		NativeCollisionShape collisionShape = new NativeCollisionShape(this, compoundShape);
		return collisionShape;

	}

	// TODO NativePhysicsWorld: Make dispose actually dispose something.
	@Override
	public void dispose() {
		dynamicsWorld.removeCollisionObject(voxelBody);
		voxelShape.dispose();
		voxelBody.dispose();
		voxelProvider.dispose();

		for (int i = 0; i < disposables.size(); i++) {
			Disposable disposable = disposables.get(i);
			disposable.dispose();
		}

		for (int i = 0; i < constraints.size(); i++) {
			// Get constraint
			IConstraint constraint = constraints.get(i);
			// Remove reference from list
			constraints.remove(i);

			// Get native reference
			btTypedConstraint constraintRef = (btTypedConstraint) constraint.getConstraint();
			// Remove from world.
			dynamicsWorld.removeConstraint(constraintRef);

			// Dispose of native reference
			constraintRef.dispose();
		}

		for (int i = 0; i < collisionObjects.size(); i++) {
			ICollisionObject object = collisionObjects.get(i);
			collisionObjects.remove(i);
			btCollisionObject objectRef = (btCollisionObject) object.getCollisionObject();
			dynamicsWorld.removeCollisionObject(objectRef);
			objectRef.dispose();
		}

		for (int i = 0; i < rigidBodies.size(); i++) {
			// Get rigidBody
			IRigidBody rigidBody = (IRigidBody) rigidBodies.get(i);
			// Remove reference from list
			rigidBodies.remove(i);

			// Get native reference.
			btRigidBody rigidBodyRef = (btRigidBody) rigidBody.getBody();
			// Remove from world.
			dynamicsWorld.removeRigidBody(rigidBodyRef);
			// Dispose of native reference
			rigidBodyRef.getCollisionShape().dispose();
			rigidBodyRef.getMotionState().dispose();
			rigidBodyRef.dispose();
		}

		broadphase.dispose();
		collisionDispatcher.dispose();
		collisionConfiguration.dispose();
		sequentialSolver.dispose();
		dynamicsWorld.dispose();
		dynamicsWorld = null;
		super.dispose();
	}
}