/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import net.gliby.gman.DataWatchableVector3f;
import net.gliby.physics.Physics;
import net.gliby.physics.common.entity.mechanics.RigidBodyMechanic;
import net.gliby.physics.common.game.items.ItemPhysicsGun;
import net.gliby.physics.common.physics.PhysicsOverworld;
import net.gliby.physics.common.physics.PhysicsWorld;
import net.gliby.physics.common.physics.engine.IRigidBody;
import net.gliby.physics.common.physics.mechanics.physicsgun.OwnedPickedObject;
import net.gliby.physics.common.physics.mechanics.physicsgun.PickUpMechanic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 */
public abstract class EntityPhysicsBase extends Entity implements IEntityAdditionalSpawnData, IEntityPhysics {

	public List<RigidBodyMechanic> mechanics = new ArrayList<RigidBodyMechanic>();
	protected PhysicsWorld physicsWorld;

	private int pickerId = -1;

	public EntityPlayer pickerEntity;

	// Shared
	public Vector3f pickLocalHit = new Vector3f(0, 0, 0);

	/**
	 * Client or Load constructor.
	 * 
	 * @param worldIn
	 */
	public EntityPhysicsBase(World world) {
		super(world);
	}

	/**
	 * If true, entity network will be updated.
	 * 
	 * @return
	 */
	public abstract boolean isDirty();

	/**
	 * Server constructor.
	 * 
	 * @param worldIn
	 */

	public EntityPhysicsBase(World world, PhysicsWorld physicsWorld) {
		super(world);
		this.physicsWorld = physicsWorld;
	}

	private long lastTimeActive;

	public void spawnRemoveParticle() {
		if (this.worldObj.isRemote) {
			for (int i = 0; i < 20; ++i) {
				double d0 = this.rand.nextGaussian() * 0.02D;
				double d1 = this.rand.nextGaussian() * 0.02D;
				double d2 = this.rand.nextGaussian() * 0.02D;
				double d3 = 10.0D;
				this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
						this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width
								- d0 * d3,
						this.posY + (double) (this.rand.nextFloat() * this.height) - d1 * d3, this.posZ
								+ (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width - d2 * d3,
						d0, d1, d2, new int[0]);
			}
		}
	}

	@Override
	public void setDead() {
		spawnRemoveParticle();
		for (int i = 0; i < mechanics.size(); i++) {
			mechanics.get(i).dispose();
		}
		this.mechanics.clear();

		if (!worldObj.isRemote) {
			if (doesPhysicsObjectExist()) {
				Vector3f minBB = new Vector3f(), maxBB = new Vector3f();
				this.getRigidBody().getAabb(minBB, maxBB);
				physicsWorld.awakenArea(minBB, maxBB);
			}
			this.dispose();
		}
		super.setDead();
	}

	private int watchablePickerId;
	private DataWatchableVector3f watchablePickHit;

	public void pick(Entity picker, Vector3f pickPoint) {
		this.pickerEntity = (EntityPlayer) picker;
		this.pickerId = picker.getEntityId();
		this.pickLocalHit = pickPoint;
		dataWatcher.updateObject(watchablePickerId, pickerId);
		this.watchablePickHit.write(pickLocalHit);
	}

	public void unpick() {
		this.pickerEntity = null;
		this.pickerId = -1;
		this.pickLocalHit = new Vector3f();
		dataWatcher.updateObject(watchablePickerId, this.pickerId);
	}

	/**
	 * 
	 */
	protected abstract void dispose();

	public abstract void onCommonInit();

	public abstract void onServerInit();

	public abstract void onCommonUpdate();

	public abstract void onServerUpdate();

	@SideOnly(Side.CLIENT)
	public abstract void onClientInit();

	@SideOnly(Side.CLIENT)
	public abstract void onClientUpdate();

	@SideOnly(Side.CLIENT)
	public abstract void interpolate();

	/**
	 * @return
	 */
	protected boolean doesPhysicsObjectExist() {
		return false;
	}

	protected abstract void createPhysicsObject(PhysicsWorld physicsWorld);

	protected abstract void updatePhysicsObject(PhysicsWorld physicsWorld);

	/**
	 * @return
	 */
	public abstract IRigidBody getRigidBody();

	@Override
	public void readEntityFromNBT(NBTTagCompound tagCompound) {
		if (!worldObj.isRemote) {
			mechanics.clear();
			PhysicsOverworld overworld = Physics.getInstance().getPhysicsOverworld();
			Gson gson = new Gson();
			// TODO Get this working!
			/*
			 * if (tagCompound.hasKey("Properties")) {
			 * this.getRigidBody().getProperties()
			 * .putAll(gson.fromJson(tagCompound.getString("Properties"),
			 * Map.class)); }
			 */
			ArrayList<String> mechanicsByNames = gson.fromJson(tagCompound.getString("Mechanics"), ArrayList.class);
			if (mechanicsByNames != null) {
				for (int i = 0; i < mechanicsByNames.size(); i++) {
					String mechanicString = mechanicsByNames.get(i);
					RigidBodyMechanic mechanic = overworld.getMechanicFromName(mechanicString);
					if (mechanic != null)
						mechanics.add(overworld.getMechanicFromName(mechanicString));
				}
			}
		}
	}

