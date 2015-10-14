/**
 * Copyright (c) 2015, Mine Fortress.
 */
package net.gliby.physics.common.entity;

import javax.vecmath.Vector3f;

import io.netty.buffer.ByteBuf;
import net.gliby.physics.client.SoundHandler;
import net.gliby.physics.client.render.RenderUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 */
public class EntityToolGunBeam extends Entity implements IEntityAdditionalSpawnData {

	@SideOnly(Side.CLIENT)
	public Vector3f worldOrigin;

	@SideOnly(Side.CLIENT)
	public Vector3f clientOrigin;

	public Vector3f hit;
	public Entity owner;

	public int msUntilGone = 200;

	/**
	 * @param worldIn
	 */
	public EntityToolGunBeam(World worldIn) {
		super(worldIn);
		setSize(0.1F, 0.1F);
		this.ignoreFrustumCheck = true;
	}

	public EntityToolGunBeam(World worldIn, Entity owner, Vector3f hitPoint) {
		super(worldIn);
		this.noClip = true;
		this.owner = owner;
		this.hit = hitPoint;
		setPositionAndUpdate(owner.posX, owner.posY, owner.posZ);
	}

	@Override
	protected void entityInit() {
	}

	@Override
	public void onUpdate() {
		if (!worldObj.isRemote) {
			if (ticksExisted > 20) {
				this.setDead();
			}
		} else {
			if (ticksExisted == 1) {
				if (!isSilent()) {
					Vector3f soundPosition = new Vector3f(clientOrigin != null ? clientOrigin : worldOrigin);
					soundPosition.sub(hit);
					float distance = soundPosition.length();
					soundPosition.normalize();
					soundPosition.scale(MathHelper.clamp_float(distance, 0, 16));
					soundPosition.add(clientOrigin != null ? clientOrigin : worldOrigin);
					worldObj.playSound(soundPosition.x, soundPosition.y, soundPosition.z,
							SoundHandler.getSoundByIdentifer("ToolGun.Beam"), 1.0F, 1.0F, false);
				}
			}
			float val = MathHelper.clamp_float((System.currentTimeMillis() - timeCreated), 0, msUntilGone)
					/ msUntilGone;
			Vector3f toBe = new Vector3f(clientOrigin != null ? clientOrigin : worldOrigin);
			toBe.interpolate(hit, val);
			setPosition(toBe.x, toBe.y, toBe.z);
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tagCompund) {
		this.setDead();
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tagCompound) {
		// Isn't required.
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeFloat(hit.x);
		buffer.writeFloat(hit.y);
		buffer.writeFloat(hit.z);
		buffer.writeInt(owner.getEntityId());
	}

	public long timeCreated;

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		this.hit = new Vector3f(additionalData.readFloat(), additionalData.readFloat(), additionalData.readFloat());
		this.owner = worldObj.getEntityByID(additionalData.readInt());
		if (owner == Minecraft.getMinecraft().thePlayer) {
			Vec3 firstPersonOffset = new Vec3(owner.onGround ? -0.20D : -0.24D, -0.06D, 0.39D);
			firstPersonOffset = firstPersonOffset
					.rotatePitch(-(owner.prevRotationPitch + (owner.rotationPitch - owner.prevRotationPitch))
							* (float) Math.PI / 180.0F);
			firstPersonOffset = firstPersonOffset.rotateYaw(
					-(owner.prevRotationYaw + (owner.rotationYaw - owner.prevRotationYaw)) * (float) Math.PI / 180.0F);
			clientOrigin = RenderUtilities.toVector3f(firstPersonOffset);
			clientOrigin.add(
					new Vector3f((float) owner.posX, (float) owner.posY + owner.getEyeHeight(), (float) owner.posZ));
		}
		worldOrigin = RenderUtilities.toVector3f(
				RenderUtilities.calculateRay(owner, 1.0f, 1.0f, new Vector3f(-0.30f, owner.getEyeHeight(), 0)));
		timeCreated = System.currentTimeMillis();
	}
}
