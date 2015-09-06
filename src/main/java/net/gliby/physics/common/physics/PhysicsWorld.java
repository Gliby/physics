/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.physics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;
import com.google.common.base.Predicate;
import com.google.gson.annotations.SerializedName;

import net.gliby.gman.settings.INIProperties;
import net.gliby.gman.settings.ObjectSetting;
import net.gliby.gman.settings.Setting.Listener;
import net.gliby.physics.Physics;
import net.gliby.physics.common.physics.worldmechanics.PhysicsMechanic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 *
 */
public abstract class PhysicsWorld implements Runnable {

	protected final int ticksPerSecond;
	protected final Vector3f gravity;
	protected boolean running;
	protected World world;

	protected final static AxisAlignedBB MAX_BB = AxisAlignedBB.fromBounds(Double.MAX_VALUE, Double.MAX_VALUE,
			Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
	protected HashMap<String, PhysicsMechanic> physicsMechanics;

	public abstract boolean shouldSimulate();

	public final int getTicksPerSecond() {
		return ticksPerSecond;
	}

	public Map<String, PhysicsMechanic> getMechanics() {
		return physicsMechanics;
	}

	public PhysicsWorld(World world, int ticksPerSecond, Vector3f gravity) {
		this.world = world;
		this.ticksPerSecond = ticksPerSecond;
		this.running = true;
		this.gravity = gravity;
		physicsMechanics = new HashMap<String, PhysicsMechanic>();
	}

	public void create() {
		Iterator it = physicsMechanics.entrySet().iterator();
		while (it.hasNext()) {
			PhysicsMechanic mechanic = ((Map.Entry<String, PhysicsMechanic>) it.next()).getValue();
			if (mechanic.isThreaded()) {
				mechanic.init();
				new Thread(mechanic, mechanic.getName()).start();
			}
		}
	}

	int tick;

	protected void update() {
		if (tick >= ticksPerSecond)
			tick = 0;
		tick++;
		Iterator it = physicsMechanics.entrySet().iterator();
		while (it.hasNext()) {
			PhysicsMechanic mechanic = ((Map.Entry<String, PhysicsMechanic>) it.next()).getValue();
			if (!mechanic.isThreaded()) {
				if (mechanic.getTicksPerSecond() % tick == 0) {
					mechanic.call();
				}
			}
		}
	}

	public abstract IRigidBody createRigidBody(Entity owner, Transform transform, float mass, ICollisionShape shape);

	public abstract IRigidBody createInertiallessRigidBody(Entity owner, Transform transform, float mass,
			ICollisionShape shape);

	public abstract ICollisionShape createBoxShape(Vector3f extents);

	public abstract IRayResult createClosestRayResultCallback(Vector3f rayFromWorld, Vector3f rayToWorld);

	public abstract void addRope(IRope object);

	public abstract void addRigidBody(final IRigidBody body);

	public abstract void addRigidBody(final IRigidBody body, short collisionFilterGroup, short collisionFilterMask);

	public abstract void removeRigidBody(final IRigidBody body);

	public abstract void awakenArea(final Vector3f min, final Vector3f max);

	public abstract void rayTest(final Vector3f rayFromWorld, final Vector3f rayToWorld, IRayResult resultCallback);

	public abstract void removeCollisionObject(final ICollisionObject collisionObject);

	public abstract void setGravity(final Vector3f newGravity);

	public abstract void addCollisionObject(ICollisionObject object);

	public abstract void addCollisionObject(ICollisionObject collisionObject, short collisionFilterGroup,
			short collisionFilterMask);

	public abstract List<IRigidBody> getRigidBodies();

	public abstract List<IRope> getRopes();

	public void destroy() {
		this.running = false;
		dispose();
	}

	public abstract void dispose();

	public abstract IRope createRope(Vector3f startPos, Vector3f endPos, int detail);

	/**
	 * @param worldObj
	 * @param blockPos
	 * @param blockState
	 * @return
	 */
	public abstract ICollisionShape createBlockShape(World worldObj, BlockPos blockPos, IBlockState blockState);

	/**
	 * @return
	 */
	public abstract IGhostObject createPairCachingGhostObject();

	/**
	 * @param collisionObject
	 * @return
	 */
	public abstract IRigidBody upcastRigidBody(Object collisionObject);

	/**
	 * @param rigidBody
	 * @param pivot
	 * @return
	 */
	public abstract IConstraintPoint2Point createPoint2PointConstraint(IRigidBody rigidBody, Vector3f pivot);

	/**
	 * @param constraint
	 */
	public abstract void addConstraint(IConstraint constraint);

	/**
	 * @param physicsMechanic
	 */
	public void restartMechanic(PhysicsMechanic mechanic) {
		new Thread(mechanic, mechanic.getName()).start();
	}

	/**
	 * @param constraint
	 */
	public abstract void removeConstraint(IConstraint constraint);

	public abstract void removeRope(IRope rope);

	/**
	 * @param string
	 * @return
	 */
	public abstract ICollisionShape readBlockCollisionShape(String string);

	/**
	 * @param shape
	 * @return
	 */
	public abstract String writeBlockCollisionShape(ICollisionShape shape);

	protected class CollisionPart {

		/**
		 * @param transform
		 * @param extent
		 */
		public CollisionPart(boolean compoundShape, Transform transform, Vector3f extent) {
			this.compoundShape = compoundShape;
			this.transform = transform;
			this.extent = extent;
		}

		@SerializedName("isCompoundShape")
		public boolean compoundShape;

		@SerializedName("Transform")
		public Transform transform;

		@SerializedName("Extent")
		public Vector3f extent;
	}

	/**
	 * @return
	 */
	public abstract List<IConstraint> getConstraints();

	public abstract IConstraintSlider createSliderConstraint(IRigidBody rbA, IRigidBody rbB, Transform frameInA,
			Transform frameInB, boolean useLinearReferenceFrameA);

	/**
	 * @return
	 */
	public abstract IConstraintGeneric6Dof createGeneric6DofConstraint(IRigidBody rbA, IRigidBody rbB,
			Transform frameInA, Transform frameInB, boolean useLinearReferenceFrameA);

	public Vector3f getGravity() {
		return gravity;
	}

	public abstract ICollisionShape createSphereShape(float radius);

}
