/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import io.netty.buffer.ByteBuf;
import net.gliby.gman.BlockUtility;
import net.gliby.gman.DataWatchableQuat4f;
import net.gliby.gman.DataWatchableVector3f;
import net.gliby.gman.WorldUtility;
import net.gliby.physics.Physics;
import net.gliby.physics.client.render.RenderHandler;
import net.gliby.physics.common.blocks.PhysicsBlockMetadata;
import net.gliby.physics.common.entity.mechanics.RigidBodyMechanic;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.ServerPhysicsOverworld;
import net.gliby.physics.common.physics.engine.ICollisionShape;
import net.gliby.physics.common.physics.engine.IRigidBody;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 */

// TODO Remove ability to spawn tile-entites.
public class EntityPhysicsBlock extends EntityPhysicsBase implements IEntityAdditionalSpawnData {
	/**
	 * @param world
	 */
	public EntityPhysicsBlock(World world) {
		super(world);
		noClip = true;
		setSize(0.85f, 1.05f);
	}

	private ItemStack dropItem;

	public EntityPhysicsBlock setDropItem(ItemStack dropItem) {
		this.dropItem = dropItem;
		return this;
	}

	private IBlockState blockState;

	@SideOnly(Side.CLIENT)
	public Vector3f renderPosition;
	@SideOnly(Side.CLIENT)
	public Quat4f renderRotation;

	private List<AxisAlignedBB> collisionBBs;

	private Vector3f position = new Vector3f();
	private Quat4f rotation = new Quat4f();

	private IRigidBody rigidBody;
	private ICollisionShape collisionShape;

	private boolean defaultCollisionShape, collisionEnabled = true;
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
		this.position.set(x, y, z);
		QuaternionUtil.setEuler(rotation, 0, 0, 0);
		this.rotation.set(rotation);

		Physics physics = Physics.getInstance();

		PhysicsBlockMetadata metadata = physics.getBlockManager().getPhysicsBlockMetadata()
				.get(physics.getBlockManager().getBlockIdentity(blockState.getBlock()));
		boolean metadataExists = metadata != null;

		this.mass = metadataExists ? metadata.mass : 10;
		this.friction = metadataExists ? metadata.friction : 0.5f;
		this.defaultCollisionShape = metadataExists ? metadata.defaultCollisionShape : false;
		this.collisionEnabled = metadataExists ? metadata.collisionEnabled : true;

