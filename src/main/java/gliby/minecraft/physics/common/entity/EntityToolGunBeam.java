package gliby.minecraft.physics.common.entity;

import gliby.minecraft.physics.client.render.VecUtility;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;

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

    // TODO use ingame time instead of System
    public int msUntilGone = 200;
    public long timeCreated;

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
        if (!world.isRemote) {
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
                    soundPosition.scale(MathHelper.clamp(distance, 0, 16));
                    soundPosition.add(clientOrigin != null ? clientOrigin : worldOrigin);
// todo 1.12.2 port
//                    SoundHandler.getSoundByIdentifer("ToolGun.Beam")
//                    world.playSound(soundPosition.x, soundPosition.y, soundPosition.z, soundEvent, SoundCategory.PLAYERS, 0.2F, 1.0F, false);
                }
            }
            float val = MathHelper.clamp((System.currentTimeMillis() - timeCreated), 0, msUntilGone)
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

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.hit = new Vector3f(additionalData.readFloat(), additionalData.readFloat(), additionalData.readFloat());
        this.owner = world.getEntityByID(additionalData.readInt());
        if (owner == Minecraft.getMinecraft().player) {
            Vec3d firstPersonOffset = new Vec3d(owner.onGround ? -0.20D : -0.24D, -0.06D, 0.39D);
            firstPersonOffset = firstPersonOffset
                    .rotatePitch(-(owner.prevRotationPitch + (owner.rotationPitch - owner.prevRotationPitch))
                            * (float) Math.PI / 180.0F);
            firstPersonOffset = firstPersonOffset.rotateYaw(
                    -(owner.prevRotationYaw + (owner.rotationYaw - owner.prevRotationYaw)) * (float) Math.PI / 180.0F);
            clientOrigin = VecUtility.toVector3f(firstPersonOffset);
            clientOrigin.add(
                    new Vector3f((float) owner.posX, (float) owner.posY + owner.getEyeHeight(), (float) owner.posZ));
        }
        worldOrigin = VecUtility.toVector3f(
                VecUtility.calculateRay(owner, 1.0f, 1.0f, new Vector3f(-0.30f, owner.getEyeHeight(), 0)));
        timeCreated = System.currentTimeMillis();
    }
}
