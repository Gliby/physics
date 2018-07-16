package gliby.minecraft.physics.common.entity;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.bulletphysicsx.collision.shapes.BoxShape;

import gliby.minecraft.gman.DataWatchableQuaternion;
import gliby.minecraft.gman.DataWatchableVector3;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 */
// TODO improvement: needs rewrite
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
	public Vector3 renderPosition = new Vector3();
	public Quaternion renderRotation = new Quaternion();
	@SideOnly(Side.CLIENT)
	private Vector3 networkPosition = new Vector3();
	@SideOnly(Side.CLIENT)
	private Quaternion networkRotation = new Quaternion();
	@SideOnly(Side.CLIENT)
	public Vector3 renderExtent = new Vector3();

	private Matrix4 transform;
	private IRigidBody rigidBody;

	public EntityPhysicsModelPart(World world, PhysicsWorld physicsWorld, Matrix4 offset, Vector3 extent, float x,
			float y, float z, float rotationY) {
		super(world, physicsWorld);
		setPositionAndUpdate(x, y, z);

		Vector3 offsetTranslation = offset.getTranslation(new Vector3());
		Quaternion offsetRotation = offset.getRotation(new Quaternion());

		transform = new Matrix4();
		transform.idt();
		transform.set(new Vector3(x + offsetTranslation.x, y + offsetTranslation.y, z + offsetTranslation.z),
				transform.getRotation(new Quaternion()).add(offsetRotation));
		rigidBody = physicsWorld.createRigidBody(this, transform, 10, physicsWorld.createBoxShape(extent));
		rigidBody.setWorldTransform(transform);
		physicsWorld.addRigidBody(rigidBody);
	}

	private DataWatchableVector3 watchablePosition;
	private DataWatchableQuaternion watchableRotation;

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
		Vector3 centerOfMass = new Vector3();
		rigidBody.getCenterOfMassPosition(centerOfMass);

		setPositionAndUpdate(centerOfMass.x, centerOfMass.y, centerOfMass.z);

		if (isDirty()) {
			if (watchableRotation != null) {
				watchableRotation.write(transform.getRotation(new Quaternion()));
				watchablePosition.write(transform.getTranslation(new Vector3()));
			}
		}
	}

	@Override
	public void onClientUpdate() {
		if (watchablePosition != null) {
			watchablePosition.read(networkPosition);
			watchableRotation.read(networkRotation);
		}
		this.setEntityBoundingBox(new AxisAlignedBB(renderPosition.x, renderPosition.y, renderPosition.z,
				renderPosition.x + 1.3f, renderPosition.y + +1.3f, renderPosition.z + +1.3f));
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
			watchablePosition = new DataWatchableVector3(this, transform.getTranslation(new Vector3()));
			watchableRotation = new DataWatchableQuaternion(this, transform.getRotation(new Quaternion()));
		}
		// TODO re-implement body parts
		/*
		 * Vector3f halfExtentsWithMargin = ((BoxShape)
		 * rigidBody.getCollisionShape().getCollisionShape())
		 * .getHalfExtentsWithMargin(new Vector3());
		 * buffer.writeFloat(halfExtentsWithMargin.x);
		 * buffer.writeFloat(halfExtentsWithMargin.y);
		 * buffer.writeFloat(halfExtentsWithMargin.z);
		 * 
		 * buffer.writeFloat(transform.origin.x); buffer.writeFloat(transform.origin.y);
		 * buffer.writeFloat(transform.origin.z);
		 * 
		 * Quat4f rotation = new Quat4f(); transform.getRotation(rotation);
		 * 
		 * buffer.writeFloat(rotation.x); buffer.writeFloat(rotation.y);
		 * buffer.writeFloat(rotation.z); buffer.writeFloat(rotation.w);
		 */
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		super.readSpawnData(buffer);
		this.renderExtent = new Vector3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		this.networkPosition = new Vector3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		this.networkRotation = new Quaternion(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
				buffer.readFloat());
		this.renderPosition = new Vector3(networkPosition);
		this.renderRotation = new Quaternion(networkRotation);
		if (watchablePosition == null && watchableRotation == null) {
			watchablePosition = new DataWatchableVector3(this, networkPosition);
			watchableRotation = new DataWatchableQuaternion(this, networkRotation);
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
		this.renderPosition.lerp(networkPosition, 0.15f);
		this.renderRotation.slerp(networkRotation, 0.15f);
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