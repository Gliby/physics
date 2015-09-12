/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.entity;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;

import io.netty.buffer.ByteBuf;
import net.gliby.gman.BlockUtility;
import net.gliby.physics.Physics;
import net.gliby.physics.client.render.Render;
import net.gliby.physics.common.entity.datawatcher.DataWatchableQuat4f;
import net.gliby.physics.common.entity.datawatcher.DataWatchableVector3f;
import net.gliby.physics.common.physics.ICollisionShape;
import net.gliby.physics.common.physics.IRigidBody;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.ServerPhysicsOverworld;
import net.gliby.physics.common.physics.block.PhysicsBlockMetadata;
import net.gliby.physics.common.physics.entitymechanics.RigidBodyMechanic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 */
public class EntityPhysicsBlock extends EntityPhysicsBase implements IEntityAdditionalSpawnData {
	/**
	 * @param world
	 */
	public EntityPhysicsBlock(World world) {
		super(world);
		noClip = true;
		setSize(0.85f, 1.05f);
	}

	private Item dropItem;

	public EntityPhysicsBlock setDropItem(Item dropItem) {
		this.dropItem = dropItem;
		return this;
	}

	private IBlockState blockState;

	@SideOnly(Side.CLIENT)
	public Vector3f renderPosition;
	@SideOnly(Side.CLIENT)
	public Quat4f renderRotation;

	private Vector3f position = new Vector3f();
	private Quat4f rotation = new Quat4f();
	private IRigidBody rigidBody;

	private Vector3f spawnLocation;
	private ICollisionShape collisionShape;

	private boolean overrideCollisionShape, collisionEnabled;
	private float mass;
	private float friction;

	/**
	 * Enables entity->entity collision.
	 */
	@Override
	public AxisAlignedBB getBoundingBox() {
		return collisionEnabled ? AxisAlignedBB.fromBounds(0.20f, 0, 0.20f, 0.80f, 1.05f, 0.80f).offset(
				Math.round(position.x * 100.0f) / 100.0f, Math.round(position.y * 100.0f) / 100.0f,
				Math.round(position.z * 100.0f) / 100.0f) : null;
	}

	public EntityPhysicsBlock(World world, PhysicsWorld physicsWorld, IBlockState blockState, float x, float y,
			float z) {
		super(world, physicsWorld);
		this.blockState = blockState;
		this.spawnLocation = new Vector3f(x, y, z);
		this.position.set(spawnLocation);

		QuaternionUtil.setEuler(rotation, 0, 0, 0);
		this.rotation.set(rotation);

		ServerPhysicsOverworld overworld = Physics.getInstance().getCommonProxy().getPhysicsOverworld();

		PhysicsBlockMetadata metadata = overworld.getPhysicsBlockMetadata()
				.get(overworld.getBlockIdentity(blockState.getBlock()));
		boolean metadataExists = metadata != null;

		this.mass = metadataExists ? metadata.mass : 10;
		this.friction = metadataExists ? metadata.friction : 0.5f;
		this.overrideCollisionShape = metadataExists ? metadata.overrideCollisionShape : false;
		this.collisionEnabled = metadataExists ? metadata.collisionEnabled : true;

		try {
			if (this.overrideCollisionShape)
				this.collisionShape = physicsWorld.createBoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
			else
				this.collisionShape = physicsWorld.createBlockShape(this.worldObj,
						new BlockPos(spawnLocation.x, spawnLocation.y, spawnLocation.z), blockState);
		} catch (IllegalArgumentException e) {
			this.collisionShape = physicsWorld.createBoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
			Physics.getLogger().error("Block doesn't exist, couldn't create collision shape");
		}
		createPhysicsObject(physicsWorld);
		if (metadata != null) {
			if (metadata.mechanics != null)
				this.mechanics.addAll(metadata.mechanics);
		}

		this.setPositionAndUpdate(spawnLocation.x, spawnLocation.y, spawnLocation.z);
	}

	private Vector3f linearVelocity, angularVelocity;

