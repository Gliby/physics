package gliby.minecraft.physics.common.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gliby.minecraft.gman.GMan;
import gliby.minecraft.gman.networking.GDataSerializers;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.entity.mechanics.RigidBodyMechanic;
import gliby.minecraft.physics.common.game.items.ItemPhysicsGun;
import gliby.minecraft.physics.common.physics.PhysicsOverworld;
import gliby.minecraft.physics.common.physics.PhysicsWorld;
import gliby.minecraft.physics.common.physics.engine.IRigidBody;
import gliby.minecraft.physics.common.physics.mechanics.physicsgun.OwnedPickedObject;
import gliby.minecraft.physics.common.physics.mechanics.physicsgun.PickUpMechanic;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// TODO (0.8.0) feature implement proper collision detection/response, stop using minecraft AABB

/**
 *
 */
public abstract class EntityPhysicsBase extends Entity implements IEntityAdditionalSpawnData, IEntityPhysics {


    public static final int TICKS_PER_SECOND = 20;
    protected static final DataParameter<Integer> PICKER_ID = EntityDataManager.createKey(EntityPhysicsBase.class, DataSerializers.VARINT);
    protected static final DataParameter<Vector3f> PICK_OFFSET = EntityDataManager.createKey(EntityPhysicsBase.class, GDataSerializers.VECTOR3F);
    protected List<RigidBodyMechanic> mechanics = new ArrayList<RigidBodyMechanic>();

    protected WeakReference<EntityPlayer> pickerEntity;
    protected WeakReference<PhysicsWorld> physicsWorld;
    /**
     * Spawned by player or game event?
     */
    protected boolean gameSpawned;

    protected Vector3f pickLocalHit;
    Gson inclusiveGSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private int lastTickActive;

    /**
     * Client or Load constructor.
     *
     * @param world
     */
    public EntityPhysicsBase(World world) {

        super(world);
    }

    /**
     * Server constructor.
     *
     * @param world
     */

    public EntityPhysicsBase(World world, PhysicsWorld physicsWorld) {
        super(world);
        this.physicsWorld = new WeakReference<PhysicsWorld>(physicsWorld);
    }

    public boolean isGameSpawned() {
        return gameSpawned;
    }

    public EntityPhysicsBase setGameSpawned(boolean gameSpawned) {
        this.gameSpawned = gameSpawned;
        return this;
    }

    public Vector3f getPickLocalHit() {
        return pickLocalHit;
    }

    public Vector3f getRenderHitPoint() {
        updateLocalPick();
        return pickLocalHit;
    }

    public List<RigidBodyMechanic> getMechanics() {
        return mechanics;
    }

    public void setMechanics(List<RigidBodyMechanic> mechanics) {
        this.mechanics = mechanics;
    }

    public EntityPlayer getPickerEntity() {
        if (pickerEntity != null)
            return pickerEntity.get();
        return null;
    }

    public void setPickerEntity(EntityPlayer pickerEntity) {
        this.pickerEntity = new WeakReference<EntityPlayer>(pickerEntity);
    }

    /**
     * If true, entity network will be updated.
     *
     * @return
     */
    public abstract boolean isDirty();