		try {
			if (this.defaultCollisionShape)
				this.collisionShape = physicsWorld.createBoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
			else {
				blockState.getBlock().addCollisionBoxesToList(worldObj, new BlockPos(x, y, z), blockState,
						WorldUtility.MAX_BB, collisionBBs = new ArrayList<AxisAlignedBB>(), this);
				for (int i = 0; i < collisionBBs.size(); i++) {
					collisionBBs.set(i, collisionBBs.get(i).offset(-x, -y, -z));
				}

				this.collisionShape = physicsWorld.buildCollisionShape(collisionBBs, VectorUtil.IDENTITY);
			}
		} catch (IllegalArgumentException e) {
			this.collisionShape = physicsWorld.createBoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
			Physics.getLogger().error("Block doesn't exist, couldn't create collision shape");
		}
		createPhysicsObject(physicsWorld);
		if (metadata != null) {
			if (metadata.mechanics != null)
				this.mechanics.addAll(metadata.mechanics);
		}
		setLocationAndAngles(position.x, position.y, position.z, 0, 0);
	}

	private Vector3f linearVelocity, angularVelocity;

	@Override
	protected void createPhysicsObject(PhysicsWorld physicsWorld) {
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(this.position);
		transform.setRotation(this.rotation);
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
			setLocationAndAngles(position.x + 0.5f, position.y, position.z + 0.5f, 0, 0);
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

		ResourceLocation resourcelocation = (ResourceLocation) Block.blockRegistry
				.getNameForObject(blockState.getBlock());
		tagCompound.setString("Block", resourcelocation == null ? "" : resourcelocation.toString());
		tagCompound.setByte("Data", (byte) blockState.getBlock().getMetaFromState(this.blockState));

		tagCompound.setFloat("Mass", mass);
		tagCompound.setFloat("Friction", friction);

		if (defaultCollisionShape)
			tagCompound.setBoolean("DefaultCollisionShape", defaultCollisionShape);
		if (!collisionEnabled)
			tagCompound.setBoolean("CollisionEnabled", collisionEnabled);
		if (collisionBBs != null)
			tagCompound.setString("CollisionShape", new Gson().toJson(collisionBBs));
		// Remove original tags.
		tagCompound.removeTag("Pos");
		tagCompound.removeTag("Rotation");

		// Add removed tags, but with new values.
		tagCompound.setTag("Pos",
				this.newDoubleNBTList(new double[] { this.position.x, this.position.y, this.position.z }));
		tagCompound.setTag("Rotation", this
				.newFloatNBTList(new float[] { this.rotation.x, this.rotation.y, this.rotation.z, this.rotation.w }));

		Vector3f linearVelocity = rigidBody.getLinearVelocity(new Vector3f());
		tagCompound.setTag("LinearVelocity",
				this.newFloatNBTList(new float[] { linearVelocity.x, linearVelocity.y, linearVelocity.z }));
		Vector3f angularVelocity = rigidBody.getAngularVelocity(new Vector3f());
		tagCompound.setTag("AngularVelocity",
				this.newFloatNBTList(new float[] { angularVelocity.x, angularVelocity.y, angularVelocity.z }));

		if (dropItem != null) {
			NBTTagCompound compound = dropItem.writeToNBT(new NBTTagCompound());
			tagCompound.setTag("DropItem", compound);
		}
		super.writeEntityToNBT(tagCompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tagCompound) {
		this.physicsWorld = Physics.getInstance().getCommonProxy().getPhysicsOverworld().getPhysicsByWorld(worldObj);
		int data = tagCompound.getByte("Data") & 255;

		if (tagCompound.hasKey("Block", 8))
			this.blockState = Block.getBlockFromName(tagCompound.getString("Block")).getStateFromMeta(data);
		else if (tagCompound.hasKey("TileID", 99))
			this.blockState = Block.getBlockById(tagCompound.getInteger("TileID")).getStateFromMeta(data);
		else
			this.blockState = Block.getBlockById(tagCompound.getByte("Tile") & 255).getStateFromMeta(data);

		this.mass = (float) (tagCompound.hasKey("Mass") ? tagCompound.getFloat("Mass") : 1.0);
		this.friction = (float) (tagCompound.hasKey("Friction") ? tagCompound.getFloat("Friction") : 0.5);
		this.defaultCollisionShape = tagCompound.hasKey("DefaultCollisionShape")
				? tagCompound.getBoolean("DefaultCollisionShape") : false;
		this.collisionEnabled = tagCompound.hasKey("CollisionEnabled") ? tagCompound.getBoolean("CollisionEnabled")
				: collisionEnabled;

		if (tagCompound.hasKey("CollisionShape")) {
			collisionBBs = new Gson().fromJson(tagCompound.getString("CollisionShape"),
					new TypeToken<List<AxisAlignedBB>>() {
					}.getType());
			this.collisionShape = physicsWorld.buildCollisionShape(collisionBBs, VectorUtil.IDENTITY);
		} else
			this.collisionShape = physicsWorld.createBoxShape(new Vector3f(0.5F, 0.5F, 0.5F));

		this.position.set((float) posX, (float) posY, (float) posZ);
		if (tagCompound.hasKey("Rotation")) {
			NBTTagList list = tagCompound.getTagList("Rotation", 5);
			this.rotation.set(list.getFloat(0), list.getFloat(1), list.get(2) != null ? list.getFloat(2) : 0,
					list.get(3) != null ? list.getFloat(3) : 0);
		}

		if (tagCompound.hasKey("LinearVelocity")) {
			NBTTagList linearVelocity = tagCompound.getTagList("LinearVelocity", 5);
			this.linearVelocity = new Vector3f(linearVelocity.getFloat(0), linearVelocity.getFloat(1),
					linearVelocity.getFloat(2));
		}

		if (tagCompound.hasKey("AngularVelocity")) {
			NBTTagList angularVelocity = tagCompound.getTagList("AngularVelocity", 5);
			this.linearVelocity = new Vector3f(angularVelocity.getFloat(0), angularVelocity.getFloat(1),
					angularVelocity.getFloat(2));
		}

		if (tagCompound.hasKey("DropItem")) {
			NBTTagCompound compound = tagCompound.getCompoundTag("DropItem");
			this.dropItem = ItemStack.loadItemStackFromNBT(compound);
		}
		if (!doesPhysicsObjectExist()) {
			createPhysicsObject(physicsWorld);
		} else {
			updatePhysicsObject(physicsWorld);
		}
		super.readEntityFromNBT(tagCompound);
	}

	@Override
	protected void updatePhysicsObject(PhysicsWorld physicsWorld) {
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(this.position);
		transform.setRotation(this.rotation);
		rigidBody.setWorldTransform(transform);
		rigidBody.getProperties().put("BlockState", blockState);
		if (mass < 0)
			rigidBody.setGravity(new Vector3f());
		rigidBody.setFriction(friction);
		if (linearVelocity != null)
			rigidBody.setLinearVelocity(linearVelocity);
		if (angularVelocity != null)
			rigidBody.setAngularVelocity(angularVelocity);
	}

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
			RenderHandler.getLightHandler().create(this, blockState.getBlock().getLightValue());
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
			entityDropItem(dropItem, 0);
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