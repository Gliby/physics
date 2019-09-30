package gliby.minecraft.physics.common.entity;
/**
 * @param world
 * @return
 * @param world
 * @return
 * @param world
 * @return
 *//*
package net.gliby.physics.common.entity;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import net.gliby.physics.common.entity.datawatcher.DataWatchableQuat4f;
import net.gliby.physics.common.entity.datawatcher.DataWatchableVector3f;
import net.gliby.physics.common.physics.jbullet.AttachementPoint;
import net.gliby.physics.common.physics.jbullet.ModelPart;
import net.gliby.physics.common.physics.jbullet.OwnedRigidBody;
import net.gliby.physics.common.physics.jbullet.OldWorldPhysicsSimulator;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysics.linearmath.Transform;

*//**
 *
 *//*
public class EntityPhysicsRagdoll extends EntityPhysicsBase implements IEntityAdditionalSpawnData {
	*//**
 * @param world
 *//*
	public EntityPhysicsRagdoll(World world) {
		super(world);
		noClip = true;
		setSize(8.5f, 8.5f);
	}

	OwnedRigidBody[] rigidBodies;
	Generic6DofConstraint[] joints;
	ModelBiped modelBiped;

	public EntityPhysicsRagdoll(World world, OldWorldPhysicsSimulator physicsWorld, ModelBiped modelBiped, float x, float y, float z) {
		super(world, physicsWorld);
		this.modelBiped = modelBiped;
		setPositionAndUpdate(x, y, z);
	}

	*//**
 *
 *//*
	private void createRagdoll() {
		ArrayList<ModelPart> models = generateModelProxies(modelBiped);
		ArrayList<AttachementPoint> points = generateAttachementPoints(modelBiped);
		watchablePositions = new DataWatchableVector3f[models.size()];
		watchableRotations = new DataWatchableQuat4f[models.size()];
		rigidBodies = new OwnedRigidBody[models.size()];
		joints = new Generic6DofConstraint[models.size()];
		HashMap<ModelBox, OwnedRigidBody> rigidBodyMap = new HashMap<ModelBox, OwnedRigidBody>();

		for (int i = 0; i < rigidBodies.length; i++) {
			ModelPart model = models.get(i);
			Transform transform = new Transform();
			transform.setIdentity();
			transform.origin.add(new Vector3f(model.getModelBox().posX1 + model.getModelBox().posX2, model.getModelBox().posY1 + model.getModelBox().posY2, model.getModelBox().posZ1 + model.getModelBox().posZ2));
			transform.origin.scale(0.5f);
			transform.origin.add(model.getPosition());
			transform.origin.scale(-0.0625f);
			// Place in world.
			transform.origin.add(new Vector3f((float) posX, (float) posY, (float) posZ));
			Vector3f extent = new Vector3f(model.getModelBox().posX2 - model.getModelBox().posX1, model.getModelBox().posY2 - model.getModelBox().posY1, model.getModelBox().posZ2 - model.getModelBox().posZ1);
			// Adjust to minecraft's scale.
			extent.scale(0.0625f);
			extent.scale(0.5f);

			OwnedRigidBody body = physicsWorld.constructRigidBody(this, 10, transform, new BoxShape(extent));
			rigidBodyMap.put(model.getModelBox(), body);
			physicsWorld.addRigidBody(body);

			rigidBodies[i] = body;
			watchablePositions[i] = new DataWatchableVector3f(this, transform.origin);
			watchableRotations[i] = new DataWatchableQuat4f(this, transform.getRotation(new Quat4f()));
		}

		// Attach joints
		for (AttachementPoint point : points) {
			if (point.getBodyA() != null && point.getBodyB() != null) {
				OwnedRigidBody bodyA = rigidBodyMap.get(point.getBodyA().getModelBox());
				OwnedRigidBody bodyB = rigidBodyMap.get(point.getBodyB().getModelBox());
				System.out.println("Created: " + bodyA + ", " + bodyB);
				Transform transformA = new Transform();
				transformA.setIdentity();
				transformA.origin.set(point.getPosition());
				transformA.origin.scale(-0.0625f);
				Transform transformB = new Transform();
				transformB.setIdentity();
				transformB.origin.set(point.getPosition());
				transformB.origin.scale(-0.0625f);
				Generic6DofConstraint joint = new Generic6DofConstraint(bodyA, bodyA, transformA, transformB, true);
				physicsWorld.getDiscreteDynamicWorld().addConstraint(joint);
			}
		}
	}

	public ArrayList<ModelPart> generateModelProxies(ModelBase modelBase) {
		ArrayList<ModelPart> proxyList = new ArrayList<ModelPart>();
		for (int i = 0; i < modelBase.getClass().getDeclaredFields().length; i++) {
			Field field = modelBase.getClass().getDeclaredFields()[i];
			field.setAccessible(true);
			Object obj = null;
			try {
				obj = field.get(modelBase);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (obj instanceof ModelRenderer) {
				ModelRenderer modelRenderer = (ModelRenderer) obj;
				for (int i1 = 0; i1 < modelRenderer.cubeList.size(); i1++) {
					ModelBox box = (ModelBox) modelRenderer.cubeList.get(i1);
					Vector3f rotationPoint = new Vector3f(modelRenderer.rotationPointX, modelRenderer.rotationPointY, modelRenderer.rotationPointZ);
					proxyList.add(new ModelPart(rotationPoint, box));
				}
			}
		}

		return proxyList;
	}

	// Attachement points should be generated from ModelProxies.
	public ArrayList<AttachementPoint> generateAttachementPoints(ModelBase modelBase) {
		ArrayList<ModelPart> proxyList = new ArrayList<ModelPart>();
		ArrayList<AttachementPoint> points = new ArrayList<AttachementPoint>();
		for (int i = 0; i < modelBase.getClass().getDeclaredFields().length; i++) {
			Field field = modelBase.getClass().getDeclaredFields()[i];
			field.setAccessible(true);
			Object obj = null;
			try {
				obj = field.get(modelBase);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (obj instanceof ModelRenderer) {
				ModelRenderer modelRenderer = (ModelRenderer) obj;
				for (int i1 = 0; i1 < modelRenderer.cubeList.size(); i1++) {
					ModelBox box = (ModelBox) modelRenderer.cubeList.get(i1);
					Vector3f rotationPoint = new Vector3f(modelRenderer.rotationPointX, modelRenderer.rotationPointY, modelRenderer.rotationPointZ);
					proxyList.add(new ModelPart(rotationPoint, box));
					points.add(new AttachementPoint(rotationPoint));
				}
			}
		}
		// Should points be the same model proxy in size?

		for (int i = 0; i < points.size(); i++) {
			AttachementPoint point = points.get(i);
			for (int j = 0; j < proxyList.size(); j++) {
				ModelPart model = proxyList.get(j);
				float size = 0.07f;
				AxisAlignedBB pointBB = AxisAlignedBB.fromBounds(point.getPosition().x, point.getPosition().y, point.getPosition().z, point.getPosition().x, point.getPosition().y, point.getPosition().z).expand(size, size, size);
				AxisAlignedBB modelBB = AxisAlignedBB.fromBounds(model.getModelBox().posX1, model.getModelBox().posY1, model.getModelBox().posZ1, model.getModelBox().posX2, model.getModelBox().posY2, model.getModelBox().posZ2).offset(model.getPosition().x, model.getPosition().y, model.getPosition().z);
				if (pointBB.intersectsWith(modelBB)) {
					if (point.bodyA == null) point.setBodyA(model);
					else if (point.bodyB == null) {
						point.setBodyB(model);
					}
				}
			}
		}
		return points;
	}

	@Override
	public void onCommonInit() {
	}

	@Override
	public void onClientInit() {
	}

	@Override
	public void onServerInit() {
	}

	@Override
	public void onCommonUpdate() {
	}

	@Override
	public void onServerUpdate() {
		for (int i = 0; i < watchablePositions.length; i++) {
			DataWatchableVector3f watchablePosition = watchablePositions[i];
			DataWatchableQuat4f watchableRotation = watchableRotations[i];
			OwnedRigidBody body = rigidBodies[i];
			if (body.isActive()) {
				Transform transform = body.getWorldTransform(new Transform());
				Quat4f quat = transform.getRotation(new Quat4f());
				watchablePosition.write(transform.origin);
				watchableRotation.write(quat);
			}
		}

		
		 * if (isInLava()) { this.setDead(); }
		 * 
		 * if (isInWater()) { if (applyEnvironmentBehaviour) { float size =
		 * 0.5f; Vector3f bbPos = new Vector3f((centerOfMass.x + (size / 2)),
		 * (centerOfMass.y + (size / 2)), (centerOfMass.z + (size / 2)));
		 * AxisAlignedBB blockBB = new AxisAlignedBB(bbPos.x, bbPos.y, bbPos.z,
		 * bbPos.x + size, bbPos.y + size, bbPos.z + size);
		 * List<BlockInformation> blocks = getLiquidsInBB(blockBB); for (int i =
		 * 0; i < blocks.size(); i++) { BlockInformation block = blocks.get(i);
		 * Material liquidMaterial =
		 * block.getBlockState().getBlock().getMaterial(); BlockDynamicLiquid
		 * liquidBlock = BlockLiquid.getFlowingBlock(liquidMaterial); Vec3 vec3
		 * = getFlowVector(worldObj, block.getBlockPosition(),
		 * block.getBlockState().getBlock(), liquidMaterial); Vector3f impulse =
		 * new Vector3f((float) vec3.xCoord, (float) vec3.yCoord, (float)
		 * vec3.zCoord); rigidBody.applyCentralImpulse(impulse); } }
		 * 
		 * if (applyEnvironmentGravity) { Vector3f modifiedGravity = new
		 * Vector3f(physicsWorld.waterGravity);
		 * modifiedGravity.scale(random.nextInt(100) / 100.0f);
		 * rigidBody.setGravity(rigidBody.isActive() ? modifiedGravity :
		 * physicsWorld.waterGravity); }
		 * 
		 * } else if (applyEnvironmentGravity) {
		 * rigidBody.setGravity(physicsWorld.gravity); }
		 

		Vector3f centerOfMassPosition = rigidBodies[2].getCenterOfMassPosition(new Vector3f());
		setPositionAndUpdate(centerOfMassPosition.x, centerOfMassPosition.y, centerOfMassPosition.z);

		
		 * if (isDirty()) { Quat4f rotation = new Quat4f();
		 * transform.getRotation(rotation); watchableRotation.write(rotation);
		 * watchablePosition.write(transform.origin); }
		 
	}

	@Override
	public void onClientUpdate() {
		for (int i = 0; i < watchablePositions.length; i++) {
			DataWatchableVector3f watchablePosition = watchablePositions[i];
			DataWatchableQuat4f watchableRotation = watchableRotations[i];
			watchablePosition.read(networkPositions[i]);
			watchableRotation.read(networkRotations[i]);
		}
		// watchablePosition.read(networkPosition);
		// watchableRotation.read(networkRotation);
		// this.setEntityBoundingBox(new AxisAlignedBB(networkPosition.x,
		// networkPosition.y, networkPosition.z, networkPosition.x + 1.3f,
		// networkPosition.y + +1.3f, networkPosition.z + +1.3f));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tagCompound) {
		// Temporary setDead(), we don't want to save yet.
		setDead();

	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tagCompound) {
	}

	// Temp for debug
	@SideOnly(Side.CLIENT)
	public Vector3f[] networkBounds;

	@SideOnly(Side.CLIENT)
	public Vector3f[] networkPositions;
	@SideOnly(Side.CLIENT)
	public Quat4f[] networkRotations;

	@SideOnly(Side.CLIENT)
	public Vector3f[] renderPositions;
	@SideOnly(Side.CLIENT)
	public Quat4f[] renderRotations;

	// Created when entity init's on server, client doesn't create until
	// readSpawnData
	DataWatchableVector3f[] watchablePositions;
	DataWatchableQuat4f[] watchableRotations;

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		// Rigid body creation is done here because extra spawn data doesn't
		// wait for init.
		if (watchablePositions == null && watchableRotations == null) {
			createRagdoll();
		}

		buffer.writeByte(watchablePositions.length);
		for (int i = 0; i < watchablePositions.length; i++) {
			DataWatchableVector3f position = watchablePositions[i];
			DataWatchableQuat4f rotation = watchableRotations[i];
			OwnedRigidBody body = rigidBodies[i];
			Transform transform = body.getWorldTransform(new Transform());
			Quat4f quat = transform.getRotation(new Quat4f());
			buffer.writeFloat(transform.origin.x);
			buffer.writeFloat(transform.origin.y);
			buffer.writeFloat(transform.origin.z);

			buffer.writeFloat(quat.x);
			buffer.writeFloat(quat.y);
			buffer.writeFloat(quat.z);
			buffer.writeFloat(quat.w);

			Vector3f bound = ((BoxShape) body.getCollisionShape()).getHalfExtentsWithMargin(new Vector3f());
			buffer.writeFloat(bound.x);
			buffer.writeFloat(bound.y);
			buffer.writeFloat(bound.z);
		}
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		// Client gets this when he receives the entity, not on spawn.
		if (watchablePositions == null && watchableRotations == null) {
			int size = buffer.readByte();
			watchablePositions = new DataWatchableVector3f[size];
			watchableRotations = new DataWatchableQuat4f[size];
			networkPositions = new Vector3f[size];
			networkRotations = new Quat4f[size];
			renderPositions = new Vector3f[size];
			renderRotations = new Quat4f[size];

			networkBounds = new Vector3f[size];

			for (int i = 0; i < size; i++) {
				DataWatchableVector3f watchablePosition = watchablePositions[i];
				DataWatchableQuat4f watchableRotation = watchableRotations[i];
				// Redundant &&, but correct.
				if (watchablePosition == null && watchableRotation == null) {
					Vector3f pos = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
					Quat4f rot = new Quat4f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
					// Temp bounds, for debugging.
					Vector3f bound = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
					networkBounds[i] = bound;

					networkPositions[i] = pos;
					networkRotations[i] = rot;

					renderPositions[i] = new Vector3f(pos);
					renderRotations[i] = new Quat4f(rot);

					watchablePosition = new DataWatchableVector3f(this, pos);
					watchableRotation = new DataWatchableQuat4f(this, rot);
					watchablePositions[i] = watchablePosition;
					watchableRotations[i] = watchableRotation;
				}
			}
		}

	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderOnFire() {
		return false;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	*//**
 * @return
 *//*
	public OwnedRigidBody[] getRigidBodies() {
		return rigidBodies;
	}

	@Override
	protected void dispose() {
		if (!worldObj.isRemote) {

		}
	}

	*//**
 *
 *//*
	public void interpolate() {
		for (int i = 0; i < networkPositions.length; i++) {
			renderPositions[i].interpolate(networkPositions[i], 0.15f);
			renderRotations[i].interpolate(networkRotations[i], 0.15f);
		}
	}

	 (non-Javadoc)
	 * @see net.gliby.physics.common.entity.EntityPhysicsBase#createPhysicsObject(net.gliby.physics.common.physics.WorldPhysicsSimulator)
	 
	@Override
	protected void createPhysicsObject(OldWorldPhysicsSimulator physicsWorld) {
		// TODO Auto-generated method stub
		
	}

	 (non-Javadoc)
	 * @see net.gliby.physics.common.entity.EntityPhysicsBase#getRigidBody()
	 
	@Override
	public OwnedRigidBody getRigidBody() {
		// TODO Auto-generated method stub
		return null;
	}

	 (non-Javadoc)
	 * @see net.gliby.physics.common.entity.EntityPhysicsBase#getRenderBoundingBox()
	 
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return getEntityBoundingBox();
	}
}*/