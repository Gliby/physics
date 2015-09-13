/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.entity;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.linearmath.Transform;

import io.netty.buffer.ByteBuf;
import net.gliby.gman.DataWatchableQuat4f;
import net.gliby.gman.DataWatchableVector3f;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.IRigidBody;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 */
//TODO REWRITE
public class EntityPhysicsModelPart extends EntityPhysicsBase implements IEntityAdditionalSpawnData {
	/**
	 * @param world
	 */
	public EntityPhysicsModelPart(World world) {
		super(world);
		noClip = true;
		setSize(1.5f, 1.5f);
	}

	@SideOnly(Side.CLIENT)
	public Vector3f renderPosition = new Vector3f();
	@SideOnly(Side.CLIENT)
	public Quat4f renderRotation = new Quat4f();
	@SideOnly(Side.CLIENT)
	private Vector3f networkPosition = new Vector3f();
	@SideOnly(Side.CLIENT)
	private Quat4f networkRotation = new Quat4f();
	@SideOnly(Side.CLIENT)
	public Vector3f renderExtent = new Vector3f();

	private Transform transform;
	private IRigidBody rigidBody;

	public EntityPhysicsModelPart(World world, PhysicsWorld physicsWorld, Transform offset, Vector3f extent, float x, float y, float z, float rotationY) {
		super(world, physicsWorld);
		setPositionAndUpdate(x, y, z);
		transform = new Transform();
		transform.setIdentity();
		transform.origin.set(x, y, z);
		transform.origin.add(offset.origin);
		transform.basis.add(offset.basis);
		rigidBody = physicsWorld.createRigidBody(this, transform, 10, physicsWorld.createBoxShape(extent));
		rigidBody.setWorldTransform(transform);
		physicsWorld.addRigidBody(rigidBody);
	}

	private DataWatchableVector3f watchablePosition;
	private DataWatchableQuat4f watchableRotation;

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
		rigidBody.getWorldTransform(transform);
		Vector3f centerOfMass = new Vector3f();
		rigidBody.getCenterOfMassPosition(centerOfMass);

		setPositionAndUpdate(centerOfMass.x, centerOfMass.y, centerOfMass.z);

		if (isDirty()) {
			if (watchableRotation != null) {
				watchableRotation.write(transform.getRotation(new Quat4f()));
				watchablePosition.write(transform.origin);
			}
		}
	}

	@Override
	public void onClientUpdate() {
		if (watchablePosition != null) {
			watchablePosition.read(networkPosition);
			watchableRotation.read(networkRotation);
		}
		this.setEntityBoundingBox(new AxisAlignedBB(renderPosition.x, renderPosition.y, renderPosition.z, renderPosition.x + 1.3f, renderPosition.y + +1.3f, renderPosition.z + +1.3f));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tagCompound) {
		// Temporary setDead(), we don't want to save yet.
		setDead();

	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tagCompound) {
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		super.writeSpawnData(buffer);
		if (watchablePosition == null && watchableRotation == null) {
			watchablePosition = new DataWatchableVector3f(this, transform.origin);
			watchableRotation = new DataWatchableQuat4f(this, transform.getRotation(new Quat4f()));
		}
		Vector3f halfExtentsWithMargin = ((BoxShape) rigidBody.getCollisionShape().getCollisionShape()).getHalfExtentsWithMargin(new Vector3f());
		buffer.writeFloat(halfExtentsWithMargin.x);
		buffer.writeFloat(halfExtentsWithMargin.y);
		buffer.writeFloat(halfExtentsWithMargin.z);

		buffer.writeFloat(transform.origin.x);
		buffer.writeFloat(transform.origin.y);
		buffer.writeFloat(transform.origin.z);

		Quat4f rotation = new Quat4f();
		transform.getRotation(rotation);

		buffer.writeFloat(rotation.x);
		buffer.writeFloat(rotation.y);
		buffer.writeFloat(rotation.z);
		buffer.writeFloat(rotation.w);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		super.readSpawnData(buffer);
		this.renderExtent = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		this.networkPosition = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		this.networkRotation = new Quat4f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		this.renderPosition = new Vector3f(networkPosition);
		this.renderRotation = new Quat4f(networkRotation);
		if (watchablePosition == null && watchableRotation == null) {
			watchablePosition = new DataWatchableVector3f(this, networkPosition);
			watchableRotation = new DataWatchableQuat4f(this, networkRotation);
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
		return rigidBody.isActive();
	}

	/**
	 * @return
	 */
	public IRigidBody getRigidBody() {
		return rigidBody;
	}

	@Override
	protected void dispose() {
		if (!worldObj.isRemote) {
			physicsWorld.removeRigidBody(this.rigidBody);
		}
	}

	@Override
	public void interpolate() {
		this.renderPosition.interpolate(networkPosition, 0.15f);
		this.renderRotation.interpolate(networkRotation, 0.15f);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return this.getEntityBoundingBox();
	}

	@Override
	protected void createPhysicsObject(PhysicsWorld physicsWorld) {
	}

	@Override
	protected void updatePhysicsObject(PhysicsWorld physicsWorld) {
	}
}