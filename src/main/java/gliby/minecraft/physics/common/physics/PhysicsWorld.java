package gliby.minecraft.physics.common.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.google.gson.annotations.SerializedName;

import gliby.minecraft.gman.WorldUtility;
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
import gliby.minecraft.physics.common.physics.mechanics.PhysicsMechanic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 *
 */
public abstract class PhysicsWorld {

	public abstract void tick();

	private IPhysicsWorldConfiguration physicsConfiguration;

	private Vector3 gravityDirection;

	public Vector3 getGravityDirection() {
		return gravityDirection;
	}

	public IPhysicsWorldConfiguration getPhysicsConfiguration() {
		return physicsConfiguration;
	}

	protected boolean enabled;
	protected HashMap<String, PhysicsMechanic> physicsMechanics;

	public Map<String, PhysicsMechanic> getMechanics() {
		return physicsMechanics;
	}

	public PhysicsWorld(IPhysicsWorldConfiguration physicsConfiguration) {
		this.physicsConfiguration = physicsConfiguration;
		this.physicsMechanics = new HashMap<String, PhysicsMechanic>();
		this.gravityDirection = new Vector3(physicsConfiguration.getRegularGravity());
		this.gravityDirection.nor();
		this.enabled = true;
	}

	public void init() {
		Iterator it = physicsMechanics.entrySet().iterator();
		while (it.hasNext()) {
			PhysicsMechanic mechanic = ((Map.Entry<String, PhysicsMechanic>) it.next()).getValue();
			if (mechanic.isThreaded()) {
				mechanic.init();
				new Thread(mechanic, mechanic.getName()).start();
			}
		}
	}

	public ICollisionShape createBlockShape(World worldObj, BlockPos blockPos, IBlockState blockState) {
		if (!blockState.getBlock().isNormalCube()) {
			List<AxisAlignedBB> collisionBBs = new ArrayList<AxisAlignedBB>();
			blockState.getBlock().addCollisionBoxesToList(worldObj, blockPos, blockState, WorldUtility.MAX_BB,
					collisionBBs, null);
			return buildCollisionShape(collisionBBs, new Vector3(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
		}
		Vector3 blockPosition = new Vector3((float) blockState.getBlock().getBlockBoundsMaxX(),
				(float) blockState.getBlock().getBlockBoundsMaxY(), (float) blockState.getBlock().getBlockBoundsMaxZ());
		blockPosition.scl(0.5f);
		return createBoxShape(blockPosition);
	}

	public void dispose() {
		enabled = false;
	}

	protected int tick;

	protected void simulate() {
		if (tick >= getPhysicsConfiguration().getTicksPerSecond())
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

	public abstract ICollisionShape buildCollisionShape(List<AxisAlignedBB> bbs, Vector3 offset);

	public abstract IRigidBody createRigidBody(Entity owner, Matrix4 transform, float mass, ICollisionShape shape);

	public abstract IRigidBody createInertiallessRigidBody(Entity owner, Matrix4 transform, float mass,
			ICollisionShape shape);

	public abstract ICollisionShape createBoxShape(Vector3 extents);

	public abstract IRayResult createClosestRayResultCallback(Vector3 rayFromWorld, Vector3 rayToWorld);

	public abstract void addRope(IRope object);

	public abstract void addRigidBody(final IRigidBody body);

	public abstract void addRigidBody(final IRigidBody body, short collisionFilterGroup, short collisionFilterMask);

	public abstract void removeRigidBody(final IRigidBody body);

	public abstract void awakenArea(final Vector3 min, final Vector3 max);

	public abstract void rayTest(final Vector3 rayFromWorld, final Vector3 rayToWorld, IRayResult resultCallback);

	public abstract void removeCollisionObject(final ICollisionObject collisionObject);

	public abstract void setGravity(final Vector3 newGravity);

	public abstract void addCollisionObject(ICollisionObject object);

	public abstract void addCollisionObject(ICollisionObject collisionObject, short collisionFilterGroup,
			short collisionFilterMask);

	public abstract List<IRigidBody> getRigidBodies();

	public abstract List<IRope> getRopes();

	/**
	 * 
	 * @param startPos
	 * @param endPos
	 * @param detail
	 * @return
	 */
	public abstract IRope createRope(Vector3 startPos, Vector3 endPos, int detail);

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
	public abstract IConstraintPoint2Point createPoint2PointConstraint(IRigidBody rigidBody, Vector3 pivot);

	/**
	 * @param constraint
	 */
	public abstract void addConstraint(IConstraint constraint);

	/**
	 * @param physicsMechanic
	 */
	public void restartMechanic(PhysicsMechanic mechanic) {
		if (mechanic.isThreaded())
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
		public CollisionPart(boolean compoundShape, Matrix4 transform, Vector3 extent) {
			this.compoundShape = compoundShape;
			this.transform = transform;
			this.extent = extent;
		}

		@SerializedName("isCompoundShape")
		public boolean compoundShape;

		@SerializedName("Transform")
		public Matrix4 transform;

		@SerializedName("Extent")
		public Vector3 extent;
	}

	/**
	 * @return
	 */
	public abstract List<IConstraint> getConstraints();

	public abstract IConstraintSlider createSliderConstraint(IRigidBody rbA, IRigidBody rbB, Matrix4 frameInA,
			Matrix4 frameInB, boolean useLinearReferenceFrameA);

	/**
	 * @return
	 */
	public abstract IConstraintGeneric6Dof createGeneric6DofConstraint(IRigidBody rbA, IRigidBody rbB,
			Matrix4 frameInA, Matrix4 frameInB, boolean useLinearReferenceFrameA);

	public abstract ICollisionShape createSphereShape(float radius);

}
