/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics.engine.nativebullet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
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
import com.bulletphysics.linearmath.Transform;

import net.gliby.gman.WorldUtility;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.ICollisionObject;
import net.gliby.physics.common.physics.engine.ICollisionShape;
import net.gliby.physics.common.physics.engine.IConstraint;
import net.gliby.physics.common.physics.engine.IConstraintGeneric6Dof;
import net.gliby.physics.common.physics.engine.IConstraintPoint2Point;
import net.gliby.physics.common.physics.engine.IConstraintSlider;
import net.gliby.physics.common.physics.engine.IGhostObject;
import net.gliby.physics.common.physics.engine.IRayResult;
import net.gliby.physics.common.physics.engine.IRigidBody;
import net.gliby.physics.common.physics.engine.IRope;
import net.gliby.physics.common.physics.mechanics.PhysicsMechanic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 *
 */
// TODO Clean up after exit.
public abstract class NativePhysicsWorld extends PhysicsWorld {

	static {
		Bullet.init();
	}

	private btDiscreteDynamicsWorld dynamicsWorld;

	private List<IConstraint> constraints;
	private List<IRigidBody> rigidBodies;

	/**
	 * @param overWorld
	 * @param world
	 * @param ticksPerSecond
	 * @param gravity
	 */
	public NativePhysicsWorld(World world, int ticksPerSecond, Vector3f gravity) {
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
					e.printStackTrace();
				}
			}
			if (shouldSimulate())
				update();
			updateFPS();
		}
	}

	private static boolean launch = true;

	@Override
	public void create() {
		rigidBodies = new ArrayList<IRigidBody>();
		constraints = new ArrayList<IConstraint>();
		btDbvtBroadphase broadphase = new btDbvtBroadphase();
		btCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfiguration);
		this.dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase,
				new btSequentialImpulseConstraintSolver(), collisionConfiguration);
		this.dynamicsWorld.setGravity(toVector3(gravity));
		btVoxelShape voxelHandler = new btVoxelShape(new NativeVoxelProvider(world, this),
				new Vector3(-Integer.MAX_VALUE, -Integer.MAX_VALUE, -Integer.MAX_VALUE),
				new Vector3(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
		btCollisionObject body = new btCollisionObject();
		body.setCollisionShape(voxelHandler);
		body.setCollisionFlags(btCollisionObject.CollisionFlags.CF_STATIC_OBJECT | body.getCollisionFlags());
		this.dynamicsWorld.addCollisionObject(body);
		super.create();
	}

	@Override
	protected void update() {
		float delta = getDelta();
		if (!world.playerEntities.isEmpty()) {
			dynamicsWorld.stepSimulation(1, Math.round(delta / 7));
		}
		super.update();
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
		return new NativeRigidBody(new btRigidBody(constructionInfo), owner);
	}

	@Override
	public ICollisionShape createBoxShape(Vector3f extents) {
		return new NativeCollisionShape(new btBoxShape(toVector3(extents)));
	}

	@Override
	public IRayResult createClosestRayResultCallback(Vector3f rayFromWorld, Vector3f rayToWorld) {
		return new NativeClosestRayResultCallback(
				new ClosestRayResultCallback(toVector3(rayFromWorld), toVector3(rayToWorld)));
	}

	@Override
	public void addRigidBody(IRigidBody body) {
		synchronized (this) {
			this.dynamicsWorld.addRigidBody((btRigidBody) body.getBody());
			this.rigidBodies.add(body);
		}
	}

	@Override
	public void addRigidBody(IRigidBody body, short collisionFilterGroup, short collisionFilterMask) {
		synchronized (this) {
			this.dynamicsWorld.addRigidBody((btRigidBody) body.getBody(), collisionFilterGroup, collisionFilterMask);
			this.rigidBodies.add(body);
		}
	}

	@Override
	public void addConstraint(IConstraint p2p) {
		synchronized (this) {
			this.dynamicsWorld.addConstraint((btTypedConstraint) p2p.getConstraint());
			constraints.add(p2p);
		}
	}

	@Override
	public void removeRigidBody(IRigidBody body) {
		synchronized (this) {
			this.dynamicsWorld.removeRigidBody((btRigidBody) body.getBody());
			this.rigidBodies.remove(body);
		}
	}

	@Override
	public void awakenArea(Vector3f min, Vector3f max) {
		synchronized (this) {
			AxisAlignedBB bb = AxisAlignedBB.fromBounds(min.x, min.y, min.z, max.x, max.y, max.z);
			for (int i = 0; i < this.rigidBodies.size(); i++) {
				IRigidBody body = this.rigidBodies.get(i);
				Vector3f vec3 = body.getCenterOfMassPosition();
				Vec3 centerOfMass = new Vec3(vec3.x, vec3.y, vec3.z);
				if (bb.isVecInside(centerOfMass)) {
					body.activate();
				}
			}
		}
	}

	@Override
	public void rayTest(Vector3f rayFromWorld, Vector3f rayToWorld, IRayResult resultCallback) {
		synchronized (this) {
			dynamicsWorld.rayTest(toVector3(rayFromWorld), toVector3(rayToWorld),
					(RayResultCallback) resultCallback.getRayResultCallback());
		}
	}

	@Override
	public void removeCollisionObject(ICollisionObject collisionObject) {
		synchronized (this) {
			dynamicsWorld.removeCollisionObject((btCollisionObject) collisionObject.getCollisionObject());
		}
	}

	@Override
	public void setGravity(Vector3f newGravity) {
		synchronized (this) {
			dynamicsWorld.setGravity(toVector3(gravity));
		}
	}

	@Override
	public void addCollisionObject(ICollisionObject object) {
		synchronized (this) {
			dynamicsWorld.addCollisionObject((btCollisionObject) object.getCollisionObject());
		}
	}

	@Override
	public void addCollisionObject(ICollisionObject object, short collisionFilterGroup, short collisionFilterMask) {
		synchronized (this) {
			dynamicsWorld.addCollisionObject((btCollisionObject) object.getCollisionObject(), collisionFilterGroup,
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
		dynamicsWorld.dispose();
		rigidBodies.clear();
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

	// TODO Replace these.
	private static Transform temp = new Transform();

	public static Matrix4 toMatrix4(Matrix4f matrix4f) {
		temp.set(matrix4f);
		Quat4f rot = temp.getRotation(new Quat4f());
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

	// TODO Replace.
	// Java vecmath to libgdx maths, you should be able to just set the values
	// from one to another.
	static Matrix4f toMatrix4f(Matrix4 matrix4) {
		Vector3 position = matrix4.getTranslation(new Vector3());
		Quaternion rotation = matrix4.getRotation(new Quaternion());
		temp.setRotation(new Quat4f(new float[] { rotation.x, rotation.y, rotation.z, rotation.w }));
		temp.origin.set(position.x, position.y, position.z);
		return temp.getMatrix(new Matrix4f());
	}

	@Override
	public IGhostObject createPairCachingGhostObject() {
		return new NativePairCachingGhostObject(new btPairCachingGhostObject());
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
		return new NativePoint2PointConstraint(
				new btPoint2PointConstraint((btRigidBody) rigidBody.getBody(), toVector3(relativePivot)));
	}

	@Override
	public void removeConstraint(IConstraint constraint) {
		synchronized (this) {
			dynamicsWorld.removeConstraint((btTypedConstraint) constraint.getConstraint());
			constraints.remove(constraint);
		}
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
		return new NativeConstraintGeneric6Dof(new btGeneric6DofConstraint((btRigidBody) rbA.getBody(),
				(btRigidBody) rbB.getBody(), this.fromTransformToMatrix4(frameInA),
				this.fromTransformToMatrix4(frameInB), useLinearReferenceFrameA));
	}

	@Override
	public String toString() {
		return "BulletPhysicsWorld[ " + this.rigidBodies.size() + " rigid bodies" + "]";
	}

	// TODO Add rope support

	@Override
	public void addRope(IRope object) {
	}

	@Override
	public List<IRope> getRopes() {
		return null;
	}

	@Override
	public void removeRope(IRope rope) {
		// TODO Auto-generated method stub

	}

	@Override
	public IRope createRope(Vector3f startPos, Vector3f endPos, int detail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICollisionShape createSphereShape(float radius) {
		return new NativeCollisionShape(new btSphereShape(radius));
	}

	@Override
	public IRigidBody createInertiallessRigidBody(Entity owner, Transform transform, float mass,
			ICollisionShape shape) {
		btDefaultMotionState motionState = new btDefaultMotionState(fromTransformToMatrix4(transform));
		btRigidBodyConstructionInfo constructionInfo = new btRigidBodyConstructionInfo(mass, motionState,
				(btCollisionShape) shape.getCollisionShape());
		return new NativeRigidBody(new btRigidBody(constructionInfo), owner);
	}

	// TODO Add slider constraint
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
		return new NativeCollisionShape(compoundShape);

	}
}