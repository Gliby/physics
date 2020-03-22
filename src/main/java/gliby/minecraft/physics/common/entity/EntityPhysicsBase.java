package gliby.minecraft.physics.common.entity;

import com.google.common.base.Predicate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gliby.minecraft.gman.GMan;
import gliby.minecraft.gman.networking.GDataSerializers;
import gliby.minecraft.physics.Physics;
import gliby.minecraft.physics.common.entity.actions.RigidBodyAction;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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


    // TODO (0.6.0) find a way to determine actual ticks per second, use that.
    public static final int TICKS_PER_SECOND = 20;
    /**
     * The current entity id that is picking this physics entity. Picking meaning manipulating with Physics Manipulator.
     */
    protected static final DataParameter<Integer> PICKER_ID = EntityDataManager.createKey(EntityPhysicsBase.class, DataSerializers.VARINT);
    /**
     * The networked relative (to the RigidBody) offset position for the pick.
     */
    protected static final DataParameter<Vector3f> PICK_OFFSET = EntityDataManager.createKey(EntityPhysicsBase.class, GDataSerializers.VECTOR3F);

    /**
     * Per-entity RigidBody mechanics.
     */
    protected List<RigidBodyAction> actions = new ArrayList<RigidBodyAction>();

    /**
     * Weak ref to current picker entity.
     */
    protected WeakReference<EntityPlayer> pickerEntity;

    /**
     * Weak ref to current physics world.
     */

    protected WeakReference<PhysicsWorld> physicsWorld;
    /**
     * Spawned by player or game event?
     */
    protected boolean gameSpawned;

    /**
     * The actual relative (to the RigidBody) offset position for the pick.
     */
    protected Vector3f pickLocalHit;
    protected static final Gson inclusiveGSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
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

    /**
     * Was spawned in by game events?
     *
     * @return
     */
    public boolean isGameSpawned() {
        return gameSpawned;
    }

    /**
     * Set game spawned flag.
     *
     * @param gameSpawned
     * @return
     */
    public EntityPhysicsBase setGameSpawned(boolean gameSpawned) {
        this.gameSpawned = gameSpawned;
        return this;
    }

    /**
     * Get relative pick offset.
     *
     * @return
     */
    public Vector3f getPickLocalHit() {
        return pickLocalHit;
    }

    /**
     * Get's current RigidBody mechanics.
     *
     * @return
     */
    public List<RigidBodyAction> getActions() {
        return actions;
    }

    /**
     * Sets RigidBody's mechanics.
     *
     * @param actions
     */
    public void setActions(List<RigidBodyAction> actions) {
        this.actions = actions;
    }

    /**
     * Get's current picker entity.
     *
     * @return
     */
    @Nullable
    public EntityPlayer getPickerEntity() {
        if (pickerEntity != null)
            return pickerEntity.get();
        return null;
    }

    /**
     * Set's the current picker entity.
     *
     * @param pickerEntity
     */
    public void setPickerEntity(@Nullable EntityPlayer pickerEntity) {
        this.pickerEntity = new WeakReference<EntityPlayer>(pickerEntity);
    }

    /**
     * Returns whether the RigidBody is currently active, e.g moving/rotation.
     * Used to determine if we should update the network state.
     *
     * @return
     */
    public abstract boolean isDirty();


    public void spawnSmallExplosionParticles() {
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
        spawnSmallExplosionParticles();
        getActions().clear();

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
            if (isPhysicsValid()) {
                Vector3f minBB = new Vector3f(), maxBB = new Vector3f();
                getRigidBody().getAabb(minBB, maxBB);
                getPhysicsWorld().awakenArea(minBB, maxBB);
            }
        }
        return this;
    }

    public EntityPhysicsBase pick(@Nonnull Entity picker, @Nullable Vector3f pickPoint) {
        this.setPickerEntity((EntityPlayer) picker);
        this.pickLocalHit = pickPoint;

        this.dataManager.set(PICKER_ID, Integer.valueOf(picker.getEntityId()));
        if (pickLocalHit != null)
            this.dataManager.set(PICK_OFFSET, pickPoint);
        return this;
    }

    public EntityPhysicsBase unpick() {
        this.pickerEntity = null;
        this.dataManager.set(PICKER_ID, Integer.valueOf(-1));
        if (pickLocalHit != null)
            this.dataManager.set(PICK_OFFSET, pickLocalHit = new Vector3f());
        return this;
    }

    /**
     * Disposes of internal physics.
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
        return physicsWorld != null  ? physicsWorld.get() : null;
    }

    protected boolean isPhysicsValid() {
        return false;
    }

    /**
     * Create's and assigns physics.
     *
     * @param physicsWorld
     */
    protected abstract void createPhysicsObject(PhysicsWorld physicsWorld);

    /**
     * Updates the state of the physics based off the entity data.
     *
     * @param physicsWorld
     */
    protected abstract void updatePhysicsObject(PhysicsWorld physicsWorld);

    /**
     * @return
     */
    public abstract IRigidBody getRigidBody();

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompound) {
        if (!world.isRemote) {
            setGameSpawned(tagCompound.getBoolean("GameSpawned"));

            getActions().clear();
            PhysicsOverworld overworld = Physics.getInstance().getPhysicsOverworld();

            if (tagCompound.hasKey("Properties")) {
                this.getRigidBody().getProperties()
                        .putAll(inclusiveGSON.fromJson(tagCompound.getString("Properties"), HashMap.class));
            }

            ArrayList<String> mechanicsByNames = GMan.getGSON().fromJson(tagCompound.getString("Actions"), ArrayList.class);
            if (mechanicsByNames != null) {
                for (int i = 0; i < mechanicsByNames.size(); i++) {
                    String mechanicString = mechanicsByNames.get(i);
                    RigidBodyAction mechanic = overworld.getActionByName(mechanicString);
                    if (mechanic != null)
                        getActions().add(overworld.getActionByName(mechanicString));
                }
            }

        }
    }

    public void writeEntityToNBT(final NBTTagCompound tagCompound) {
        tagCompound.setBoolean("GameSpawned", isGameSpawned());

        ArrayList<String> mechanicsByNames = new ArrayList<String>();
        for (int i = 0; i < getActions().size(); i++) {
            mechanicsByNames
                    .add(Physics.getInstance().getPhysicsOverworld().getRigidBodyMechanicsMap().inverse().get(getActions().get(i)));
        }

        String gson = inclusiveGSON.toJson(this.getRigidBody().getProperties());
        tagCompound.setString("Properties", gson);

        tagCompound.setString("Actions", GMan.getGSON().toJson(mechanicsByNames));
    }

    @Override
    public final void entityInit() {
        this.dataManager.register(PICK_OFFSET, pickLocalHit = new Vector3f());
        this.dataManager.register(PICKER_ID, Integer.valueOf(-1));

        onCommonInit();
        if (this.world.isRemote)
            onClientInit();
        else
            onServerInit();

    }

    @Override
    public final void onUpdate() {
        super.onUpdate();

        // Update mechanics.
        for (int i = 0; i < getActions().size(); i++) {
            RigidBodyAction mechanic = getActions().get(i);
            if (mechanic.isEnabled()) {

                if (!mechanic.isCommon() && this.world.isRemote)
                    continue;

                mechanic.update(getRigidBody(), getPhysicsWorld(), this, world.isRemote ? Side.CLIENT : Side.SERVER);
            }
        }


        if (this.world.isRemote) {
            int pickerId = this.getDataManager().get(PICKER_ID).intValue();
            if (pickerId != -1) {
                Entity entity = this.world.getEntityByID(pickerId);
                if (entity instanceof EntityPlayer) {
                    setPickerEntity((EntityPlayer) entity);
                    if (this.getPickerEntity() != null) {
                        this.pickLocalHit = dataManager.get(PICK_OFFSET);
                    } else {
                        this.pickLocalHit = null;
                    }
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


                float deathTime = gameSpawned ? Physics.getConfig().getPhysicsEntities().getGameSpawnedExpiryTime() :
                        Physics.getConfig().getPhysicsEntities().getPlayerSpawnedExpiryTime();

                if (((float) (ticksExisted - lastTickActive) / (float) TICKS_PER_SECOND + 1) > deathTime) {
                    this.setDead();
                }

                if (rigidBody.getProperties().containsKey(EnumRigidBodyProperty.DEAD.getName())) {
                    this.setDead();
                }
            }
        }
        onCommonUpdate();
    }

    /**
     * @return
     */
    public abstract AxisAlignedBB getRenderBoundingBox();

    public static final Predicate<Entity> NOT_PHYSICS = entity -> !(entity instanceof EntityPhysicsBase);

    @Override
    public void writeSpawnData(ByteBuf buffer) {

        PhysicsOverworld overworld = Physics.getInstance().getPhysicsOverworld();

        List<String> savedMechanics = new ArrayList<String>();
        for (int i = 0; i < getActions().size(); i++) {
            RigidBodyAction mechanic = getActions().get(i);
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
            RigidBodyAction mechanic = overworld.getActionByName(mechanicName);
            getActions().add(mechanic);
        }

        this.setPickerEntity((EntityPlayer) this.world.getEntityByID(buffer.readInt()));
        Vector3f readPick = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        if (getPickerEntity() != null)
            this.pickLocalHit = readPick;
    }

    /**
     * Sets entity velocity. (motionX, motionY, motionZ)
     *
     * @param x
     * @param y
     * @param z
     */
    public void setEntityVelocity(float x, float y, float z) {
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
    }

    public boolean canBeAttackedWithItem() {
        return false;
    }

    protected boolean canTriggerWalking() {
        return true;
    }


}
