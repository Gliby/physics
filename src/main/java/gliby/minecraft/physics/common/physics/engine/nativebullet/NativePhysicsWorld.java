package gliby.minecraft.physics.common.physics.engine.nativebullet;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.BulletBase;
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
import com.bulletphysicsx.linearmath.Transform;
import com.google.common.collect.Queues;

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
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import sun.rmi.runtime.RuntimeUtil;

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
	public void run() {
		getDelta();
		lastFPS = getTime();
		while (running) {
			synchronized (this) {
				try {
					wait(1000 / getPhysicsConfiguration().getTicksPerSecond());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (getPhysicsConfiguration().shouldSimulate(getPhysicsConfiguration().getWorld(), this))
				update();
			updateFPS();
		}
	}

	private btDbvtBroadphase broadphase;
	private btCollisionConfiguration collisionConfiguration;
	private btCollisionDispatcher collisionDispatcher;
	private btVoxelShape voxelShape;

	private btCollisionObject voxelBody;
	private NativeVoxelProvider voxelProvider;

	private btSequentialImpulseConstraintSolver sequentialSolver;

	@Override
	public void create() {
		collisionObjects = new CopyOnWriteArrayList<ICollisionObject>();
		rigidBodies = new CopyOnWriteArrayList<IRigidBody>();
		constraints = new CopyOnWriteArrayList<IConstraint>();
		disposables = new ArrayList<Disposable>();

		broadphase = new btDbvtBroadphase();
		collisionConfiguration = new btDefaultCollisionConfiguration();
		collisionDispatcher = new btCollisionDispatcher(collisionConfiguration);

		dynamicsWorld = new btDiscreteDynamicsWorld(collisionDispatcher, broadphase,
				sequentialSolver = new btSequentialImpulseConstraintSolver(), collisionConfiguration);
		dynamicsWorld.setGravity(toVector3(getPhysicsConfiguration().getRegularGravity()));

		voxelShape = new btVoxelShape(
				voxelProvider = new NativeVoxelProvider(getPhysicsConfiguration().getWorld(), this, physics),
				new Vector3(-Integer.MAX_VALUE, -Integer.MAX_VALUE, -Integer.MAX_VALUE),
				new Vector3(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
		voxelBody = new btCollisionObject();
		voxelBody.setCollisionShape(voxelShape);
		voxelBody.setCollisionFlags(btCollisionObject.CollisionFlags.CF_STATIC_OBJECT | voxelBody.getCollisionFlags());
		dynamicsWorld.addCollisionObject(voxelBody);
		super.create();
	}

	@Override
	protected void update() {
		float delta = getDelta();
		if (dynamicsWorld != null) {
			super.update();
			dynamicsWorld.stepSimulation(1, Math.round(delta / 7));
		}
	}

	@Override
	public IRigidBody createRigidBody(Entity owner, Transform transform, float mass, ICollisionShape shape) {
		Vector3 localInertia = new Vector3();
		if (mass != 0) {
			shape.calculateLocalInertia(mass, localInertia);
		}
		btDefaultMotionState motionState = new btDefaultMotionState(fromTransformToMatrix4(transform));
		btRigidBodyConstructionInfo constructionInfo = new btRigidBodyConstructionInfo(mass, motionState,
				(btCollisionShape) shape.getCollisionShape(), localInertia);
		NativeRigidBody rigidBody = new NativeRigidBody(this, new btRigidBody(constructionInfo), owner);

		return rigidBody;
	}

	@Override
	public IRigidBody createInertiallessRigidBody(Entity owner, Transform transform, float mass,
			ICollisionShape shape) {
		btDefaultMotionState motionState = new btDefaultMotionState(fromTransformToMatrix4(transform));
		btRigidBodyConstructionInfo constructionInfo = new btRigidBodyConstructionInfo(mass, motionState,
				(btCollisionShape) shape.getCollisionShape());
		NativeRigidBody rigidBody = new NativeRigidBody(this, new btRigidBody(constructionInfo), owner);
		return rigidBody;
	}

	@Override
	public ICollisionShape createBoxShape(Vector3f extents) {
		NativeCollisionShape shape = new NativeCollisionShape(this, new btBoxShape(toVector3(extents)));
		return shape;
	}

	/**
	 * Tracks disposable objects that fall under miscellaneous.
	 */

	private List<Disposable> disposables;

	protected List<ICollisionObject> collisionObjects;

	@Override
	public IRayResult createClosestRayResultCallback(Vector3f rayFromWorld, Vector3f rayToWorld) {
		ClosestRayResultCallback nativeCallback;
		NativeClosestRayResultCallback callback = new NativeClosestRayResultCallback(
				nativeCallback = new ClosestRayResultCallback(toVector3(rayFromWorld), toVector3(rayToWorld)));
		disposables.add(nativeCallback);
		return callback;
	}

	@Override
	public void addRigidBody(final IRigidBody body) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.addRigidBody((btRigidBody) body.getBody());
				rigidBodies.add(body);
			}
		});
	}

	@Override
	public void addRigidBody(final IRigidBody body, final short collisionFilterGroup, final short collisionFilterMask) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.addRigidBody((btRigidBody) body.getBody(), collisionFilterGroup, collisionFilterMask);
				rigidBodies.add(body);

			}
		});
	}

	@Override
	public void addConstraint(final IConstraint p2p) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.addConstraint((btTypedConstraint) p2p.getConstraint());
				constraints.add(p2p);
			}
		});
	}

	// TODO NativePhysicsWorld: Dispose of object on remove.
	@Override
	public void removeRigidBody(final IRigidBody body) {
		physicsTasks.add(new Runnable() {
			@Override
			public void run() {
				btRigidBody nativeBody;
				dynamicsWorld.removeRigidBody(nativeBody = (btRigidBody) body.getBody());
				rigidBodies.remove(body);
				nativeBody.dispose();
			}
		});
	}

	@Override
	public void awakenArea(Vector3f min, Vector3f max) {
		final AxisAlignedBB bb = AxisAlignedBB.fromBounds(min.x, min.y, min.z, max.x, max.y, max.z);
		synchronized (this) {
			for (int i = 0; i < rigidBodies.size(); i++) {
				IRigidBody body = rigidBodies.get(i);
				Vector3f vec3 = body.getCenterOfMassPosition();
				Vec3 centerOfMass = new Vec3(vec3.x, vec3.y, vec3.z);
				if (bb.isVecInside(centerOfMass)) {
					body.activate();
				}
			}
		}
	}

	@Override
	public void rayTest(final Vector3f rayFromWorld, final Vector3f rayToWorld, final IRayResult resultCallback) {
		/*
		 * physicsTasks.add(new Runnable() {
		 * 
		 * @Override public void run() { } });
		 * dynamicsWorld.rayTest(toVector3(rayFromWorld), toVector3(rayToWorld),
		 * (RayResultCallback) resultCallback.getRayResultCallback());
		 */
		synchronized (this) {
			dynamicsWorld.rayTest(toVector3(rayFromWorld), toVector3(rayToWorld),
					(RayResultCallback) resultCallback.getRayResultCallback());
		}

	}

	@Override
	public void removeCollisionObject(final ICollisionObject collisionObject) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				btCollisionObject nativeCollsionObject;
				dynamicsWorld.removeCollisionObject(
						nativeCollsionObject = (btCollisionObject) collisionObject.getCollisionObject());
				collisionObjects.remove(collisionObject);
				nativeCollsionObject.dispose();
			}
		});

	}

	@Override
	public void setGravity(final Vector3f newGravity) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.setGravity(toVector3(newGravity));
			}
		});
	}

	@Override
	public void addCollisionObject(final ICollisionObject object) {
		physicsTasks.add(new Runnable() {
			@Override
			public void run() {
				dynamicsWorld.addCollisionObject((btCollisionObject) object.getCollisionObject());
				collisionObjects.add(object);
			}
		});
	}

	@Override
	public void addCollisionObject(final ICollisionObject object, final short collisionFilterGroup,
			final short collisionFilterMask) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				dynamicsWorld.addCollisionObject((btCollisionObject) object.getCollisionObject(), collisionFilterGroup,
						collisionFilterMask);
				collisionObjects.add(object);
			}
		});
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
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

	protected long getTime() {
		return System.currentTimeMillis();
	}

	static Matrix4 fromTransformToMatrix4(Transform transform) {
		return toMatrix4(transform.getMatrix(new Matrix4f()));
	}

	// TODO NativePhysicsWorld: replace this hack.
	private static Transform temp = new Transform();
	private static Quat4f rotationTemp = new Quat4f();

	public static Matrix4 toMatrix4(Matrix4f matrix4f) {
		temp.set(matrix4f);
		Quat4f rot = temp.getRotation(rotationTemp);
		return new Matrix4().set(temp.origin.x, temp.origin.y, temp.origin.z, rot.x, rot.y, rot.z, rot.w, 1, 1, 1);
	}

	static Vector3 toVector3(Vector3f vector3f) {
		return new Vector3(vector3f.x, vector3f.y, vector3f.z);
	}

	static Vector3f toVector3f(Vector3 vector3) {
		return new Vector3f(vector3.x, vector3.y, vector3.z);
	}

	static Vector3f staticVector = new Vector3f();

	static Vector3f toStaticVector3f(Vector3 vector3) {
		staticVector.set(vector3.x, vector3.y, vector3.z);
		return staticVector;
	}

	static Vector3 tempVec = new Vector3();
	static Quaternion tempQuat = new Quaternion();

	// FIXME NativePhysicsWorld: Potential Memory Leak.
	static Matrix4f toMatrix4f(Matrix4 matrix4) {
		Vector3 position = matrix4.getTranslation(tempVec);
		Quaternion rotation = matrix4.getRotation(tempQuat);
		rotationTemp.set(rotation.x, rotation.y, rotation.z, rotation.w);
		temp.setRotation(rotationTemp);
		temp.origin.set(position.x, position.y, position.z);
		return temp.getMatrix(new Matrix4f());
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
	public IConstraintPoint2Point createPoint2PointConstraint(IRigidBody rigidBody, Vector3f relativePivot) {
		btPoint2PointConstraint nativeConstraint;
		NativePoint2PointConstraint p2p = new NativePoint2PointConstraint(this,
				nativeConstraint = new btPoint2PointConstraint((btRigidBody) rigidBody.getBody(),
						toVector3(relativePivot)));
		return p2p;
	}

	@Override
	public void removeConstraint(final IConstraint constraint) {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
				btTypedConstraint nativeConstraint = (btTypedConstraint) constraint.getConstraint();
				dynamicsWorld.removeConstraint((btTypedConstraint) constraint.getConstraint());
				constraints.remove(constraint);
				nativeConstraint.dispose();
			}
		});
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
	public IConstraintGeneric6Dof createGeneric6DofConstraint(IRigidBody rbA, IRigidBody rbB, Transform frameInA,
			Transform frameInB, boolean useLinearReferenceFrameA) {
		NativeConstraintGeneric6Dof constraint = new NativeConstraintGeneric6Dof(this,
				new btGeneric6DofConstraint((btRigidBody) rbA.getBody(), (btRigidBody) rbB.getBody(),
						this.fromTransformToMatrix4(frameInA), this.fromTransformToMatrix4(frameInB),
						useLinearReferenceFrameA));
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
	public IRope createRope(Vector3f startPos, Vector3f endPos, int detail) {
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
	public IConstraintSlider createSliderConstraint(IRigidBody rbA, IRigidBody rbB, Transform frameInA,
			Transform frameInB, boolean useLinearReferenceFrameA) {
		return null;
	}

	public ICollisionShape buildCollisionShape(List<AxisAlignedBB> bbs, Vector3f offset) {
		btCompoundShape compoundShape = new btCompoundShape();
		for (AxisAlignedBB bb : bbs) {
			AxisAlignedBB relativeBB = AxisAlignedBB.fromBounds((bb.minX - offset.x) * 0.5f,
					(bb.minY - offset.y) * 0.5f, (bb.minZ - offset.z) * 0.5f, (bb.maxX - offset.x) * 0.5f,
					(bb.maxY - offset.y) * 0.5f, (bb.maxZ - offset.z) * 0.5f);
			Vector3f extents = new Vector3f((float) relativeBB.maxX - (float) relativeBB.minX,
					(float) relativeBB.maxY - (float) relativeBB.minY,
					(float) relativeBB.maxZ - (float) relativeBB.minZ);
			Transform transform = new Transform();
			transform.setIdentity();
			transform.origin.set((float) relativeBB.minX + (float) relativeBB.maxX - 0.5f,
					(float) relativeBB.minY + (float) relativeBB.maxY - 0.5f,
					(float) relativeBB.minZ + (float) relativeBB.maxZ - 0.5f);
			compoundShape.addChildShape(fromTransformToMatrix4(transform), new btBoxShape(toVector3(extents)));
		}
		NativeCollisionShape collisionShape = new NativeCollisionShape(this, compoundShape);
		return collisionShape;

	}

	// TODO NativePhysicsWorld: Make dispose actually dispose something.
	@Override
	public void dispose() {
		physicsTasks.add(new Runnable() {

			@Override
			public void run() {
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
			}
		});
		super.dispose();
	}
}