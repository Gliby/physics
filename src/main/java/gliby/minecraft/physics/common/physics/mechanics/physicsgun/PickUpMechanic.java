package gliby.minecraft.physics.common.physics.mechanics.physicsgun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.bulletphysicsx.linearmath.Transform;

import gliby.minecraft.gman.EntityUtility;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IConstraintPoint2Point;
import gliby.minecraft.physics.common.physics.mechanics.PhysicsMechanic;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

/**
 *
 */
public class PickUpMechanic extends PhysicsMechanic {
	/**
	 * @param physicsWorld
	 * @param ticksPerSecond
	 */
	public PickUpMechanic(PhysicsWorld physicsWorld, boolean threaded, int ticksPerSecond) {
		super(physicsWorld, threaded, ticksPerSecond);
		pickedObjects = new ArrayList<PickedObject>();
		ownedPickedObjects = new HashMap<Integer, OwnedPickedObject>();
	}

	private List<PickedObject> pickedObjects;
	private Map<Integer, OwnedPickedObject> ownedPickedObjects;

	@Override
	public void update() {
		for (int i = 0; i < pickedObjects.size(); i++) {
			PickedObject pickObj = pickedObjects.get(i);
			if (pickObj instanceof OwnedPickedObject) {
				OwnedPickedObject ownedPicked = (OwnedPickedObject) pickObj;
				Vector3 offset = new Vector3(-0.5f, -0.5f, -0.5f);
				Vector3 playerPosition = new Vector3((float) ownedPicked.getOwner().posX,
						(float) ownedPicked.getOwner().posY + ownedPicked.getOwner().getEyeHeight(),
						(float) ownedPicked.getOwner().posZ);
				playerPosition.add(offset);
				Vector3 pickRaw = EntityUtility.calculateRay(ownedPicked.getOwner(), 64, offset);
				pickRaw.add(offset);

				Vector3 newRayTo = new Vector3(pickRaw);
				Vector3 eyePos = new Vector3(playerPosition);
				Vector3 dir = new Vector3();
				dir = newRayTo.sub(eyePos);
				dir.nor();
				dir.scl(ownedPicked.getPickDistance());
				Vector3 newPos = new Vector3();
				newPos = eyePos.add(dir);
				IConstraintPoint2Point p2pConstraint = (IConstraintPoint2Point) ownedPicked.getConstraint();
				p2pConstraint.setPivotB(newPos);
				float size = 1.75f;
				final AxisAlignedBB bb = new AxisAlignedBB(-size, -size, -size, size, size, size).offset(newPos.x,
						newPos.y, newPos.z);
				physicsWorld.awakenArea(new Vector3((float) bb.minX, (float) bb.minY, (float) bb.minZ),
						new Vector3((float) bb.maxX, (float) bb.maxY, (float) bb.maxZ));
				/*
				 * Vector3f directionOffset = new Vector3f(); directionOffset.sub(pickRaw,
				 * eyePos); directionOffset.normalize(); Transform transform = new Transform();
				 * ownedPicked.getRigidBody().getCenterOfMassTransform (transform); //
				 * transform.basis.set(ownedPicked. getOriginalCenterOfMassTransform ().basis);
				 * 
				 * // ownedPicked.getRigidBody().setCenterOfMassTransform(transform ); //
				 * ownedPicked.getRigidBody().setAngularVelocity(new Vector3f(0, // 0, 0)); //
				 * ownedPicked.getRigidBody().setLinearVelocity(new Vector3f(0, // 0, 0));
				 * 
				 * float size = 1.75f; final AxisAlignedBB bb = new AxisAlignedBB(-size, -size,
				 * -size, size, size, size).offset(newPos.x, newPos.y, newPos.z);
				 * this.awakenArea(new Vector3f((float) bb.minX, (float) bb.minY, (float)
				 * bb.minZ), new Vector3f((float) bb.maxX, (float) bb.maxY, (float) bb.maxZ));
				 */
			}
		}
	}

	/**
	 * 
	 */
	public void addOwnedPickedObject(Entity owner, OwnedPickedObject object) {
		addPickedObject(object);
		this.ownedPickedObjects.put(owner.getEntityId(), object);
	}

	/**
	 * 
	 */
	public synchronized void addPickedObject(PickedObject object) {
		if (object.getRigidBody() != null) {
			Vector3 pickPosition = new Vector3(object.getRayCallback().getHitPointWorld());
			Matrix4 centerOfMassTransform = object.getRigidBody().getCenterOfMassTransform(new Matrix4());
			centerOfMassTransform.inv();
			Vector3 relativePivot = new Vector3(pickPosition);
			// TODO bug: needs transform
			centerOfMassTransform.translate(relativePivot);

			IConstraintPoint2Point p2p = physicsWorld.createPoint2PointConstraint(object.getRigidBody(), relativePivot);
			object.setConstraint(p2p);
			Vector3 posToEye = new Vector3();
			posToEye = pickPosition.sub(object.getRayFromWorld());
			object.setPickDistance(posToEye.len());
			object.setOriginalCenterOfMassTransform(object.getRigidBody().getCenterOfMassTransform(new Matrix4()));
			// p2p.setImpulseClamp(3.0f);
			p2p.setTau(0.1f);
			this.physicsWorld.addConstraint(p2p);
			this.pickedObjects.add(object);
		}
	}

	public OwnedPickedObject getOwnedPickedObject(Entity owner) {
		return ownedPickedObjects.get(owner.getEntityId());
	}

	/**
	 * @param object
	 */
	public void removeOwnedPickedObject(OwnedPickedObject object) {
		removePickedObject(object);
		this.ownedPickedObjects.remove(object.getOwner().getEntityId());
	}

	/**
	 * @param object
	 */
	public void removePickedObject(PickedObject object) {
		this.pickedObjects.remove(object);
		if (object.getConstraint() != null) {
			this.physicsWorld.removeConstraint(object.getConstraint());
		}
	}

	@Override
	public String getName() {
		return "PickUpMechanic";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.gliby.physics.common.physics.worldmechanics.PhysicsMechanic#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		pickedObjects.clear();
		ownedPickedObjects.clear();
		physicsWorld = null;

	}
}