    public void spawnRemoveParticle() {
        if (this.world.isRemote) {
            for (int i = 0; i < 5; ++i) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                double d3 = 10.0D;
                this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                        this.posX + (double) (this.rand.nextFloat() * this.width / 2 * 2.0F) - (double) this.width / 2
                                - d0 * d3,
                        this.posY + (double) (this.rand.nextFloat() * this.height / 2) - d1 * d3, this.posZ
                                + (double) (this.rand.nextFloat() * this.width / 2 * 2.0F) - (double) this.width / 2 - d2 * d3,
                        d0, d1, d2);
            }
        }
    }

    @Override
    public void setDead() {
        spawnRemoveParticle();
        for (int i = 0; i < getMechanics().size(); i++) {
            getMechanics().get(i).dispose();
        }
        getMechanics().clear();

        if (!world.isRemote) {
            wakeUp();
            dispose();
        }

        super.setDead();
    }

    /**
     * Wake up surrounding RigidBodies.
     *
     * @return
     */
    public EntityPhysicsBase wakeUp() {
        if (!world.isRemote) {
            if (doesPhysicsObjectExist()) {
                Vector3f minBB = new Vector3f(), maxBB = new Vector3f();
                getRigidBody().getAabb(minBB, maxBB);
                getPhysicsWorld().awakenArea(minBB, maxBB);
            }
        }
        return this;
    }

    public void pick(Entity picker, Vector3f pickPoint) {
        this.setPickerEntity((EntityPlayer) picker);
        this.pickLocalHit = pickPoint;
        this.dataManager.set(PICKER_ID, Integer.valueOf(picker.getEntityId()));
        if (pickLocalHit != null)
            this.dataManager.set(PICK_OFFSET, pickLocalHit);
    }

    public void unpick() {
        this.pickerEntity = null;
        this.dataManager.set(PICKER_ID, Integer.valueOf(-1));
        if (pickLocalHit != null)
            this.dataManager.set(PICK_OFFSET, pickLocalHit = new Vector3f());
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

    public PhysicsWorld getPhysicsWorld() {
        return physicsWorld.get();
    }

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
        if (!world.isRemote) {
            setGameSpawned(tagCompound.getBoolean("GameSpawned"));

            getMechanics().clear();
            PhysicsOverworld overworld = Physics.getInstance().getPhysicsOverworld();

            if (tagCompound.hasKey("Properties")) {
                this.getRigidBody().getProperties()
                        .putAll(inclusiveGSON.fromJson(tagCompound.getString("Properties"), HashMap.class));
            }

            ArrayList<String> mechanicsByNames = GMan.getGSON().fromJson(tagCompound.getString("Mechanics"), ArrayList.class);
            if (mechanicsByNames != null) {
                for (int i = 0; i < mechanicsByNames.size(); i++) {
                    String mechanicString = mechanicsByNames.get(i);
                    RigidBodyMechanic mechanic = overworld.getMechanicFromName(mechanicString);
                    if (mechanic != null)
                        getMechanics().add(overworld.getMechanicFromName(mechanicString));
                }
            }

        }
    }

    public void writeEntityToNBT(final NBTTagCompound tagCompound) {
        tagCompound.setBoolean("GameSpawned", isGameSpawned());

        ArrayList<String> mechanicsByNames = new ArrayList<String>();
        for (int i = 0; i < getMechanics().size(); i++) {
            mechanicsByNames
                    .add(Physics.getInstance().getPhysicsOverworld().getRigidBodyMechanicsMap().inverse().get(getMechanics().get(i)));
        }

        String gson = inclusiveGSON.toJson(this.getRigidBody().getProperties());
        tagCompound.setString("Properties", gson);

        tagCompound.setString("Mechanics", GMan.getGSON().toJson(mechanicsByNames));
    }

    @Override
    public void entityInit() {
        this.dataManager.register(PICK_OFFSET, pickLocalHit = new Vector3f());
        this.dataManager.register(PICKER_ID, Integer.valueOf(-1));

        onCommonInit();
        if (this.world.isRemote)
            onClientInit();
        else
            onServerInit();

    }

    public void updateLocalPick() {
        if (this.getPickerEntity() != null) {
            this.pickLocalHit = dataManager.get(PICK_OFFSET);
        } else {
            this.pickLocalHit = null;
        }
    }

    @Override
    public final void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) {
            int pickerId = this.getDataManager().get(PICKER_ID).intValue();
            if (pickerId != -1) {
                Entity entity = this.world.getEntityByID(pickerId);
                if (entity instanceof EntityPlayer) {
                    setPickerEntity((EntityPlayer) entity);
                }
            } else {
                setPickerEntity(null);
            }
            onClientUpdate();
        } else {
            onServerUpdate();
            // Check if picker exists.
            if (getPickerEntity() != null) {
                PickUpMechanic mechanic = (PickUpMechanic) getPhysicsWorld().getMechanics().get("PickUp");
                // Continue if mechanic exists.
                if (mechanic != null) {
                    EntityPlayer picker = getPickerEntity();
                    Item item = picker.getHeldItem(EnumHand.MAIN_HAND) != null ? picker.getHeldItem(EnumHand.MAIN_HAND).getItem() : null;
                    // Check if held item isn't physics gun.
                    if (!(item instanceof ItemPhysicsGun)) {
                        OwnedPickedObject object;
                        // Continue if picked object exists.
                        if ((object = mechanic.getOwnedPickedObject(picker)) != null) {
                            // Alert's dataWatcher that item shouldn't be
                            // picked.
                            unpick();

                            // Remove picked object
                            mechanic.removeOwnedPickedObject(object);
                        }
                    }
                }
            }

            IRigidBody rigidBody = getRigidBody();
            if (rigidBody != null && rigidBody.isValid()) {
                if (rigidBody.isActive())
                    lastTickActive = ticksExisted;


                String deathKey = gameSpawned ? "PhysicsEntities.GameSpawnedDeathTime" : "PhysicsEntities.PlayerSpawnedDeathTime";

                if (((float) (ticksExisted - lastTickActive) / (float) TICKS_PER_SECOND + 1) > Physics.getInstance().getSettings()
                        .getFloatSetting(deathKey).getFloatValue()) {
                    this.setDead();
                }
                // Update mechanics.
                for (int i = 0; i < getMechanics().size(); i++) {
                    RigidBodyMechanic mechanic = getMechanics().get(i);
                    if (mechanic.isEnabled())
                        mechanic.update(getRigidBody(), getPhysicsWorld(), this, world.isRemote ? Side.CLIENT : Side.SERVER);
                }

                if (rigidBody.getProperties().containsKey(EnumRigidBodyProperty.DEAD.getName())) {
                    this.setDead();
                }
            }
        }
    }

    /**
     * @return
     */
    public abstract AxisAlignedBB getRenderBoundingBox();

    @Override
    public void writeSpawnData(ByteBuf buffer) {

        PhysicsOverworld overworld = Physics.getInstance().getPhysicsOverworld();

        List<String> savedMechanics = new ArrayList<String>();
        for (int i = 0; i < getMechanics().size(); i++) {
            RigidBodyMechanic mechanic = getMechanics().get(i);
            String mechanicName = overworld.getRigidBodyMechanicsMap().inverse().get(mechanic);
            if (mechanic.isCommon() && !mechanicName.isEmpty()) {
                savedMechanics.add(mechanicName);
            }
        }

        buffer.writeInt(savedMechanics.size());
        for (int i = 0; i < savedMechanics.size(); i++) {
            ByteBufUtils.writeUTF8String(buffer, savedMechanics.get(i));
        }

        buffer.writeInt((dataManager.get(PICKER_ID)).intValue());

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
            getMechanics().add(mechanic);
        }

        this.setPickerEntity((EntityPlayer) this.world.getEntityByID(buffer.readInt()));
        Vector3f readPick = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        if (getPickerEntity() != null)
            this.pickLocalHit = readPick;
    }

    public boolean canBeAttackedWithItem() {
        return false;
    }

    protected boolean canTriggerWalking() {
        return true;
    }

}