	public void writeEntityToNBT(NBTTagCompound tagCompound) {
		Gson gson = new Gson();
		ArrayList<String> mechanicsByNames = new ArrayList<String>();
		for (int i = 0; i < mechanics.size(); i++) {
			mechanicsByNames
					.add(Physics.getInstance().getPhysicsOverworld().getMechanicsMap().inverse().get(mechanics.get(i)));
		}
		// TODO Get this working!
		// tagCompound.setString("Properties",
		// gson.toJson(this.getRigidBody().getProperties()));
		tagCompound.setString("Mechanics", gson.toJson(mechanicsByNames));
	}

	@Override
	public final void entityInit() {
		this.watchablePickHit = new DataWatchableVector3f(this, pickLocalHit = new Vector3f());
		this.watchablePickerId = watchablePickHit.getNextIndex();
		this.dataWatcher.addObject(watchablePickerId, pickerId);
		onCommonInit();
		if (this.worldObj.isRemote)
			onClientInit();
		else
			onServerInit();

	}

	@Override
	public final void onUpdate() {
		super.onUpdate();
		if (this.worldObj.isRemote) {
			int pickerId = dataWatcher.getWatchableObjectInt(watchablePickerId);
			if (pickerId != -1) {
				Entity entity = this.worldObj.getEntityByID(pickerId);
				if (entity instanceof EntityPlayer) {
					this.pickerEntity = (EntityPlayer) entity;
					if (this.pickerEntity != null) {
						watchablePickHit.read(this.pickLocalHit = new Vector3f());
					} else {
						this.pickLocalHit = null;
					}
				}
			} else {
				this.pickerEntity = null;
			}
			onClientUpdate();
		} else {
			onServerUpdate();
			// Check if picker exists.
			if (pickerEntity != null) {
				PickUpMechanic mechanic = (PickUpMechanic) physicsWorld.getMechanics().get("PickUp");
				// Continue if mechanic exists.
				if (mechanic != null) {
					Item item = pickerEntity.getHeldItem() != null ? pickerEntity.getHeldItem().getItem() : null;
					// Check if held item isn't physics gun.
					if (!(item instanceof ItemPhysicsGun)) {
						OwnedPickedObject object = null;
						// Continue if picked object exists.
						if ((object = mechanic.getOwnedPickedObject(pickerEntity)) != null) {
							// Alert's dataWatcher that item shouldn't be
							// picked.
							unpick();

							// Remove picked object
							mechanic.removeOwnedPickedObject(object);
						}
					}
				}
			}

			if (getRigidBody() != null) {
				if (getRigidBody().getProperties().containsKey(EnumRigidBodyProperty.DEAD.getName())) {
					System.out.println("Set dead: " + getRigidBody().getProperties());
					this.setDead();
				}
				if (getRigidBody().isActive())
					lastTimeActive = System.currentTimeMillis();
				if ((System.currentTimeMillis() - lastTimeActive) / 1000 > Physics.getInstance().getSettings()
						.getFloatSetting("PhysicsEntities.InactivityDeathTime").getFloatValue()) {
					this.setDead();
				}
			}
		}

		// Update mechanics.
		for (int i = 0; i < mechanics.size(); i++) {
			RigidBodyMechanic mechanic = (RigidBodyMechanic) mechanics.get(i);
			if (mechanic.isEnabled())
				mechanic.update(getRigidBody(), physicsWorld, this, worldObj.isRemote ? Side.CLIENT : Side.SERVER);
		}
	}

	/**
	 * @return
	 */
	public abstract AxisAlignedBB getRenderBoundingBox();

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		List<RigidBodyMechanic> clientMechanics = new ArrayList<RigidBodyMechanic>();
		for (int i = 0; i < mechanics.size(); i++) {
			RigidBodyMechanic mechanic = mechanics.get(i);
			if (mechanic.isCommon()) {
				clientMechanics.add(mechanic);
			}
		}

		PhysicsOverworld overworld = Physics.getInstance().getPhysicsOverworld();

		buffer.writeInt(clientMechanics.size());
		for (int i = 0; i < clientMechanics.size(); i++) {
			ByteBufUtils.writeUTF8String(buffer, overworld.getMechanicsMap().inverse().get(clientMechanics.get(i)));
		}

		buffer.writeInt(pickerId);
		buffer.writeFloat(this.pickLocalHit.x);
		buffer.writeFloat(this.pickLocalHit.y);
		buffer.writeFloat(this.pickLocalHit.z);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		int size = buffer.readInt();
		PhysicsOverworld overworld = Physics.getInstance().getPhysicsOverworld();
		for (int i = 0; i < size; i++) {
			String mechanicName = ByteBufUtils.readUTF8String(buffer);
			RigidBodyMechanic mechanic = overworld.getMechanicFromName(mechanicName);
			mechanics.add(mechanic);
		}

		this.pickerEntity = (EntityPlayer) this.worldObj.getEntityByID(buffer.readInt());
		Vector3f readPick = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		if (pickerEntity != null)
			this.pickLocalHit = readPick;
	}

}