	@Override
	protected void createPhysicsObject(PhysicsWorld physicsWorld) {
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(this.position);
		transform.setRotation(this.rotation);
		// TODO Capitalize getProperties.put names!

		rigidBody = physicsWorld.createRigidBody(this, transform, Math.abs(mass), collisionShape);
		rigidBody.getProperties().put("BlockState", blockState);

		for (int i = 0; i < mechanics.size(); i++) {
			RigidBodyMechanic mechanic = mechanics.get(i);
			mechanic.onCreatePhysics(rigidBody);
		}

		if (collisionEnabled)
			physicsWorld.addRigidBody(rigidBody);
		else
			physicsWorld.addRigidBody(rigidBody, CollisionFilterGroups.CHARACTER_FILTER,
					(short) (CollisionFilterGroups.ALL_FILTER));

		if (mass < 0)
			rigidBody.setGravity(new Vector3f());
		rigidBody.setFriction(friction);
		if (linearVelocity != null)
			rigidBody.setLinearVelocity(linearVelocity);
		if (angularVelocity != null)
			rigidBody.setAngularVelocity(angularVelocity);
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

	@SideOnly(Side.CLIENT)
	@Override
	public int getBrightnessForRender(float p_70070_1_) {
		AxisAlignedBB renderAABB = this.getRenderBoundingBox();
		float width = (float) (renderAABB.maxX - renderAABB.minX);
		float height = (float) (renderAABB.maxY - renderAABB.minY);
		float length = (float) (renderAABB.maxZ - renderAABB.minZ);
		float lightX = (float) (renderAABB.minX + (width / 2));
		float lightY = (float) (renderAABB.minY + (height / 2));
		float lightZ = (float) (renderAABB.minZ + (length / 2));

		BlockPos blockpos = new BlockPos(lightX, lightY, lightZ);

		if (this.worldObj.isBlockLoaded(blockpos)) {
			int lightValue = this.worldObj.getCombinedLight(blockpos, 0);
			return lightValue;
		} else {
			return 0;
		}
	}

	@Override
	public void onServerUpdate() {
		if (rigidBody != null) {
			position.set(rigidBody.getPosition().getX(), rigidBody.getPosition().getY(),
					rigidBody.getPosition().getZ());
			rotation.set(rigidBody.getRotation().getX(), rigidBody.getRotation().getY(), rigidBody.getRotation().getZ(),
					rigidBody.getRotation().getW());
			setPositionAndUpdate(position.x + 0.5f, position.y, position.z + 0.5f);
			if (watchablePosition != null) {
				if (isDirty() && (!watchablePosition.lastWrote.equals(position)
						|| !watchableRotation.lastWrote.equals(rotation))) {
					watchableRotation.write(rotation);
					watchablePosition.write(position);
				}
			}
		}
	}

	@Override
	public void onClientUpdate() {
		// No-collision check.
		this.onGround = false;
		watchablePosition.read(position);
		watchableRotation.read(rotation);

		this.setEntityBoundingBox(getRenderBoundingBox());
		// setPosition(renderPosition.x + 0.5f, renderPosition.y,
		// renderPosition.z + 0.5f);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tagCompound) {
		try {
			tagCompound.setInteger("Block", Block.getStateId(blockState));
			tagCompound.setBoolean("CollisionEnabled", collisionEnabled);
			tagCompound.setFloat("Mass", mass);
			tagCompound.setFloat("Friction", friction);
			tagCompound.setBoolean("OverrideCollisionShape", overrideCollisionShape);
			tagCompound.setString("CollisionShape", physicsWorld.writeBlockCollisionShape(collisionShape));
			tagCompound.setString("Rotation", rotation.x + ":" + rotation.y + ":" + rotation.z + ":" + rotation.w);
			Vector3f linearVelocity = rigidBody.getLinearVelocity(new Vector3f());
			tagCompound.setString("LinearVelocity", linearVelocity.x + ":" + linearVelocity.y + ":" + linearVelocity.z);
			Vector3f angularVelocity = rigidBody.getAngularVelocity(new Vector3f());
			tagCompound.setString("AngularVelocity",
					angularVelocity.x + ":" + angularVelocity.y + ":" + angularVelocity.z);
			super.writeEntityToNBT(tagCompound);
		} catch (Exception e) {
			// TODO Remove
		}
	}

	// FIXME Block's don't get saved.
	@Override
	public void readEntityFromNBT(NBTTagCompound tagCompound) {
		try {
			this.blockState = Block.getStateById(tagCompound.getInteger("Block"));
			this.collisionEnabled = tagCompound.getBoolean("CollisionEnabled");

			this.mass = tagCompound.getFloat("Mass");
			this.friction = tagCompound.getFloat("Friction");
			this.overrideCollisionShape = tagCompound.getBoolean("OverrideCollisionShape");
			this.physicsWorld = Physics.getInstance().getCommonProxy().getPhysicsOverworld()
					.getPhysicsByWorld(this.worldObj);
			this.collisionShape = physicsWorld.readBlockCollisionShape(tagCompound.getString("CollisionShape"));
			String locationString[] = tagCompound.getString("Location").split(":");
			// Read spawn location.
			this.spawnLocation = new Vector3f((float) this.posX - 0.5f, (float) posY, (float) posZ - 0.5f);
			// Read quat rotation string, and set.
			String rotationString[] = tagCompound.getString("Rotation").split(":");
			this.rotation.set(new Quat4f(Float.parseFloat(rotationString[0]), Float.parseFloat(rotationString[1]),
					Float.parseFloat(rotationString[2]), Float.parseFloat(rotationString[3])));

			String linearVelocity[] = tagCompound.getString("LinearVelocity").split(":");
			this.linearVelocity = new Vector3f(Float.parseFloat(linearVelocity[0]), Float.parseFloat(linearVelocity[1]),
					Float.parseFloat(linearVelocity[2]));

			String angularVelocity[] = tagCompound.getString("AngularVelocity").split(":");
			this.angularVelocity = new Vector3f(Float.parseFloat(angularVelocity[0]),
					Float.parseFloat(angularVelocity[1]), Float.parseFloat(angularVelocity[2]));

			if (watchablePosition == null && watchableRotation == null) {
				watchablePosition = new DataWatchableVector3f(this, position);
				watchableRotation = new DataWatchableQuat4f(this, rotation);
			}
			super.readEntityFromNBT(tagCompound);
		} catch (Exception e) {

		}
	}

	// FIXME Causing java.util.ConcurrentModificationException
	// TODO Probable causes: Saving, dupe entities.
	@Override
	public void writeSpawnData(ByteBuf buffer) {
		super.writeSpawnData(buffer);

		if (watchablePosition == null && watchableRotation == null) {
			watchablePosition = new DataWatchableVector3f(this, position);
			watchableRotation = new DataWatchableQuat4f(this, rotation);
		}

		BlockUtility.serializeBlockState(blockState, buffer);
		buffer.writeBoolean(collisionEnabled);
		buffer.writeFloat(position.x);
		buffer.writeFloat(position.y);
		buffer.writeFloat(position.z);

		buffer.writeFloat(rotation.x);
		buffer.writeFloat(rotation.y);
		buffer.writeFloat(rotation.z);
		buffer.writeFloat(rotation.w);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		super.readSpawnData(buffer);
		this.blockState = BlockUtility.deserializeBlockState(buffer);
		this.collisionEnabled = buffer.readBoolean();
		this.position = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		this.rotation = new Quat4f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		this.renderPosition = new Vector3f(position);
		this.renderRotation = new Quat4f(rotation);
		if (watchablePosition == null && watchableRotation == null) {
			watchablePosition = new DataWatchableVector3f(this, position);
			watchableRotation = new DataWatchableQuat4f(this, rotation);
		}

		if (blockState.getBlock().getLightValue() > 0 && !hasLight) {
			Render.getLightHandler().create(this, blockState.getBlock().getLightValue());
			hasLight = true;
		}

	}

	private @SideOnly(Side.CLIENT) boolean hasLight;

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
		return isBurning();
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	/**
	 * @return
	 */
	public IBlockState getBlockState() {
		return blockState;
	}

	@Override
	public boolean isDirty() {
		return rigidBody.isActive();
	}

	@Override
	protected boolean doesPhysicsObjectExist() {
		return rigidBody != null;
	}

	/**
	 * @return
	 */
	public IRigidBody getRigidBody() {
		return rigidBody;
	}

	@Override
	protected void dispose() {
		// Drops item.
		if (dropItem != null) {
			Vector3f centerOfMass = rigidBody.getCenterOfMassPosition();
			entityDropItem(new ItemStack(dropItem), 0);
			/*
			 * Block.spawnAsEntity(worldObj, new BlockPos(centerOfMass.x + 0.5f,
			 * centerOfMass.y + 0.5F, centerOfMass.z + 0.5F), new
			 * ItemStack(dropItem));
			 */
		}

		if (doesPhysicsObjectExist()) {
			physicsWorld.removeRigidBody(this.rigidBody);
		}
	}

	@Override
	public void interpolate() {
		this.renderPosition.interpolate(position, 0.15f);
		this.renderRotation.interpolate(rotation, 0.15f);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return AxisAlignedBB.fromBounds(-0.2f, -0.2f, -0.2f, 1.3f, 1.2f, 1.2f).offset(renderPosition.x,
				renderPosition.y, renderPosition.z);
	}
}